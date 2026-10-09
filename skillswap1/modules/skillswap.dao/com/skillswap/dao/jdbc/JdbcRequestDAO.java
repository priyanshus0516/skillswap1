package com.skillswap.dao.jdbc;

import com.skillswap.dao.RequestDAO;
import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.Skill;
import com.skillswap.model.Student;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of RequestDAO for exchange requests in MySQL.
 */
public class JdbcRequestDAO implements RequestDAO {

    private final UserDAO userDAO;
    private final SkillDAO skillDAO;

    public JdbcRequestDAO(UserDAO userDAO, SkillDAO skillDAO) {
        this.userDAO = userDAO;
        this.skillDAO = skillDAO;
    }

    @Override
    public ExchangeRequest findById(int requestId) throws DataAccessException {
        String sql = "SELECT request_id, sender_id, receiver_id, skill_requested_id, "
                   + "skill_offered_id, message, status, created_at FROM exchange_requests WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding request by id " + requestId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExchangeRequest> findAll() throws DataAccessException {
        String sql = "SELECT request_id, sender_id, receiver_id, skill_requested_id, "
                   + "skill_offered_id, message, status, created_at FROM exchange_requests ORDER BY request_id DESC";
        List<ExchangeRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving exchange requests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExchangeRequest> findIncomingFor(int studentId) throws DataAccessException {
        String sql = "SELECT request_id, sender_id, receiver_id, skill_requested_id, "
                   + "skill_offered_id, message, status, created_at FROM exchange_requests "
                   + "WHERE receiver_id = ? ORDER BY request_id DESC";
        List<ExchangeRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving incoming requests for student " + studentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExchangeRequest> findOutgoingFor(int studentId) throws DataAccessException {
        String sql = "SELECT request_id, sender_id, receiver_id, skill_requested_id, "
                   + "skill_offered_id, message, status, created_at FROM exchange_requests "
                   + "WHERE sender_id = ? ORDER BY request_id DESC";
        List<ExchangeRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving outgoing requests for student " + studentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public ExchangeRequest save(ExchangeRequest request) throws DataAccessException {
        if (request == null) throw new DataAccessException("Request cannot be null");
        if (request.getRequestId() > 0) {
            String sql = "UPDATE exchange_requests SET status = ?, message = ? WHERE request_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, request.getStatus().name());
                ps.setString(2, request.getMessage());
                ps.setInt(3, request.getRequestId());
                ps.executeUpdate();
                return request;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating exchange request: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO exchange_requests (sender_id, receiver_id, skill_requested_id, "
                       + "skill_offered_id, message, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, request.getSender().getUserId());
                ps.setInt(2, request.getReceiver().getUserId());
                ps.setInt(3, request.getSkillRequested().getSkillId());
                if (request.getSkillOffered() != null) {
                    ps.setInt(4, request.getSkillOffered().getSkillId());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setString(5, request.getMessage());
                ps.setString(6, request.getStatus().name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        request.setRequestId(keys.getInt(1));
                    }
                }
                return request;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting exchange request: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateStatus(int requestId, RequestStatus status) throws DataAccessException {
        String sql = "UPDATE exchange_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating request status: " + e.getMessage(), e);
        }
    }

    private ExchangeRequest mapRow(ResultSet rs) throws SQLException, DataAccessException {
        int reqId = rs.getInt("request_id");
        int senderId = rs.getInt("sender_id");
        int receiverId = rs.getInt("receiver_id");
        int requestedSkillId = rs.getInt("skill_requested_id");
        int offeredSkillId = rs.getInt("skill_offered_id");
        boolean wasOfferedNull = rs.wasNull();
        String message = rs.getString("message");
        RequestStatus status = RequestStatus.valueOf(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        Student sender = userDAO.findById(senderId);
        Student receiver = userDAO.findById(receiverId);
        Skill requested = skillDAO.findById(requestedSkillId);
        Skill offered = wasOfferedNull ? null : skillDAO.findById(offeredSkillId);

        return new ExchangeRequest(reqId, sender, receiver, requested, offered, message, status, created);
    }
}
