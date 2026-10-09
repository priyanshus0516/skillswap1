package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * A scheduled learning session originating from an ACCEPTED exchange request.
 */
public class Session {

    private int sessionId;
    private ExchangeRequest request;
    private LocalDateTime dateTime;
    private int durationMinutes;
    private SessionMode mode;
    private String locationOrLink;
    private SessionStatus status;

    public Session(int sessionId, ExchangeRequest request, LocalDateTime dateTime,
                   int durationMinutes, SessionMode mode, String locationOrLink) {
        this(sessionId, request, dateTime, durationMinutes, mode, locationOrLink, SessionStatus.SCHEDULED);
    }

    public Session(int sessionId, ExchangeRequest request, LocalDateTime dateTime,
                   int durationMinutes, SessionMode mode, String locationOrLink, SessionStatus status) {
        this.sessionId = sessionId;
        this.request = request;
        this.dateTime = dateTime;
        this.durationMinutes = durationMinutes;
        this.mode = mode;
        this.locationOrLink = locationOrLink;
        this.status = status != null ? status : SessionStatus.SCHEDULED;
    }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public ExchangeRequest getRequest() { return request; }
    public void setRequest(ExchangeRequest request) { this.request = request; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public SessionMode getMode() { return mode; }
    public void setMode(SessionMode mode) { this.mode = mode; }

    public String getLocationOrLink() { return locationOrLink; }
    public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Session#" + sessionId + " " + request.getSkillRequested() + " between "
                + request.getSender().getName() + " & " + request.getReceiver().getName()
                + " @ " + dateTime + " (" + status + ")";
    }
}
