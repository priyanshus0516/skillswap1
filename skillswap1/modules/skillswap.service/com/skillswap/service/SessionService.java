package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.SessionDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.exception.InvalidRequestException;
import com.skillswap.model.*;
import com.skillswap.util.FileLogger;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for scheduling, completing, and cancelling learning sessions.
 */
public class SessionService {

    private final SessionDAO sessionDAO;

    public SessionService() {
        this(DAOFactory.getInstance().getSessionDAO());
    }

    public SessionService(SessionDAO sessionDAO) {
        this.sessionDAO = sessionDAO;
    }

    public Session schedule(ExchangeRequest request, LocalDateTime dateTime, int durationMinutes,
                            SessionMode mode, String locationOrLink)
            throws InvalidRequestException, DataAccessException {
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new InvalidRequestException("Only an ACCEPTED request can be scheduled into a session.");
        }

        Session session = new Session(0, request, dateTime, durationMinutes, mode, locationOrLink);
        Session saved = sessionDAO.save(session);
        FileLogger.log("SESSION SCHEDULED: " + saved);
        return saved;
    }

    public void markCompleted(Session session) throws DataAccessException {
        session.setStatus(SessionStatus.COMPLETED);
        if (session.getRequest() != null) {
            session.getRequest().setStatus(RequestStatus.COMPLETED);
        }
        sessionDAO.updateStatus(session.getSessionId(), SessionStatus.COMPLETED);
        FileLogger.log("SESSION COMPLETED: #" + session.getSessionId());
    }

    public void cancel(Session session) throws DataAccessException {
        session.setStatus(SessionStatus.CANCELLED);
        sessionDAO.updateStatus(session.getSessionId(), SessionStatus.CANCELLED);
        FileLogger.log("SESSION CANCELLED: #" + session.getSessionId());
    }

    public List<Session> sessionsFor(Student student) throws DataAccessException {
        return sessionDAO.findByStudent(student.getUserId());
    }
}
