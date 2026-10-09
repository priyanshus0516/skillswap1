package com.skillswap.dao;

import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Session;
import com.skillswap.model.SessionStatus;

import java.util.List;

/**
 * Data Access Object interface for Session scheduling and lifecycle operations.
 */
public interface SessionDAO {
    Session findById(int sessionId) throws DataAccessException;
    List<Session> findAll() throws DataAccessException;
    List<Session> findByStudent(int studentId) throws DataAccessException;
    Session save(Session session) throws DataAccessException;
    void updateStatus(int sessionId, SessionStatus status) throws DataAccessException;
}
