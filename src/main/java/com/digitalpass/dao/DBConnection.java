package com.digitalpass.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to manage database connections using plain JDBC.
 *
 * OOP Concept Demonstrated: Abstraction
 * - Hides the complex connection string, driver loading, and credential management
 *   behind a clean, simple static method getConnection().
 *
 * OOP Concept Demonstrated: Exception Handling
 * - Handles ClassNotFoundException for the JDBC driver and throws SQLException
 *   for calling DAO classes to handle gracefully.
 */
public final class DBConnection {

    // =========================================================================
    // DATABASE CONFIGURATION CONSTANTS (Easily editable for viva or lab setup)
    // =========================================================================
    public static final String DB_URL =
            "jdbc:mysql://localhost:3306/digital_pass_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "@Saiprasad5"; // Update this with your local MySQL password if different

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Static block loads the MySQL JDBC driver once when the class is loaded into memory
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    // Private constructor to prevent direct instantiation (Singleton/Factory style)
    private DBConnection() {
    }

    /**
     * Establishes and returns a new connection to MySQL database.
     *
     * @return active java.sql.Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
