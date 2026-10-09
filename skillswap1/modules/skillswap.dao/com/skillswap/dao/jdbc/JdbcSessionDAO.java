package com.skillswap.dao.jdbc;

import com.skillswap.dao.RequestDAO;
import com.skillswap.dao.SessionDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Session;
import com.skillswap.model.SessionMode;
import com.skillswap.model.SessionStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of SessionDAO for scheduled sessions in MySQL.
 */
public class JdbcSessionDAO implements SessionDAO {

    private final RequestDAO requestDAO;

    public JdbcSessionDAO(RequestDAO requestDAO) {
        this.requestDAO = requestDAO;
    }

    @Override
    public Session findById(int sessionId) throws DataAccessException {
        String sql = "SELECT session_id, request_id, session_datetime, duration_minutes, mode, "
                   + "location_or_link, status FROM sessions WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding session by id " + sessionId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Session> findAll() throws DataAccessException {
        String sql = "SELECT session_id, request_id, session_datetime, duration_minutes, mode, "
                   + "location_or_link, status FROM sessions ORDER BY session_id DESC";
        List<Session> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all sessions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Session> findByStudent(int studentId) throws DataAccessException {
        String sql = "SELECT s.session_id, s.request_id, s.session_datetime, s.duration_minutes, "
                   + "s.mode, s.location_or_link, s.status FROM sessions s "
                   + "JOIN exchange_requests r ON s.request_id = r.request_id "
                   + "WHERE r.sender_id = ? OR r.receiver_id = ? ORDER BY s.session_datetime DESC";
        List<Session> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding sessions for student " + studentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Session save(Session session) throws DataAccessException {
        if (session == null) throw new DataAccessException("Session cannot be null");
        if (session.getSessionId() > 0) {
            String sql = "UPDATE sessions SET session_datetime = ?, duration_minutes = ?, "
                       + "mode = ?, location_or_link = ?, status = ? WHERE session_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(session.getDateTime()));
                ps.setInt(2, session.getDurationMinutes());
                ps.setString(3, session.getMode().name());
                ps.setString(4, session.getLocationOrLink());
                ps.setString(5, session.getStatus().name());
                ps.setInt(6, session.getSessionId());
                ps.executeUpdate();
                return session;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating session: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO sessions (request_id, session_datetime, duration_minutes, "
                       + "mode, location_or_link, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, session.getRequest().getRequestId());
                ps.setTimestamp(2, Timestamp.valueOf(session.getDateTime()));
                ps.setInt(3, session.getDurationMinutes());
                ps.setString(4, session.getMode().name());
                ps.setString(5, session.getLocationOrLink());
                ps.setString(6, session.getStatus().name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        session.setSessionId(keys.getInt(1));
                    }
                }
                return session;
            } catch (SQLException e) {
                throw new DataAccessException("Error creating session: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateStatus(int sessionId, SessionStatus status) throws DataAccessException {
        String sql = "UPDATE sessions SET status = ? WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, sessionId);
            ps.executeUpdate();

            if (status == SessionStatus.COMPLETED) {
                Session session = findById(sessionId);
                if (session != null && session.getRequest() != null) {
                    requestDAO.updateStatus(session.getRequest().getRequestId(),
                            com.skillswap.model.RequestStatus.COMPLETED);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating session status: " + e.getMessage(), e);
        }
    }

    private Session mapRow(ResultSet rs) throws SQLException, DataAccessException {
        int sessId = rs.getInt("session_id");
        int reqId = rs.getInt("request_id");
        Timestamp ts = rs.getTimestamp("session_datetime");
        LocalDateTime dt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        int duration = rs.getInt("duration_minutes");
        SessionMode mode = SessionMode.valueOf(rs.getString("mode"));
        String location = rs.getString("location_or_link");
        SessionStatus status = SessionStatus.valueOf(rs.getString("status"));

        ExchangeRequest req = requestDAO.findById(reqId);
        return new Session(sessId, req, dt, duration, mode, location, status);
    }
}
