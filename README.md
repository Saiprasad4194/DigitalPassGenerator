# Digital Pass Generator

A lightweight, beginner-friendly Java EE web application for campus event pass generation and verification.
Built using: **Servlets (4.0)**, **JSP**, **JSTL 1.2**, **plain JDBC**, and **MySQL 8** without heavyweight frameworks.

---

## 👥 User Roles & Access Model

1. **PUBLIC USER**: Any student or visitor. **No login required.**
   - Browses active campus events.
   - Registers for **SOLO** or **TEAM** passes using their Name, PRN (Roll Number), Email, and Phone.
   - Instantly generates and prints an official Digital Pass with a unique code (e.g. `DP-8K3M2QX7`).
   - Retrieves previously registered passes by PRN, email, or pass code.

2. **ADMIN**: Authorized staff. **Logs in with credentials.**
   - Scans and checks in attendees at venue gates using the gate terminal.
   - Verifies pass codes in real time, preventing duplicate entry.
   - Manages events and participant quotas.

---

## 🗄️ Database Architecture (`digital_pass_db`)

| Table | Purpose | Key Constraints |
| :--- | :--- | :--- |
| **`admins`** | Admin staff login accounts | `admin_id` (PK AI), `username` (UNIQUE), `password_hash` (SHA-256) |
| **`events`** | Campus events with rules & limits | `event_id` (PK AI), `event_name`, `capacity`, `participation_type` (`INDIVIDUAL`, `TEAM`, `BOTH`), `min_team_size`, `max_team_size`, `status` (`OPEN`, `CLOSED`) |
| **`teams`** | Student teams formed for events | `team_id` (PK AI), `event_id` (FK -> `events`), `team_name` |
| **`registrations`** | Issued passes for participants | `reg_id` (PK AI), `event_id` (FK), `team_id` (FK nullable), `prn` (College Roll), `member_role` (`SOLO`, `LEADER`, `MEMBER`), `pass_code` (UNIQUE), `status` (`REGISTERED`, `CHECKED_IN`, `CANCELLED`), `UNIQUE(event_id, prn)` |
| **`scan_logs`** | Gate audit verification history | `log_id` (PK AI), `reg_id` (FK nullable), `attempted_code`, `scanned_by` (FK -> `admins`), `result` |

Indexes created: `registrations.pass_code`, `registrations.prn`, `registrations.event_id`.

---

## 🚀 How to Re-Create the Database from Scratch

### Option A: Via Command Line Terminal (Fastest)
Run the two SQL scripts using your MySQL root password:
```bash
# 1. Drop existing tables and recreate the redesigned schema
mysql -u root -p < sql/schema.sql

# 2. Insert admin account, 3 events (Solo, Team, Both), and sample team passes
mysql -u root -p < sql/seed.sql
```

### Option B: Via MySQL Workbench
1. Open **MySQL Workbench** and connect to your local MySQL instance.
2. Click **File** $\rightarrow$ **Open SQL Script...** (`Cmd + O` or `Ctrl + O`).
3. Select `sql/schema.sql` and click the **Lightning Bolt ⚡** icon to execute.
4. Open `sql/seed.sql` and click the **Lightning Bolt ⚡** icon to execute.
5. In the left **Schemas** panel, click the refresh 🔄 button to verify all 5 tables under `digital_pass_db`.

---

## 🔑 Default Credentials & Seed Data

### Admin Login
* **Username**: `admin`
* **Plaintext Password**: `admin123`
* **SHA-256 Hash**: `240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9`

### Seeded Events
1. **Coding Clash 2026** &mdash; `INDIVIDUAL` (Solo only, capacity: 80)
2. **HackNova 24-Hour Hackathon** &mdash; `TEAM` (Team size 2 to 4, capacity: 120)
3. **RoboWars Arena** &mdash; `BOTH` (Solo or team size 2 to 3, capacity: 60)

### Seeded Registrations
* Team **CodeCommanders** for HackNova:
  * Aarav Sharma (`PRN2024001`, `LEADER`, Pass Code: `DP-8K3M2QX7`)
  * Diya Patel (`PRN2024002`, `MEMBER`, Pass Code: `DP-9V4N1TY2`)
  * Rohan Verma (`PRN2024003`, `MEMBER`, Pass Code: `DP-7X2W5PL9`)
* Solo Participant for Coding Clash:
  * Neha Gupta (`PRN2024004`, `SOLO`, Pass Code: `DP-4M8Q7KJ3`)
* Solo Participant for RoboWars (Checked in):
  * Vikram Joshi (`PRN2024005`, `SOLO`, Pass Code: `DP-2H6R9TB1`, Status: `CHECKED_IN`)

---

## 💻 Running the Web Application

### 1. Compile the Source
```bash
mvn clean compile
```

### 2. Start with Embedded Jetty (No Tomcat Required)
```bash
mvn jetty:run
```

### 3. Open in Browser
Visit **http://localhost:8080/** to access the public registration portal.
To scan passes, visit **http://localhost:8080/verify-pass**.
