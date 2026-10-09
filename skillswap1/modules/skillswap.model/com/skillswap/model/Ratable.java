package com.skillswap.model;

/**
 * Contract for any entity that can accumulate numerical ratings.
 * Demonstrates abstraction and interfaces in OOP.
 */
public interface Ratable {
    void addRating(int rating);
    double getAverageRating();
    int getTotalRatings();
}
