package com.digitalpass.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing an audit log entry when a pass is scanned at gates.
 *
 * OOP Concept: Encapsulation
 */
public class ScanLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private Integer regId;
    private String attemptedCode;
    private Integer eventId;
    private int scannedBy;
    private Timestamp scanTime;
    private String result;

    // Display fields from joins
    private String adminUsername;
    private String eventName;
    private String participantName;
    private String prn;

    public ScanLog() {
    }

    public ScanLog(int id, Integer regId, String attemptedCode, Integer eventId,
                   int scannedBy, Timestamp scanTime, String result) {
        this.id = id;
        this.regId = regId;
        this.attemptedCode = attemptedCode;
        this.eventId = eventId;
        this.scannedBy = scannedBy;
        this.scanTime = scanTime;
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getRegId() {
        return regId;
    }

    public void setRegId(Integer regId) {
        this.regId = regId;
    }

    public String getAttemptedCode() {
        return attemptedCode;
    }

    public void setAttemptedCode(String attemptedCode) {
        this.attemptedCode = attemptedCode;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public int getScannedBy() {
        return scannedBy;
    }

    public void setScannedBy(int scannedBy) {
        this.scannedBy = scannedBy;
    }

    public Timestamp getScanTime() {
        return scanTime;
    }

    public void setScanTime(Timestamp scanTime) {
        this.scanTime = scanTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    @Override
    public String toString() {
        return "ScanLog{id=" + id + ", code='" + attemptedCode + "', result='" + result
                + "', time=" + scanTime + "}";
    }
}
