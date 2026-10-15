package com.digitalpass.dao;

import com.digitalpass.model.Admin;
import com.digitalpass.util.AppException;
import com.digitalpass.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for Admin accounts.
 *
 * Plain JDBC, PreparedStatement only, try-with-resources.
 */
public class AdminDAO {

    /**
     * Finds an admin account by username.
     *
     * @param username the username to look up
     * @return Admin object if found, or null
     * @throws AppException if a database access error occurs
     */
    public Admin findByUsername(String username) throws AppException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT admin_id, username, password_hash FROM admins WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            throw new AppException("Database error while finding admin: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Authenticates an admin using username and raw password.
     */
    public Admin authenticate(String username, String plainPassword) throws AppException {
        if (username == null || plainPassword == null) {
            return null;
        }

        Admin admin = findByUsername(username);
        if (admin != null) {
            String hash = PasswordUtil.hash(plainPassword);
            if (hash.equalsIgnoreCase(admin.getPasswordHash())) {
                return admin;
            }
        }
        return null;
    }

    /**
     * Finds an admin by primary key id.
     */
    public Admin getAdminById(int adminId) throws AppException {
        String sql = "SELECT admin_id, username, password_hash FROM admins WHERE admin_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            throw new AppException("Database error while fetching admin by ID: " + e.getMessage(), e);
        }
        return null;
    }
}
