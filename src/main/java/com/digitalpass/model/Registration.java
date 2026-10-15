package com.digitalpass.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing an Attendee Event Registration and Digital Pass.
 *
 * OOP Concept: Encapsulation
 * - Private fields with public accessors.
 * - Encapsulates solo, team leader, and team member pass information.
 */
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    // Database fields
    private int id;
    private int eventId;
    private Integer teamId; // Nullable for SOLO participants
    private String name;
    private String phone;
    private String prn;     // College Roll / Permanent Registration Number
    private String email;
    private String memberRole; // 'SOLO', 'LEADER', 'MEMBER'
    private String passCode;   // Unique alphanumeric pass code (e.g. DP-8K3M2QX7)
    private String status;     // 'REGISTERED', 'CHECKED_IN', 'CANCELLED'
    private Timestamp registeredAt;
    private Timestamp checkedInAt;

    // Display fields from JOIN queries
    private String eventName;
    private String teamName;
    private String location;
    private Timestamp eventDate;

    // Default constructor
    public Registration() {
        this.status = "REGISTERED";
    }

    // Convenience constructor for solo/leader registration
    public Registration(String name, String phone, String prn, String email) {
        this();
        this.name = name;
        this.phone = phone;
        this.prn = prn;
        this.email = email;
    }

    // Convenience constructor for team members (phone is null)
    public Registration(String name, String prn, String email) {
        this(name, null, prn, email);
    }

    // Core constructor
    public Registration(int id, int eventId, Integer teamId, String name, String phone,
                        String prn, String email, String memberRole, String passCode,
                        String status, Timestamp registeredAt, Timestamp checkedInAt) {
        this.id = id;
        this.eventId = eventId;
        this.teamId = teamId;
        this.name = name;
        this.phone = phone;
        this.prn = prn;
        this.email = email;
        this.memberRole = memberRole;
        this.passCode = passCode;
        this.status = status;
        this.registeredAt = registeredAt;
        this.checkedInAt = checkedInAt;
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

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Timestamp getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Timestamp checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isCheckedIn() {
        return "CHECKED_IN".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", prn='" + prn + "', name='" + name
                + "', role='" + memberRole + "', passCode='" + passCode + "', status='" + status + "'}";
    }
}
