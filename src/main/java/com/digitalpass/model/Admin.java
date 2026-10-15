package com.digitalpass.model;

import java.io.Serializable;

/**
 * Model class representing an Admin staff user.
 *
 * OOP Concept: Encapsulation
 * - All instance fields are private.
 * - Access is provided strictly via public getters and setters.
 */
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String passwordHash;

    // Default constructor
    public Admin() {
    }

    // Parameterized constructor without ID (for insertion)
    public Admin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Full constructor
    public Admin(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "'}";
    }
}
