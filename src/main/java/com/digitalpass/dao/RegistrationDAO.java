package com.digitalpass.dao;

import com.digitalpass.model.Registration;
import com.digitalpass.util.AppException;
import com.digitalpass.util.PassCodeGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for Registrations and Digital Passes.
 *
 * Implements strict transaction management:
 * - One transaction per registration operation.
 * - Row-level locking on event via SELECT ... FOR UPDATE.
 * - Comprehensive capacity, participation type, duplicate PRN, and date validations.
 * - Automatic pass code generation with collision retry.
 * - Roll back on any failure and restore autoCommit.
 */
public class RegistrationDAO {

    private static final String BASE_REG_SELECT =
        "SELECT r.reg_id, r.event_id, r.team_id, r.name, r.phone, r.prn, r.email, "
      + "       r.member_role, r.pass_code, r.status, r.registered_at, r.checked_in_at, "
      + "       e.event_name, e.location, e.event_date, t.team_name "
      + "FROM registrations r "
      + "JOIN events e ON r.event_id = e.event_id "
      + "LEFT JOIN teams t ON r.team_id = t.team_id ";

    /**
     * Registers a SOLO attendee in a single atomic transaction with SELECT ... FOR UPDATE.
     *
     * @param eventId the event ID
     * @param person the Registration object containing solo attendee details
     * @return the saved Registration with assigned ID and unique pass_code
     * @throws AppException if validation fails or event is not eligible
     */
    public Registration registerSolo(int eventId, Registration person) throws AppException {
        if (person == null) {
            throw new AppException("Registration data cannot be null.");
        }
        if (person.getName() == null || person.getName().trim().isEmpty()) {
            throw new AppException("Name is required.");
        }
        if (person.getPrn() == null || person.getPrn().trim().isEmpty()) {
            throw new AppException("PRN is required.");
        }
        if (person.getEmail() == null || person.getEmail().trim().isEmpty()) {
            throw new AppException("Email is required.");
        }
        if (person.getPhone() == null || person.getPhone().trim().isEmpty()) {
            throw new AppException("Phone number is required for solo registration.");
        }

        String cleanPrn = person.getPrn().trim().toUpperCase();

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Lock event row with SELECT ... FOR UPDATE
            String lockEventSql = "SELECT event_id, event_name, event_date, capacity, participation_type, status "
                                + "FROM events WHERE event_id = ? FOR UPDATE";

            Timestamp eventDate;
            int capacity;
            String participationType;
            String status;

            try (PreparedStatement stmt = conn.prepareStatement(lockEventSql)) {
                stmt.setInt(1, eventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new AppException("Event not found.");
                    }
                    eventDate = rs.getTimestamp("event_date");
                    capacity = rs.getInt("capacity");
                    participationType = rs.getString("participation_type");
                    status = rs.getString("status");
                }
            }

            // 2. Validate event status, date, and participation type
            if (!"OPEN".equalsIgnoreCase(status)) {
                throw new AppException("Registration for this event is closed.");
            }
            if (eventDate != null && eventDate.before(new java.util.Date())) {
                throw new AppException("This event has already taken place.");
            }
            if ("TEAM".equalsIgnoreCase(participationType)) {
                throw new AppException("This event requires team participation.");
            }

            // 3. Check remaining capacity
            int currentCount = getEventRegisteredCount(conn, eventId);
            if (currentCount >= capacity) {
                throw new AppException("Event is full.");
            }

            // 4. Check if PRN is already registered for this event
            if (isPrnRegisteredInTx(conn, eventId, cleanPrn)) {
                throw new AppException("PRN " + cleanPrn + " is already registered for this event.");
            }

            // 5. Generate unique pass code with clash retry
            String passCode = generateUniquePassCode(conn);

            // 6. Insert solo registration
            String insertSql = "INSERT INTO registrations (event_id, team_id, name, phone, prn, email, "
                             + "                           member_role, pass_code, status, registered_at) "
                             + "VALUES (?, NULL, ?, ?, ?, ?, 'SOLO', ?, 'REGISTERED', NOW())";

            int regId;
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, eventId);
                stmt.setString(2, person.getName().trim());
                stmt.setString(3, person.getPhone().trim());
                stmt.setString(4, cleanPrn);
                stmt.setString(5, person.getEmail().trim().toLowerCase());
                stmt.setString(6, passCode);

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        regId = rs.getInt(1);
                    } else {
                        throw new AppException("Failed to assign registration ID.");
                    }
                }
            }

            // 7. Commit transaction
            conn.commit();

            return getByIdWithConnection(conn, regId);

        } catch (AppException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new AppException("Database transaction error: " + e.getMessage(), e);
        } finally {
            cleanupConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Registers a complete Team in a single atomic transaction with SELECT ... FOR UPDATE.
     *
     * @param eventId the event ID
     * @param teamName the team name
     * @param leader the leader Registration (must contain name, phone, prn, email)
     * @param members list of team member Registrations (name, prn, email; phone is optional/null)
     * @return List of saved Registrations (leader first, followed by members)
     * @throws AppException if validation fails or event constraints are violated
     */
    public List<Registration> registerTeam(int eventId, String teamName,
                                           Registration leader, List<Registration> members) throws AppException {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new AppException("Team name cannot be empty.");
        }
        if (leader == null) {
            throw new AppException("Team leader details are required.");
        }
        if (leader.getName() == null || leader.getName().trim().isEmpty() ||
            leader.getPrn() == null || leader.getPrn().trim().isEmpty() ||
            leader.getEmail() == null || leader.getEmail().trim().isEmpty() ||
            leader.getPhone() == null || leader.getPhone().trim().isEmpty()) {
            throw new AppException("Team leader must provide Name, Phone, PRN, and Email.");
        }

        int memberCount = (members != null) ? members.size() : 0;
        int totalTeamSize = 1 + memberCount;

        // Verify distinct PRNs inside the team
        Set<String> prnSet = new HashSet<>();
        String leaderPrn = leader.getPrn().trim().toUpperCase();
        prnSet.add(leaderPrn);

        if (members != null) {
            for (Registration m : members) {
                if (m == null || m.getName() == null || m.getName().trim().isEmpty() ||
                    m.getPrn() == null || m.getPrn().trim().isEmpty() ||
                    m.getEmail() == null || m.getEmail().trim().isEmpty()) {
                    throw new AppException("All team members must have Name, PRN, and Email provided.");
                }
                String cleanMPrn = m.getPrn().trim().toUpperCase();
                if (!prnSet.add(cleanMPrn)) {
                    throw new AppException("PRN " + cleanMPrn + " appears more than once in the team.");
                }
            }
        }

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DBConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Lock event row with SELECT ... FOR UPDATE
            String lockEventSql = "SELECT event_id, event_name, event_date, capacity, participation_type, "
                                + "       min_team_size, max_team_size, status "
                                + "FROM events WHERE event_id = ? FOR UPDATE";

            Timestamp eventDate;
            int capacity;
            String participationType;
            int minTeamSize;
            int maxTeamSize;
            String status;

            try (PreparedStatement stmt = conn.prepareStatement(lockEventSql)) {
                stmt.setInt(1, eventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new AppException("Event not found.");
                    }
                    eventDate = rs.getTimestamp("event_date");
                    capacity = rs.getInt("capacity");
                    participationType = rs.getString("participation_type");
                    minTeamSize = rs.getInt("min_team_size");
                    maxTeamSize = rs.getInt("max_team_size");
                    status = rs.getString("status");
                }
            }

            // 2. Validate event status and date
            if (!"OPEN".equalsIgnoreCase(status)) {
                throw new AppException("Registration for this event is closed.");
            }
            if (eventDate != null && eventDate.before(new java.util.Date())) {
                throw new AppException("This event has already taken place.");
            }

            // 3. Validate participation type (must NOT be INDIVIDUAL)
            if ("INDIVIDUAL".equalsIgnoreCase(participationType)) {
                throw new AppException("This event does not allow team registration.");
            }

            // 4. Validate team size bounds
            if (totalTeamSize < minTeamSize || totalTeamSize > maxTeamSize) {
                throw new AppException("Team size must be between " + minTeamSize + " and " + maxTeamSize + " members.");
            }

            // 5. Validate capacity fits the whole team
            int currentCount = getEventRegisteredCount(conn, eventId);
            if (currentCount + totalTeamSize > capacity) {
                throw new AppException("Not enough capacity remaining for this team.");
            }

            // 6. Check that no PRN in team is already registered for this event
            for (String prn : prnSet) {
                if (isPrnRegisteredInTx(conn, eventId, prn)) {
                    throw new AppException("PRN " + prn + " is already registered for this event.");
                }
            }

            // 7. Insert Team
            String insertTeamSql = "INSERT INTO teams (event_id, team_name, created_at) VALUES (?, ?, NOW())";
            int teamId;
            try (PreparedStatement stmt = conn.prepareStatement(insertTeamSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, eventId);
                stmt.setString(2, teamName.trim());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        teamId = rs.getInt(1);
                    } else {
                        throw new AppException("Failed to assign team ID.");
                    }
                }
            }

            List<Integer> savedIds = new ArrayList<>();

            // 8. Insert Leader
            String insertLeaderSql = "INSERT INTO registrations (event_id, team_id, name, phone, prn, email, "
                                   + "                           member_role, pass_code, status, registered_at) "
                                   + "VALUES (?, ?, ?, ?, ?, ?, 'LEADER', ?, 'REGISTERED', NOW())";

            String leaderPassCode = generateUniquePassCode(conn);
            try (PreparedStatement stmt = conn.prepareStatement(insertLeaderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, eventId);
                stmt.setInt(2, teamId);
                stmt.setString(3, leader.getName().trim());
                stmt.setString(4, leader.getPhone().trim());
                stmt.setString(5, leaderPrn);
                stmt.setString(6, leader.getEmail().trim().toLowerCase());
                stmt.setString(7, leaderPassCode);

                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        savedIds.add(rs.getInt(1));
                    }
                }
            }

            // 9. Insert Members (Phone is nullable)
            if (members != null) {
                String insertMemberSql = "INSERT INTO registrations (event_id, team_id, name, phone, prn, email, "
                                       + "                           member_role, pass_code, status, registered_at) "
                                       + "VALUES (?, ?, ?, ?, ?, ?, 'MEMBER', ?, 'REGISTERED', NOW())";

                for (Registration m : members) {
                    String memberPassCode = generateUniquePassCode(conn);
                    try (PreparedStatement stmt = conn.prepareStatement(insertMemberSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setInt(1, eventId);
                        stmt.setInt(2, teamId);
                        stmt.setString(3, m.getName().trim());

                        // Phone is nullable for team members
                        if (m.getPhone() != null && !m.getPhone().trim().isEmpty()) {
                            stmt.setString(4, m.getPhone().trim());
                        } else {
                            stmt.setNull(4, java.sql.Types.VARCHAR);
                        }

                        stmt.setString(5, m.getPrn().trim().toUpperCase());
                        stmt.setString(6, m.getEmail().trim().toLowerCase());
                        stmt.setString(7, memberPassCode);

                        stmt.executeUpdate();
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                savedIds.add(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            // 10. Commit transaction
            conn.commit();

            // 11. Retrieve saved registrations (Leader first)
            List<Registration> resultList = new ArrayList<>();
            for (int id : savedIds) {
                Registration r = getByIdWithConnection(conn, id);
                if (r != null) {
                    resultList.add(r);
                }
            }
            return resultList;

        } catch (AppException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new AppException("Database transaction error: " + e.getMessage(), e);
        } finally {
            cleanupConnection(conn, originalAutoCommit);
        }
    }

    /**
     * Searches registrations where pass_code, prn, or email exactly equals text (trimmed, case-insensitive).
     * If a matched person is a LEADER, also includes the other members of that team.
     *
     * @param text the exact search query
     * @return List of matching Registrations with event and team details
     * @throws AppException if database error occurs
     */
    public List<Registration> search(String text) throws AppException {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String clean = text.trim();
        List<Registration> results = new ArrayList<>();
        Set<Integer> visitedRegIds = new HashSet<>();
        Set<Integer> leaderTeamIds = new HashSet<>();

        String sql = BASE_REG_SELECT
                   + "WHERE LOWER(r.pass_code) = ? OR UPPER(r.prn) = ? OR LOWER(r.email) = ? "
                   + "ORDER BY r.registered_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clean.toLowerCase());
            stmt.setString(2, clean.toUpperCase());
            stmt.setString(3, clean.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Registration reg = mapRegistration(rs);
                    if (visitedRegIds.add(reg.getId())) {
                        results.add(reg);
                        if ("LEADER".equalsIgnoreCase(reg.getMemberRole()) && reg.getTeamId() != null) {
                            leaderTeamIds.add(reg.getTeamId());
                        }
                    }
                }
            }

            // If a matched person is a LEADER, also include the other members of that team
            for (Integer teamId : leaderTeamIds) {
                List<Registration> teamMembers = getTeamMembersWithConnection(conn, teamId);
                for (Registration member : teamMembers) {
                    if (visitedRegIds.add(member.getId())) {
                        results.add(member);
                    }
                }
            }

            return results;

        } catch (SQLException e) {
            throw new AppException("Database error during search: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all registrations for an event, ordered by team then role (LEADER first).
     */
    public List<Registration> getByEvent(int eventId) throws AppException {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_REG_SELECT
                   + "WHERE r.event_id = ? AND r.status != 'CANCELLED' "
                   + "ORDER BY COALESCE(r.team_id, 0), "
                   + "         CASE WHEN r.member_role = 'LEADER' THEN 1 WHEN r.member_role = 'SOLO' THEN 2 ELSE 3 END, "
                   + "         r.reg_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRegistration(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new AppException("Database error fetching event registrations: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all members of a team (LEADER first).
     */
    public List<Registration> getTeamMembers(int teamId) throws AppException {
        try (Connection conn = DBConnection.getConnection()) {
            return getTeamMembersWithConnection(conn, teamId);
        } catch (SQLException e) {
            throw new AppException("Database error fetching team members: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels a registration.
     */
    public void cancel(int regId) throws AppException {
        String sql = "UPDATE registrations SET status = 'CANCELLED' WHERE reg_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, regId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new AppException("Registration not found with ID: " + regId);
            }

        } catch (SQLException e) {
            throw new AppException("Database error cancelling registration: " + e.getMessage(), e);
        }
    }

    /**
     * Checks in a registration at the gate.
     */
    public void checkIn(int regId) throws AppException {
        String sql = "UPDATE registrations SET status = 'CHECKED_IN', checked_in_at = NOW() WHERE reg_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, regId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new AppException("Registration not found with ID: " + regId);
            }

        } catch (SQLException e) {
            throw new AppException("Database error checking in registration: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a registration by its pass code (case-insensitive trim).
     */
    public Registration findByPassCode(String code) throws AppException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String sql = BASE_REG_SELECT + "WHERE LOWER(r.pass_code) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRegistration(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new AppException("Database error finding pass code: " + e.getMessage(), e);
        }
    }

    /**
     * Counts all active registrations (status != 'CANCELLED').
     */
    public int countAll() throws AppException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE status != 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new AppException("Database error counting registrations: " + e.getMessage(), e);
        }
    }

    /**
     * Counts all checked-in registrations.
     */
    public int countCheckedIn() throws AppException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE status = 'CHECKED_IN'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new AppException("Database error counting checked-in registrations: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // INTERNAL HELPER METHODS
    // =========================================================================

    private int getEventRegisteredCount(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private boolean isPrnRegisteredInTx(Connection conn, int eventId, String prn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND prn = ? AND status != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setString(2, prn.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String generateUniquePassCode(Connection conn) throws SQLException, AppException {
        for (int i = 0; i < 10; i++) {
            String code = PassCodeGenerator.generate();
            String checkSql = "SELECT COUNT(*) FROM registrations WHERE pass_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setString(1, code);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return code;
                    }
                }
            }
        }
        throw new AppException("Failed to generate a unique pass code. Please try again.");
    }

    private Registration getByIdWithConnection(Connection conn, int regId) throws SQLException {
        String sql = BASE_REG_SELECT + "WHERE r.reg_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, regId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRegistration(rs) : null;
            }
        }
    }

    private List<Registration> getTeamMembersWithConnection(Connection conn, int teamId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_REG_SELECT
                   + "WHERE r.team_id = ? "
                   + "ORDER BY CASE WHEN r.member_role = 'LEADER' THEN 1 ELSE 2 END, r.reg_id ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRegistration(rs));
                }
            }
        }
        return list;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void cleanupConnection(Connection conn, boolean originalAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {
            }
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private Registration mapRegistration(ResultSet rs) throws SQLException {
        Registration r = new Registration(
            rs.getInt("reg_id"),
            rs.getInt("event_id"),
            rs.getObject("team_id") != null ? rs.getInt("team_id") : null,
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("prn"),
            rs.getString("email"),
            rs.getString("member_role"),
            rs.getString("pass_code"),
            rs.getString("status"),
            rs.getTimestamp("registered_at"),
            rs.getTimestamp("checked_in_at")
        );
        r.setEventName(rs.getString("event_name"));
        r.setLocation(rs.getString("location"));
        r.setEventDate(rs.getTimestamp("event_date"));
        r.setTeamName(rs.getString("team_name"));
        return r;
    }
}
