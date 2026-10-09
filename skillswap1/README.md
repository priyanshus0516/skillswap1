# SkillSwap — Peer-to-Peer Skill Exchange Platform

A modular Java desktop application (Swing + JPMS + JDBC + MySQL) built for the college project brief.

SkillSwap is fully structured with **real Java Platform Module System (JPMS) modules**, a clean **Data Access Object (DAO) architecture** with both **In-Memory** (instant zero-setup demo) and **MySQL JDBC** implementations, and a dynamic **DAOFactory (Strategy pattern)** that switches persistence via configuration or system properties.

---

## 1. Modular Architecture (JPMS)

SkillSwap is partitioned into 6 distinct Java 9+ JPMS modules located in the `modules/` directory:

```
SkillSwap/
├── modules/
│   ├── skillswap.exception/     -> Custom checked exception hierarchy (SkillSwapException, DataAccessException, etc.)
│   ├── skillswap.util/          -> Utilities (SHA-256 PasswordUtil, persistent FileLogger)
│   ├── skillswap.model/         -> Domain entities (User, Student, Skill, ExchangeRequest, Session, Feedback, Ratable)
│   ├── skillswap.dao/           -> DAO Interfaces, DAOFactory, InMemory*DAO, and Jdbc*DAO
│   ├── skillswap.service/       -> Business services (Auth, Skill, Matching, Request, Session, Feedback)
│   └── skillswap.gui/           -> Swing desktop UI (Login, Register, Dashboard with 7 interactive tabs)
├── sql/
│   ├── schema.sql               -> MySQL tables (with rating_sum and rating_count columns)
│   └── sample_data.sql          -> Demo seed data for MySQL
├── storage.properties           -> Persistence configuration (inmemory vs jdbc)
└── README.md
```

### Module Dependency Graph

```
┌─────────────────┐
│  skillswap.gui  │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  skillswap.service  │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│    skillswap.dao    │
└────────┬────────────┘
         │
         ├───────────────────────────────┐
         ▼                               ▼
┌──────────────────┐            ┌─────────────────────┐
│ skillswap.model  │            │ skillswap.exception │
└──────────────────┘            └─────────────────────┘
         ▲                               ▲
         │                               │
         └───────── skillswap.util ──────┘
```

- **Clean Decoupling**: The GUI layer (`skillswap.gui`) only talks to the Service layer (`skillswap.service`).
- **Encapsulated Persistence**: Neither the GUI nor Service layer touches `java.sql`. All SQL errors are wrapped into `DataAccessException` in the DAO layer.
- **Strict Exports**: Each module exposes strictly what other modules require via `module-info.java`.

---

## 2. Dual Storage & DAOFactory Strategy

The DAO layer provides interfaces and two complete implementations:
- **Interfaces**: `UserDAO`, `SkillDAO`, `RequestDAO`, `SessionDAO`, `FeedbackDAO`.
- **In-Memory**: `InMemoryUserDAO`, `InMemorySkillDAO`, `InMemoryRequestDAO`, `InMemorySessionDAO`, `InMemoryFeedbackDAO` (seeded with standard SRM demo accounts).
- **MySQL JDBC**: `JdbcUserDAO`, `JdbcSkillDAO`, `JdbcRequestDAO`, `JdbcSessionDAO`, `JdbcFeedbackDAO` (connected via JDBC with prepared statements and transactions).

### Switching Between In-Memory and MySQL

`DAOFactory` selects the persistence provider at runtime:

1. **Via `storage.properties`** (in the application directory):
   ```properties
   # Options: 'inmemory' or 'jdbc'
   storage.type=inmemory

   # MySQL settings (used when storage.type=jdbc):
   db.url=jdbc:mysql://localhost:3306/skillswap_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=rootpassword
   db.driver=com.mysql.cj.jdbc.Driver
   ```

2. **Via JVM System Property**:
   - To force In-Memory: `-Dskillswap.storage=inmemory`
   - To force MySQL: `-Dskillswap.storage=jdbc`

---

## 3. How to Compile & Run

### Prerequisites
- JDK 11, 17, 21, or 25.
- (Optional, for MySQL mode): MySQL Server 8.x + MySQL Connector/J driver jar.

### Step 1: Compile all JPMS modules
From the root `SkillSwap/` directory:

```bash
# On Mac / Linux:
javac -d out --module-source-path modules $(find modules -name "*.java")

# On Windows (cmd):
javac -d out --module-source-path modules modules\skillswap.exception\module-info.java modules\skillswap.exception\com\skillswap\exception\*.java modules\skillswap.util\module-info.java modules\skillswap.util\com\skillswap\util\*.java modules\skillswap.model\module-info.java modules\skillswap.model\com\skillswap\model\*.java modules\skillswap.dao\module-info.java modules\skillswap.dao\com\skillswap\dao\*.java modules\skillswap.dao\com\skillswap\dao\inmemory\*.java modules\skillswap.dao\com\skillswap\dao\jdbc\*.java modules\skillswap.service\module-info.java modules\skillswap.service\com\skillswap\service\*.java modules\skillswap.gui\module-info.java modules\skillswap.gui\com\skillswap\gui\*.java
```

### Step 2: Run the Application

#### Option A: In-Memory Mode (Zero Setup Demo)
```bash
java --module-path out --module skillswap.gui/com.skillswap.gui.MainApp
```

#### Option B: MySQL Mode
1. Import `sql/schema.sql` and `sql/sample_data.sql` into MySQL:
   ```bash
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/sample_data.sql
   ```
2. Put `mysql-connector-j-9.x.x.jar` into a `lib/` folder (or your module path).
3. Set `storage.type=jdbc` in `storage.properties`.
4. Run:
   ```bash
   java --module-path out:lib --module skillswap.gui/com.skillswap.gui.MainApp
   ```

---

## 4. Demo Walkthrough & Seeded Accounts

### Demo Credentials (Password is `pass123` for all)
- `ps0612@srmist.edu.in` — **Priyanshu Sharma** (CSE CORE, Yr 2 | Teaches: Java Programming | Wants: Guitar)
- `ananya@srmist.edu.in` — **Ananya Rao** (CSE CORE, Yr 2 | Teaches: Guitar | Wants: Java Programming)
- `kabir@srmist.edu.in` — **Kabir Mehta** (ECE, Yr 3 | Teaches: Photoshop | Wants: Public Speaking)
- `diya@srmist.edu.in` — **Diya Nair** (IT, Yr 1 | Teaches: Public Speaking | Wants: Photoshop)

### Recommended Demo Flow:
1. **Login** as `ps0612@srmist.edu.in` / `pass123`.
2. Notice the interactive **Home** dashboard showing live statistics (skills owned, matches found, pending requests).
3. Go to **Discover Matches** tab: Ananya Rao is ranked at the top as a **MUTUAL match** (she teaches Guitar which you want; you teach Java which she wants).
4. Double-click her row (or click "Send exchange request to selected") and enter a note.
5. Log out and log in as `ananya@srmist.edu.in`.
6. Open **Exchange Requests** tab: double-click the incoming request and click **Accept**.
7. Go to **Sessions** tab: click **Schedule from an accepted request**, pick date/time and mode (Online/Offline).
8. Once session is held, click **Mark selected as completed**.
9. Go to **Feedback & Ratings** tab: click **Leave feedback for a completed session**, give a rating (1-5) and comment. Partner's average rating updates immediately!
10. Check `skillswap_activity.log` in the project root to view timestamped audit logs of all actions.

---

## 5. UI Features & Enhancements

| Feature | Where to test |
|---|---|
| Clickable Stat Cards | **Home** tab — click any stat card to jump directly to that tab |
| Interactive Visual Skill Cards | **My Skills** tab — cards lift on hover, display level meters, and support leveling up or removal |
| Profile & Skills Management | **My Profile** tab — add new skills to catalogue or teach/learn lists; double click to remove |
| Live Instant Search | **Discover Matches** — filters across all columns on keystrokes |
| Column Sorting | Every table — click header to sort ascending/descending |
| Double-Click Shortcuts | Double-click Discover row to send request; incoming row to Accept/Reject; session row for actions |
| Live Form Validation | **Create account** dialog — instant inline validation turning red/green on keystroke |

---

## 6. College Project Requirements Mapping

| Requirement | Implementation Detail |
|---|---|
| **Modular Programming (JPMS)** | 6 independent JPMS modules (`skillswap.exception`, `skillswap.util`, `skillswap.model`, `skillswap.dao`, `skillswap.service`, `skillswap.gui`), each with `module-info.java` |
| **Classes & Objects** | Entity models (`User`, `Student`, `Skill`, `ExchangeRequest`, `Session`, `Feedback`) |
| **Inheritance** | `Student extends User` |
| **Polymorphism** | Abstract methods `displayProfile()` and `getRole()` in `User`, overridden in `Student` |
| **Interfaces** | `Ratable` interface implemented by `Student`; DAO interfaces implemented by InMemory and JDBC classes |
| **Exception Handling** | Hierarchy of checked exceptions (`SkillSwapException`, `AuthenticationException`, `DuplicateUserException`, `InvalidRequestException`, `DataAccessException`) |
| **File Handling** | `FileLogger` writes audit log to `skillswap_activity.log`; `storage.properties` configuration file loading |
| **Collections Framework** | `ArrayList`, `LinkedHashMap`, `List`, `Map` used across DAO and service components |
| **GUI & Event-Driven** | Swing GUI (`JFrame`, `JPanel`, `JTable`, `JTabbedPane`, `DocumentListener`, `MouseAdapter`, lambda action listeners) |
| **Database & JDBC** | `Jdbc*DAO` classes, `sql/schema.sql`, `PreparedStatement`, transactional commits for ratings and feedback |
| **Design Patterns** | Strategy & Factory Method (`DAOFactory`), DAO Pattern, MVC Architecture |

## Setup notes

- Copy `storage.properties.example` to `storage.properties` and set your own MySQL password (or set `storage.type=inmemory` for the zero-setup demo).
- Download `mysql-connector-j-8.3.0.jar` (MySQL Connector/J 8.3.0) and place it in `lib/`.
