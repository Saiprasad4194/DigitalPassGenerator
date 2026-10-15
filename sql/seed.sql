-- ============================================================================
-- DIGITAL PASS GENERATOR - SAMPLE SEED DATA
-- Database: digital_pass_db
-- ============================================================================
-- NOTE ON ADMIN CREDENTIALS:
--   Username: admin
--   Plaintext Password: "admin123"
--   SHA-256 Hash: "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"
--   Computed using PasswordUtil.hash("admin123")
-- ============================================================================

USE digital_pass_db;

-- ----------------------------------------------------------------------------
-- 1. SEED ADMIN
-- ----------------------------------------------------------------------------
INSERT INTO admins (admin_id, username, password_hash) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- ----------------------------------------------------------------------------
-- 2. SEED EVENTS
-- Event 1: INDIVIDUAL only
-- Event 2: TEAM only (team size 2 to 4)
-- Event 3: BOTH individual and team allowed (team size 2 to 3)
-- ----------------------------------------------------------------------------
INSERT INTO events (event_id, event_name, description, event_date, location, capacity, participation_type, min_team_size, max_team_size, status) VALUES
(1, 'Coding Clash 2026',
 'Solo competitive programming contest testing algorithms, speed, and data structures.',
 '2026-11-10 10:00:00', 'Computer Lab 3, IT Block', 80, 'INDIVIDUAL', 1, 1, 'OPEN'),

(2, 'HackNova 24-Hour Hackathon',
 'Premier 24-hour team hackathon to build full-stack solutions for real-world campus problems.',
 '2026-11-20 09:00:00', 'Main Seminar Hall & Tech Lounge', 120, 'TEAM', 2, 4, 'OPEN'),

(3, 'RoboWars Arena',
 'Combat robotics challenge open for solo bots or small teams of up to 3 engineers.',
 '2026-11-28 14:00:00', 'Campus Open Air Amphitheatre', 60, 'BOTH', 2, 3, 'OPEN');

-- ----------------------------------------------------------------------------
-- 3. SEED TEAMS
-- One sample team for Event 2 (HackNova)
-- ----------------------------------------------------------------------------
INSERT INTO teams (team_id, event_id, team_name) VALUES
(1, 2, 'CodeCommanders');

-- ----------------------------------------------------------------------------
-- 4. SEED REGISTRATIONS
-- Includes 1 team (1 LEADER + 2 MEMBERS) and 2 SOLO participants
-- ----------------------------------------------------------------------------
INSERT INTO registrations (reg_id, event_id, team_id, name, phone, prn, email, member_role, pass_code, status, registered_at, checked_in_at) VALUES
-- Team "CodeCommanders" for HackNova (Event 2)
(1, 2, 1, 'Aarav Sharma', '9876543210', 'PRN2024001', 'aarav@student.edu', 'LEADER', 'DP-8K3M2QX7', 'REGISTERED', NOW(), NULL),
(2, 2, 1, 'Diya Patel',   NULL,         'PRN2024002', 'diya@student.edu',  'MEMBER', 'DP-9V4N1TY2', 'REGISTERED', NOW(), NULL),
(3, 2, 1, 'Rohan Verma',  NULL,         'PRN2024003', 'rohan@student.edu', 'MEMBER', 'DP-7X2W5PL9', 'REGISTERED', NOW(), NULL),

-- Solo participant for Coding Clash (Event 1)
(4, 1, NULL, 'Neha Gupta', '9876543213', 'PRN2024004', 'neha@student.edu', 'SOLO', 'DP-4M8Q7KJ3', 'REGISTERED', NOW(), NULL),

-- Solo participant for RoboWars (Event 3), already checked in
(5, 3, NULL, 'Vikram Joshi', '9876543214', 'PRN2024005', 'vikram@student.edu', 'SOLO', 'DP-2H6R9TB1', 'CHECKED_IN', NOW(), '2026-11-28 14:10:00');

-- ----------------------------------------------------------------------------
-- 5. SEED SCAN LOGS
-- ----------------------------------------------------------------------------
INSERT INTO scan_logs (log_id, reg_id, attempted_code, event_id, scanned_by, scan_time, result) VALUES
(1, 5, 'DP-2H6R9TB1', 3, 1, '2026-11-28 14:10:00', 'VALID');
