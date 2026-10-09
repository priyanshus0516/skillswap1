package com.skillswap.dao.inmemory;

import com.skillswap.dao.FeedbackDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of FeedbackDAO.
 */
public class InMemoryFeedbackDAO implements FeedbackDAO {

    private final List<Feedback> feedbackList = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final UserDAO userDAO;

    public InMemoryFeedbackDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public synchronized Feedback findById(int feedbackId) throws DataAccessException {
        for (Feedback f : feedbackList) {
            if (f.getFeedbackId() == feedbackId) return f;
        }
        return null;
    }

    @Override
    public synchronized List<Feedback> findAll() throws DataAccessException {
        return new ArrayList<>(feedbackList);
    }

    @Override
    public synchronized List<Feedback> findAboutStudent(int studentId) throws DataAccessException {
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : feedbackList) {
            if (f.getRatedTo() != null && f.getRatedTo().getUserId() == studentId) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public synchronized Feedback save(Feedback feedback) throws DataAccessException {
        if (feedback == null) throw new DataAccessException("Feedback cannot be null");
        if (feedback.getFeedbackId() <= 0) {
            feedback.setFeedbackId(nextId.getAndIncrement());
        }
        feedbackList.removeIf(f -> f.getFeedbackId() == feedback.getFeedbackId());
        feedbackList.add(feedback);

        // Update the rated student's aggregate stats
        if (feedback.getRatedTo() != null && userDAO != null) {
            feedback.getRatedTo().addRating(feedback.getRating());
            userDAO.updateRatings(feedback.getRatedTo().getUserId(),
                    feedback.getRatedTo().getRatingSum(),
                    feedback.getRatedTo().getRatingCount());
        }

        return feedback;
    }
}
