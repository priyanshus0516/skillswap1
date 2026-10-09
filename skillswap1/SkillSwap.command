#!/bin/bash
# ================================================================
#  SkillSwap — One-Click Launcher  (double-click this file on Mac)
#  Automatically: checks Java → starts MySQL → sets up DB → runs app
# ================================================================

# ── Keep Terminal window open on error ──────────────────────────
trap 'echo; echo "  Press any key to close..."; read -n1 -s' EXIT

# ── Colors ──────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

info()   { echo -e "${CYAN}[SkillSwap]${NC} $*"; }
ok()     { echo -e "${GREEN}  ✓${NC} $*"; }
warn()   { echo -e "${YELLOW}  ⚠ $*${NC}"; }
err()    { echo -e "${RED}  ✗ $*${NC}" >&2; }
banner() { echo -e "\n${BOLD}${CYAN}━━━  $*  ━━━${NC}\n"; }

# ── Change to the folder this script lives in ───────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

clear
echo -e "${BOLD}${CYAN}"
echo "  ╔═══════════════════════════════════════╗"
echo "  ║        SkillSwap  Auto-Launcher       ║"
echo "  ╚═══════════════════════════════════════╝"
echo -e "${NC}"

# ── Read storage.properties ─────────────────────────────────────
prop() { grep -E "^${1}=" "$SCRIPT_DIR/storage.properties" 2>/dev/null | head -1 | sed 's/^[^=]*=//'; }
DB_USER="$(prop db.user)"
DB_PASS="$(prop db.password)"
DB_URL="$(prop db.url)"
DB_HOST=$(echo "$DB_URL" | sed -E 's|.*://([^:/]+).*|\1|')
DB_PORT=$(echo "$DB_URL" | sed -E 's|.*:([0-9]+)/.*|\1|')
DB_NAME=$(echo "$DB_URL" | sed -E 's|.*/([^?]+).*|\1|')

# ════════════════════════════════════════════════════════════════
# STEP 1 — Java
# ════════════════════════════════════════════════════════════════
banner "Step 1 / 4  —  Java"

_find_java() {
    for c in "${JAVA_HOME:-}/bin/java" \
              "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java" \
              "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java" \
              "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java" \
              "/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home/bin/java"; do
        [ -x "$c" ] && { echo "$c"; return 0; }
    done
    command -v java 2>/dev/null || true
}

JAVA_CMD="$(_find_java)"
JAVAC_CMD="${JAVA_CMD%java}javac"

if [ -z "$JAVA_CMD" ] || ! "$JAVA_CMD" -version &>/dev/null; then
    warn "Java not found — attempting Homebrew install of Temurin 21..."
    if ! command -v brew &>/dev/null; then
        err "Homebrew not found. Please install Java 17+ from https://adoptium.net then double-click again."
        exit 1
    fi
    brew install --cask temurin@21
    JAVA_CMD="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java"
    JAVAC_CMD="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/javac"
fi

JAVA_VER=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/{print $2}' | cut -d. -f1)
[[ "${JAVA_VER:-0}" -lt 11 ]] 2>/dev/null && { err "Need Java 11+, found version $JAVA_VER. Please update Java."; exit 1; }
ok "Java $JAVA_VER  →  $JAVA_CMD"

# ════════════════════════════════════════════════════════════════
# STEP 2 — MySQL
# ════════════════════════════════════════════════════════════════
banner "Step 2 / 4  —  MySQL"

_mysql() {
    mysql --host="$DB_HOST" --port="$DB_PORT" \
          --user="$DB_USER" --password="$DB_PASS" \
          --connect-timeout=5 "$@" 2>/dev/null
}
_mysql_up() { _mysql -e "SELECT 1;" &>/dev/null; }

info "Connecting to MySQL at $DB_HOST:$DB_PORT..."

if ! _mysql_up; then
    warn "MySQL is not running — trying to start it..."

    # Try Homebrew services
    if command -v brew &>/dev/null; then
        SVC=$(brew services list 2>/dev/null | awk '/^mysql/{print $1; exit}')
        if [ -n "$SVC" ]; then
            info "Starting '$SVC' via Homebrew services..."
            brew services start "$SVC" >/dev/null 2>&1 || true
            sleep 5
        fi
    fi

    # Try mysql.server
    if ! _mysql_up && command -v mysql.server &>/dev/null; then
        info "Trying mysql.server start..."
        mysql.server start >/dev/null 2>&1 || true
        sleep 5
    fi

    # Final check
    if ! _mysql_up; then
        err "Cannot connect to MySQL at $DB_HOST:$DB_PORT."
        echo
        echo "  Please start MySQL manually, then double-click this launcher again."
        echo "  Tip: open Terminal and run:  brew services start mysql"
        exit 1
    fi
fi
ok "MySQL is running at $DB_HOST:$DB_PORT"

# ════════════════════════════════════════════════════════════════
# STEP 3 — Database schema + seed data
# ════════════════════════════════════════════════════════════════
banner "Step 3 / 4  —  Database"

info "Applying schema to '$DB_NAME'..."
_mysql < "$SCRIPT_DIR/sql/schema.sql" || { err "Schema setup failed — check your DB credentials in storage.properties"; exit 1; }
ok "Schema applied"

info "Seeding sample data..."
_mysql "$DB_NAME" < "$SCRIPT_DIR/sql/sample_data.sql" || warn "Seed warnings (non-fatal — data may already exist)"
ok "Sample data ready"

# ════════════════════════════════════════════════════════════════
# STEP 4 — Compile (only if source is newer than build output)
# ════════════════════════════════════════════════════════════════
banner "Step 4 / 4  —  Build"

REBUILD=false
[ ! -d "$SCRIPT_DIR/out" ] && REBUILD=true

if [ "$REBUILD" = false ] && find "$SCRIPT_DIR/modules" -name "*.java" -newer "$SCRIPT_DIR/out" | grep -q .; then
    REBUILD=true
fi

if [ "$REBUILD" = true ]; then
    info "Compiling JPMS modules (one moment)..."
    rm -rf "$SCRIPT_DIR/out"
    mkdir -p "$SCRIPT_DIR/out"

    # Collect all .java files safely
    mapfile -d '' SRC < <(find "$SCRIPT_DIR/modules" -name "*.java" -print0)

    "$JAVAC_CMD" \
        -d "$SCRIPT_DIR/out" \
        --module-source-path "$SCRIPT_DIR/modules" \
        "${SRC[@]}" \
        || { err "Compilation failed — fix the errors above and double-click again."; exit 1; }
    ok "Compilation complete"
else
    ok "Build is up-to-date — skipping compilation"
fi

# ════════════════════════════════════════════════════════════════
# LAUNCH
# ════════════════════════════════════════════════════════════════
echo
echo -e "${BOLD}${GREEN}  All systems go — launching SkillSwap!${NC}"
echo
echo -e "  ${CYAN}Demo credentials (password: pass123)${NC}"
echo -e "  Student : ps0612@srmist.edu.in"
echo -e "  Admin   : admin@srmist.edu.in   (password: admin123)"
echo

# Remove the EXIT trap so the window closes cleanly when the app exits normally
trap - EXIT

exec "$JAVA_CMD" \
    --module-path "$SCRIPT_DIR/out:$SCRIPT_DIR/lib" \
    -m skillswap.gui/com.skillswap.gui.MainApp
