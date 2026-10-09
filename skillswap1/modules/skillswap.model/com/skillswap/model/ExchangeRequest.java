package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * A peer-to-peer exchange request sent from one student to another.
 */
public class ExchangeRequest {

    private int requestId;
    private Student sender;
    private Student receiver;
    private Skill skillRequested;
    private Skill skillOffered;
    private String message;
    private RequestStatus status;
    private final LocalDateTime createdAt;

    public ExchangeRequest(int requestId, Student sender, Student receiver,
                           Skill skillRequested, Skill skillOffered, String message) {
        this(requestId, sender, receiver, skillRequested, skillOffered, message,
             RequestStatus.PENDING, LocalDateTime.now());
    }

    public ExchangeRequest(int requestId, Student sender, Student receiver,
                           Skill skillRequested, Skill skillOffered, String message,
                           RequestStatus status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.sender = sender;
        this.receiver = receiver;
        this.skillRequested = skillRequested;
        this.skillOffered = skillOffered;
        this.message = message;
        this.status = status != null ? status : RequestStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public Student getSender() { return sender; }
    public void setSender(Student sender) { this.sender = sender; }

    public Student getReceiver() { return receiver; }
    public void setReceiver(Student receiver) { this.receiver = receiver; }

    public Skill getSkillRequested() { return skillRequested; }
    public void setSkillRequested(Skill skillRequested) { this.skillRequested = skillRequested; }

    public Skill getSkillOffered() { return skillOffered; }
    public void setSkillOffered(Skill skillOffered) { this.skillOffered = skillOffered; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "#" + requestId + " " + sender.getName() + " -> " + receiver.getName()
                + " [" + skillRequested + " for " + (skillOffered != null ? skillOffered : "none")
                + "] (" + status + ")";
    }
}
