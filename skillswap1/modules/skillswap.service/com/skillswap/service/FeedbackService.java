package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.FeedbackDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.exception.InvalidRequestException;
import com.skillswap.model.Feedback;
import com.skillswap.model.Session;
import com.skillswap.model.SessionStatus;
import com.skillswap.model.Student;
import com.skillswap.util.FileLogger;

import java.util.List;

/**
 * Business logic for leaving feedback and ratings after completed sessions.
 */
public class FeedbackService {

    private final FeedbackDAO feedbackDAO;

    public FeedbackService() {
        this(DAOFactory.getInstance().getFeedbackDAO());
    }

    public FeedbackService(FeedbackDAO feedbackDAO) {
        this.feedbackDAO = feedbackDAO;
    }

    public Feedback submit(Session session, Student ratedBy, Student ratedTo, int rating, String comments)
            throws InvalidRequestException, DataAccessException {
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidRequestException("You can only leave feedback after the session has been completed.");
        }
        if (rating < 1 || rating > 5) {
            throw new InvalidRequestException("Rating must be an integer between 1 and 5.");
        }

        Feedback fb = new Feedback(0, session, ratedBy, ratedTo, rating, comments);
        Feedback saved = feedbackDAO.save(fb);
        FileLogger.log("FEEDBACK: " + saved);
        return saved;
    }

    public List<Feedback> feedbackAbout(Student student) throws DataAccessException {
        return feedbackDAO.findAboutStudent(student.getUserId());
    }
}
