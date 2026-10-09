package com.skillswap.dao.inmemory;

import com.skillswap.dao.SessionDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Session;
import com.skillswap.model.SessionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of SessionDAO.
 */
public class InMemorySessionDAO implements SessionDAO {

    private final List<Session> sessions = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public synchronized Session findById(int sessionId) throws DataAccessException {
        for (Session s : sessions) {
            if (s.getSessionId() == sessionId) return s;
        }
        return null;
    }

    @Override
    public synchronized List<Session> findAll() throws DataAccessException {
        return new ArrayList<>(sessions);
    }

    @Override
    public synchronized List<Session> findByStudent(int studentId) throws DataAccessException {
        List<Session> result = new ArrayList<>();
        for (Session s : sessions) {
            ExchangeRequest r = s.getRequest();
            if (r != null && (r.getSender().getUserId() == studentId || r.getReceiver().getUserId() == studentId)) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public synchronized Session save(Session session) throws DataAccessException {
        if (session == null) throw new DataAccessException("Session cannot be null");
        if (session.getSessionId() <= 0) {
            session.setSessionId(nextId.getAndIncrement());
        }
        sessions.removeIf(s -> s.getSessionId() == session.getSessionId());
        sessions.add(session);
        return session;
    }

    @Override
    public synchronized void updateStatus(int sessionId, SessionStatus status) throws DataAccessException {
        Session s = findById(sessionId);
        if (s != null) {
            s.setStatus(status);
            if (s.getRequest() != null && status == SessionStatus.COMPLETED) {
                s.getRequest().setStatus(com.skillswap.model.RequestStatus.COMPLETED);
            }
        }
    }
}
