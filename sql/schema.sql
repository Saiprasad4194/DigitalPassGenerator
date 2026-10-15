-- ============================================================================
-- DIGITAL PASS GENERATOR - REDESIGNED DATABASE SCHEMA
-- Target DB: digital_pass_db
-- Roles:
--   1. ADMIN (logs in, manages events, scans passes at gates)
--   2. USER  (public student/visitor, no login, registers for solo or team passes)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS digital_pass_db;
USE digital_pass_db;

-- ----------------------------------------------------------------------------
-- Drop all existing tables (new and legacy) in reverse dependency order
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS scan_logs;
DROP TABLE IF EXISTS verification_logs;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS passes;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS users;

-- ----------------------------------------------------------------------------
-- 1. ADMINS TABLE
-- Authorized staff accounts who log in to verify passes and manage events.
-- ----------------------------------------------------------------------------
CREATE TABLE admins (
    -- PRIMARY KEY: Uniquely identifies each admin
    admin_id INT AUTO_INCREMENT PRIMARY KEY,

    -- UNIQUE: No two admins can share the same username
    username VARCHAR(50) NOT NULL UNIQUE,

    -- NOT NULL: Secure 64-character SHA-256 hexadecimal hash
    password_hash CHAR(64) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 2. EVENTS TABLE
-- Campus events with capacity limits and participation rules.
-- ----------------------------------------------------------------------------
CREATE TABLE events (
    -- PRIMARY KEY: Uniquely identifies each event
    event_id INT AUTO_INCREMENT PRIMARY KEY,

    -- NOT NULL: Event title is mandatory
    event_name VARCHAR(150) NOT NULL,

    -- Event details and rules
    description TEXT,

    -- NOT NULL: Scheduled date and time
    event_date DATETIME NOT NULL,

    -- Campus venue/hall
    location VARCHAR(200),

    -- NOT NULL: Maximum total allowed participants (solo + team members)
    capacity INT NOT NULL,

    -- Participation type: INDIVIDUAL only, TEAM only, or BOTH allowed
    participation_type ENUM('INDIVIDUAL', 'TEAM', 'BOTH') NOT NULL DEFAULT 'INDIVIDUAL',

    -- Team size rules (includes the leader):
    -- For INDIVIDUAL events: min=1, max=1
    -- For TEAM events: e.g. min=2, max=4
    min_team_size INT DEFAULT 1,
    max_team_size INT DEFAULT 1,

    -- Event status: OPEN for registration or CLOSED
    status ENUM('OPEN', 'CLOSED') DEFAULT 'OPEN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 3. TEAMS TABLE
-- Represents a group created for team-based events.
-- ----------------------------------------------------------------------------
CREATE TABLE teams (
    -- PRIMARY KEY: Uniquely identifies each team
    team_id INT AUTO_INCREMENT PRIMARY KEY,

    -- FOREIGN KEY: References events(event_id)
    event_id INT NOT NULL,

    -- Name of the student team
    team_name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- If event is deleted, delete associated teams
    CONSTRAINT fk_teams_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- 4. REGISTRATIONS TABLE
-- Every issued digital pass corresponds to one registration row.
-- Supports solo attendees, team leaders, and team members.
-- ----------------------------------------------------------------------------
CREATE TABLE registrations (
    -- PRIMARY KEY: Uniquely identifies each participant registration
    reg_id INT AUTO_INCREMENT PRIMARY KEY,

    -- FOREIGN KEY: References events(event_id)
    event_id INT NOT NULL,

    -- FOREIGN KEY: References teams(team_id) (NULL for SOLO attendees)
    team_id INT NULL,

    -- Attendee personal info (Phone is required for SOLO and LEADER, NULL for team members)
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,

    -- College PRN / Roll Number (Permanent Registration Number)
    prn VARCHAR(30) NOT NULL,
    email VARCHAR(150) NOT NULL,

    -- Role within the registration:
    -- SOLO   : Individual participant
    -- LEADER : Created the team
    -- MEMBER : Joined an existing team
    member_role ENUM('SOLO', 'LEADER', 'MEMBER') NOT NULL,

    -- UNIQUE: Unique digital pass code scanned at gates (e.g., DP-8K3M2QX7)
    pass_code VARCHAR(30) NOT NULL UNIQUE,

    -- Pass lifecycle status
    status ENUM('REGISTERED', 'CHECKED_IN', 'CANCELLED') DEFAULT 'REGISTERED',

    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_in_at DATETIME NULL,

    -- CONSTRAINT: One PRN can register only once per event (prevents duplicates)
    CONSTRAINT unique_event_prn UNIQUE (event_id, prn),

    -- FOREIGN KEYS
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_team FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes on frequently queried fields
CREATE INDEX idx_reg_pass_code ON registrations(pass_code);
CREATE INDEX idx_reg_prn ON registrations(prn);
CREATE INDEX idx_reg_event_id ON registrations(event_id);

-- ----------------------------------------------------------------------------
-- 5. SCAN_LOGS TABLE
-- Audit log of gate scanning attempts by admins.
-- ----------------------------------------------------------------------------
CREATE TABLE scan_logs (
    -- PRIMARY KEY: Uniquely identifies each scan attempt
    log_id INT AUTO_INCREMENT PRIMARY KEY,

    -- FOREIGN KEY: References registrations(reg_id) (NULL if code invalid)
    reg_id INT NULL,

    -- Raw pass code entered/scanned at terminal
    attempted_code VARCHAR(30) NOT NULL,

    -- Event ID context (nullable)
    event_id INT NULL,

    -- FOREIGN KEY: References admins(admin_id) who performed the scan
    scanned_by INT NOT NULL,

    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Outcome result: e.g. 'VALID', 'ALREADY_CHECKED_IN', 'INVALID_CODE', 'WRONG_EVENT'
    result VARCHAR(30) NOT NULL,

    CONSTRAINT fk_scan_reg FOREIGN KEY (reg_id) REFERENCES registrations(reg_id) ON DELETE SET NULL,
    CONSTRAINT fk_scan_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE SET NULL,
    CONSTRAINT fk_scan_admin FOREIGN KEY (scanned_by) REFERENCES admins(admin_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
