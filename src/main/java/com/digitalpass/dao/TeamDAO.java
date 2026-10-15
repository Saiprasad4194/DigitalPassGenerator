package com.digitalpass.dao;

import com.digitalpass.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Team management.
 *
 * OOP Concept: Abstraction & Collections (ArrayList)
 */
public class TeamDAO {

    /**
     * Creates a new team for an event and returns the generated team_id.
     */
    public int createTeam(int eventId, String teamName) throws SQLException {
        String sql = "INSERT INTO teams (event_id, team_name, created_at) VALUES (?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, eventId);
            stmt.setString(2, teamName.trim());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create team record.");
    }

    /**
     * Retrieves a team by its primary key id.
     */
    public Team getTeamById(int teamId) throws SQLException {
        String sql = "SELECT team_id, event_id, team_name, created_at FROM teams WHERE team_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teamId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Team(
                        rs.getInt("team_id"),
                        rs.getInt("event_id"),
                        rs.getString("team_name"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all teams participating in a specific event.
     */
    public List<Team> getTeamsByEvent(int eventId) throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT team_id, event_id, team_name, created_at FROM teams WHERE event_id = ? ORDER BY team_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Team(
                        rs.getInt("team_id"),
                        rs.getInt("event_id"),
                        rs.getString("team_name"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }
}
