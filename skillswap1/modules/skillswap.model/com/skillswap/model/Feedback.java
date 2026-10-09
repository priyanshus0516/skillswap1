package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * Rating (1-5) and feedback comment given after a completed session.
 */
public class Feedback {

    private int feedbackId;
    private Session session;
    private Student ratedBy;
    private Student ratedTo;
    private int rating;
    private String comments;
    private final LocalDateTime createdAt;

    public Feedback(int feedbackId, Session session, Student ratedBy, Student ratedTo,
                    int rating, String comments) {
        this(feedbackId, session, ratedBy, ratedTo, rating, comments, LocalDateTime.now());
    }

    public Feedback(int feedbackId, Session session, Student ratedBy, Student ratedTo,
                    int rating, String comments, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.session = session;
        this.ratedBy = ratedBy;
        this.ratedTo = ratedTo;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Student getRatedBy() { return ratedBy; }
    public void setRatedBy(Student ratedBy) { this.ratedBy = ratedBy; }

    public Student getRatedTo() { return ratedTo; }
    public void setRatedTo(Student ratedTo) { this.ratedTo = ratedTo; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return ratedBy.getName() + " rated " + ratedTo.getName() + ": " + rating + "/5 - " + comments;
    }
}
