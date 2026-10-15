package com.digitalpass.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing a Team formed for team-based events.
 *
 * OOP Concept: Encapsulation
 * - Private fields, getters, setters, constructors, and toString().
 */
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int eventId;
    private String name;
    private Timestamp createdAt;

    // Default constructor
    public Team() {
    }

    // Parameterized constructor without ID (for creation)
    public Team(int eventId, String name) {
        this.eventId = eventId;
        this.name = name;
    }

    // Full constructor
    public Team(int id, int eventId, String name, Timestamp createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Team{id=" + id + ", eventId=" + eventId + ", name='" + name + "'}";
    }
}
