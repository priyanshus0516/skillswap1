package com.skillswap.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A student account. Extends User (inheritance) and implements Ratable.
 */
public class Student extends User implements Ratable {

    private int yearOfStudy;
    private String department;

    // Skill -> proficiency level this student can TEACH
    private final Map<Skill, SkillLevel> teachSkills = new LinkedHashMap<>();
    // Skill -> desired level this student wants to LEARN
    private final Map<Skill, SkillLevel> learnSkills = new LinkedHashMap<>();

    private int ratingSum = 0;
    private int ratingCount = 0;

    public Student(int userId, String regNo, String name, String email,
                   String passwordHash, int yearOfStudy, String department) {
        super(userId, regNo, name, email, passwordHash);
        this.yearOfStudy = yearOfStudy;
        this.department = department;
    }

    public Student(int userId, String regNo, String name, String email,
                   String passwordHash, int yearOfStudy, String department,
                   int ratingSum, int ratingCount, LocalDateTime createdAt) {
        super(userId, regNo, name, email, passwordHash, createdAt);
        this.yearOfStudy = yearOfStudy;
        this.department = department;
        this.ratingSum = ratingSum;
        this.ratingCount = ratingCount;
    }

    @Override
    public String displayProfile() {
        return String.format(
            "%s | Year %d, %s | Teaches: %s | Wants to learn: %s | Rating: %.1f (%d reviews)",
            name, yearOfStudy, department, teachSkills.keySet(), learnSkills.keySet(),
            getAverageRating(), ratingCount);
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    // ---------------- Ratable Implementation ----------------
    @Override
    public void addRating(int rating) {
        this.ratingSum += rating;
        this.ratingCount++;
    }

    @Override
    public double getAverageRating() {
        return ratingCount == 0 ? 0.0 : (double) ratingSum / ratingCount;
    }

    @Override
    public int getTotalRatings() {
        return ratingCount;
    }

    public int getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(int ratingSum) {
        this.ratingSum = ratingSum;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setRatingStats(int sum, int count) {
        this.ratingSum = sum;
        this.ratingCount = count;
    }

    // ---------------- Skill Management ----------------
    public void addTeachSkill(Skill skill, SkillLevel level) {
        teachSkills.put(skill, level);
    }

    public void removeTeachSkill(Skill skill) {
        teachSkills.remove(skill);
    }

    public void clearTeachSkills() {
        teachSkills.clear();
    }

    public void addLearnSkill(Skill skill, SkillLevel level) {
        learnSkills.put(skill, level);
    }

    public void removeLearnSkill(Skill skill) {
        learnSkills.remove(skill);
    }

    public void clearLearnSkills() {
        learnSkills.clear();
    }

    public Map<Skill, SkillLevel> getTeachSkills() {
        return teachSkills;
    }

    public Map<Skill, SkillLevel> getLearnSkills() {
        return learnSkills;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
