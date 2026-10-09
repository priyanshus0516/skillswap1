package com.skillswap.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for every user in the platform.
 * Subclasses demonstrate inheritance and polymorphic displayProfile().
 */
public abstract class User {

    protected int userId;
    protected String regNo;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected LocalDateTime createdAt;

    public User(int userId, String regNo, String name, String email, String passwordHash) {
        this(userId, regNo, name, email, passwordHash, LocalDateTime.now());
    }

    public User(int userId, String regNo, String name, String email, String passwordHash, LocalDateTime createdAt) {
        this.userId = userId;
        this.regNo = regNo;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public abstract String displayProfile();

    public abstract String getRole();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + regNo + ") <" + email + ">";
    }
}
