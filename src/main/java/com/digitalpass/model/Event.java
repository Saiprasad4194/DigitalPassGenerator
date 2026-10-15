package com.digitalpass.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing an Event.
 *
 * OOP Concept: Encapsulation
 * - Private fields, public accessors, robust constructors, and toString().
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    // Database fields
    private int id;
    private String eventName;
    private String description;
    private Timestamp eventDate;
    private String location;
    private int capacity;
    private String participationType; // 'INDIVIDUAL', 'TEAM', 'BOTH'
    private int minTeamSize;
    private int maxTeamSize;
    private String status; // 'OPEN', 'CLOSED'

    // Display fields for UI metrics
    private int registeredCount;
    private int teamCount;

    // Default constructor
    public Event() {
        this.minTeamSize = 1;
        this.maxTeamSize = 1;
        this.status = "OPEN";
    }

    // Core parameterized constructor
    public Event(int id, String eventName, String description, Timestamp eventDate,
                 String location, int capacity, String participationType,
                 int minTeamSize, int maxTeamSize, String status) {
        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.capacity = capacity;
        this.participationType = participationType;
        this.minTeamSize = minTeamSize;
        this.maxTeamSize = maxTeamSize;
        this.status = status;
    }

    // Full constructor including display counters
    public Event(int id, String eventName, String description, Timestamp eventDate,
                 String location, int capacity, String participationType,
                 int minTeamSize, int maxTeamSize, String status,
                 int registeredCount, int teamCount) {
        this(id, eventName, description, eventDate, location, capacity, participationType, minTeamSize, maxTeamSize, status);
        this.registeredCount = registeredCount;
        this.teamCount = teamCount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getParticipationType() {
        return participationType;
    }

    public void setParticipationType(String participationType) {
        this.participationType = participationType;
    }

    public int getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(int minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    // Helper checks
    public boolean isTeamAllowed() {
        return "TEAM".equalsIgnoreCase(participationType) || "BOTH".equalsIgnoreCase(participationType);
    }

    public boolean isIndividualAllowed() {
        return "INDIVIDUAL".equalsIgnoreCase(participationType) || "BOTH".equalsIgnoreCase(participationType);
    }

    public boolean isFull() {
        return registeredCount >= capacity;
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", name='" + eventName + "', type='" + participationType
                + "', capacity=" + capacity + ", registered=" + registeredCount + ", status='" + status + "'}";
    }
}
