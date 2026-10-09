package com.skillswap.dao;

import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Feedback;

import java.util.List;

/**
 * Data Access Object interface for ratings and feedback operations.
 */
public interface FeedbackDAO {
    Feedback findById(int feedbackId) throws DataAccessException;
    List<Feedback> findAll() throws DataAccessException;
    List<Feedback> findAboutStudent(int studentId) throws DataAccessException;
    Feedback save(Feedback feedback) throws DataAccessException;
}
