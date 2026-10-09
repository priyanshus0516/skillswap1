#!/bin/bash
# ==============================================================
#  SkillSwap - Fully Automated Launcher
#  Just run:  bash launch.sh
#  Everything (MySQL, DB setup, compile, run) is done for you.
# ==============================================================

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'  GREEN='\033[0;32m'  YELLOW='\033[1;33m'
CYAN='\033[0;36m' BOLD='\033[1m'      NC='\033[0m'

info()    { echo -e "${CYAN}[SkillSwap]${NC} $*"; }
ok()      { echo -e "${GREEN}  [OK]${NC} $*"; }
warn()    { echo -e "${YELLOW}  [!] $*${NC}"; }
err()     { echo -e "${RED}  [ERR] $*${NC}" >&2; }
banner()  { echo -e "\n${BOLD}${CYAN}>>> $* <<<${NC}\n"; }

# ── Read storage.properties ──────────────────────────────────
prop() { grep -E "^${1}=" "$SCRIPT_DIR/storage.properties" | head -1 | cut -d= -f2- | xargs; }
DB_USER="$(prop db.user)"
DB_PASS="$(prop db.password)"
DB_URL="$(prop db.url)"
DB_HOST=$(echo "$DB_URL" | sed -E 's|.*://([^:/]+).*|\1|')
DB_PORT=$(echo "$DB_URL" | sed -E 's|.*:([0-9]+)/.*|\1|')
DB_NAME=$(echo "$DB_URL" | sed -E 's|.*/([^?]+).*|\1|')

banner "SkillSwap Auto-Launcher"

# ── Step 1: Java ─────────────────────────────────────────────
info "Step 1/4 — Checking Java..."
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
    warn "Java not found — trying Homebrew install..."
    command -v brew &>/dev/null || { err "Homebrew missing. Install Java 17+ from https://adoptium.net"; exit 1; }
    brew install --cask temurin@21
    JAVA_CMD="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java"
    JAVAC_CMD="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/javac"
fi
JAVA_VER=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/{print $2}' | cut -d. -f1)
[ "${JAVA_VER:-0}" -lt 11 ] 2>/dev/null && { err "Need Java 11+, found $JAVA_VER"; exit 1; }
ok "Java $JAVA_VER  ($JAVA_CMD)"

# ── Step 2: MySQL ─────────────────────────────────────────────
info "Step 2/4 — Checking MySQL on $DB_HOST:$DB_PORT..."
_mysql() {
    mysql --host="$DB_HOST" --port="$DB_PORT" \
          --user="$DB_USER" --password="$DB_PASS" \
          --connect-timeout=5 "$@" 2>/dev/null
}
_mysql_up() { _mysql -e "SELECT 1;" &>/dev/null; }

if ! _mysql_up; then
    warn "MySQL not running — trying to start it..."
    if command -v brew &>/dev/null; then
        SVC=$(brew services list 2>/dev/null | awk '/^mysql/{print $1; exit}')
        [ -n "$SVC" ] && { brew services start "$SVC" >/dev/null 2>&1 || true; sleep 5; }
    fi
    if ! _mysql_up && command -v mysql.server &>/dev/null; then
        mysql.server start >/dev/null 2>&1 || true; sleep 5
    fi
    _mysql_up || {
        err "Cannot reach MySQL at $DB_HOST:$DB_PORT"
        echo "  Run one of:  brew services start mysql   |   mysql.server start"
        exit 1
    }
fi
ok "MySQL is running"

# ── Step 3: DB Schema + Seed ──────────────────────────────────
info "Step 3/4 — Setting up database '$DB_NAME'..."
_mysql < "$SCRIPT_DIR/sql/schema.sql"  || { err "Schema setup failed — check credentials"; exit 1; }
ok "Schema applied"
_mysql "$DB_NAME" < "$SCRIPT_DIR/sql/sample_data.sql" || warn "Seed warnings (non-fatal — data may already exist)"
ok "Sample data seeded"

# ── Step 4: Compile (only if needed) ─────────────────────────
info "Step 4/4 — Checking if compilation is needed..."
REBUILD=false
[ ! -d "$SCRIPT_DIR/out" ] && REBUILD=true
if [ "$REBUILD" = false ] && find "$SCRIPT_DIR/modules" -name "*.java" -newer "$SCRIPT_DIR/out" | grep -q .; then
    REBUILD=true
fi

if [ "$REBUILD" = true ]; then
    info "  Compiling JPMS modules (this takes a few seconds)..."
    rm -rf "$SCRIPT_DIR/out"
    mapfile -d '' SRC < <(find "$SCRIPT_DIR/modules" -name "*.java" -print0)
    "$JAVAC_CMD" -d "$SCRIPT_DIR/out" \
        --module-source-path "$SCRIPT_DIR/modules" \
        "${SRC[@]}" || { err "Compilation failed — fix errors and re-run"; exit 1; }
    ok "Compilation complete"
else
    ok "Build is up-to-date — skipping compilation"
fi

# ── Launch! ───────────────────────────────────────────────────
banner "All systems go — launching SkillSwap!"
info "  Demo login (password: pass123)"
info "  Student  :  ps0612@srmist.edu.in"
info "  Admin    :  admin@srmist.edu.in   (password: admin123)"
echo

exec "$JAVA_CMD" \
    --module-path "$SCRIPT_DIR/out:$SCRIPT_DIR/lib" \
    -m skillswap.gui/com.skillswap.gui.MainApp
