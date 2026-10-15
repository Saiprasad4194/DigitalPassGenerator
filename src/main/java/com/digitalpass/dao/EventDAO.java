package com.digitalpass.dao;

import com.digitalpass.model.Event;
import com.digitalpass.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Event management.
 * Plain JDBC, PreparedStatement only, try-with-resources.
 * Every Event includes live registeredCount (not CANCELLED) and teamCount.
 */
public class EventDAO {

    private static final String BASE_EVENT_SELECT =
        "SELECT e.event_id, e.event_name, e.description, e.event_date, e.location, e.capacity, "
      + "       e.participation_type, e.min_team_size, e.max_team_size, e.status, "
      + "       (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.event_id AND r.status != 'CANCELLED') AS reg_count, "
      + "       (SELECT COUNT(*) FROM teams t WHERE t.event_id = e.event_id) AS team_count "
      + "FROM events e ";

    /**
     * Adds a new event to the database.
     *
     * @param event the event to insert
     * @return the generated event ID
     * @throws AppException if insertion fails
     */
    public int addEvent(Event event) throws AppException {
        if (event == null) {
            throw new AppException("Event data cannot be null.");
        }

        String sql = "INSERT INTO events (event_name, description, event_date, location, capacity, "
                   + "                    participation_type, min_team_size, max_team_size, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getDescription());
            stmt.setTimestamp(3, event.getEventDate());
            stmt.setString(4, event.getLocation());
            stmt.setInt(5, event.getCapacity());
            stmt.setString(6, event.getParticipationType() != null ? event.getParticipationType() : "INDIVIDUAL");
            stmt.setInt(7, event.getMinTeamSize() > 0 ? event.getMinTeamSize() : 1);
            stmt.setInt(8, event.getMaxTeamSize() > 0 ? event.getMaxTeamSize() : 1);
            stmt.setString(9, event.getStatus() != null ? event.getStatus() : "OPEN");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    event.setId(id);
                    return id;
                }
            }
            throw new AppException("Failed to retrieve generated event ID.");

        } catch (SQLException e) {
            throw new AppException("Database error while adding event: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing event.
     *
     * @param event the event with updated values
     * @throws AppException if update fails
     */
    public void updateEvent(Event event) throws AppException {
        if (event == null || event.getId() <= 0) {
            throw new AppException("Invalid event ID for update.");
        }

        String sql = "UPDATE events SET event_name = ?, description = ?, event_date = ?, location = ?, "
                   + "                 capacity = ?, participation_type = ?, min_team_size = ?, "
                   + "                 max_team_size = ?, status = ? "
                   + "WHERE event_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getEventName());
            stmt.setString(2, event.getDescription());
            stmt.setTimestamp(3, event.getEventDate());
            stmt.setString(4, event.getLocation());
            stmt.setInt(5, event.getCapacity());
            stmt.setString(6, event.getParticipationType());
            stmt.setInt(7, event.getMinTeamSize());
            stmt.setInt(8, event.getMaxTeamSize());
            stmt.setString(9, event.getStatus());
            stmt.setInt(10, event.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new AppException("Event not found with ID: " + event.getId());
            }

        } catch (SQLException e) {
            throw new AppException("Database error while updating event: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an event by its ID.
     *
     * @param id the event ID
     * @throws AppException if deletion fails
     */
    public void deleteEvent(int id) throws AppException {
        String sql = "DELETE FROM events WHERE event_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new AppException("Event not found with ID: " + id);
            }

        } catch (SQLException e) {
            throw new AppException("Database error while deleting event: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all events along with live registeredCount and teamCount.
     */
    public List<Event> getAllEvents() throws AppException {
        List<Event> events = new ArrayList<>();
        String sql = BASE_EVENT_SELECT + "ORDER BY e.event_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }
            return events;

        } catch (SQLException e) {
            throw new AppException("Database error while fetching events: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all OPEN events with upcoming dates along with live registeredCount and teamCount.
     */
    public List<Event> getOpenEvents() throws AppException {
        List<Event> events = new ArrayList<>();
        String sql = BASE_EVENT_SELECT + "WHERE e.status = 'OPEN' ORDER BY e.event_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }
            return events;

        } catch (SQLException e) {
            throw new AppException("Database error while fetching open events: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a single event by ID along with live registeredCount and teamCount.
     */
    public Event getEventById(int id) throws AppException {
        String sql = BASE_EVENT_SELECT + "WHERE e.event_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new AppException("Database error while fetching event: " + e.getMessage(), e);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        return new Event(
            rs.getInt("event_id"),
            rs.getString("event_name"),
            rs.getString("description"),
            rs.getTimestamp("event_date"),
            rs.getString("location"),
            rs.getInt("capacity"),
            rs.getString("participation_type"),
            rs.getInt("min_team_size"),
            rs.getInt("max_team_size"),
            rs.getString("status"),
            rs.getInt("reg_count"),
            rs.getInt("team_count")
        );
    }
}
