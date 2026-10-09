package com.skillswap.dao.jdbc;

import com.skillswap.dao.FeedbackDAO;
import com.skillswap.dao.SessionDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Feedback;
import com.skillswap.model.Session;
import com.skillswap.model.Student;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of FeedbackDAO for ratings in MySQL.
 */
public class JdbcFeedbackDAO implements FeedbackDAO {

    private final SessionDAO sessionDAO;
    private final UserDAO userDAO;

    public JdbcFeedbackDAO(SessionDAO sessionDAO, UserDAO userDAO) {
        this.sessionDAO = sessionDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Feedback findById(int feedbackId) throws DataAccessException {
        String sql = "SELECT feedback_id, session_id, rated_by, rated_to, rating, comments, created_at "
                   + "FROM feedback WHERE feedback_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, feedbackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding feedback by id " + feedbackId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Feedback> findAll() throws DataAccessException {
        String sql = "SELECT feedback_id, session_id, rated_by, rated_to, rating, comments, created_at "
                   + "FROM feedback ORDER BY feedback_id DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all feedback: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Feedback> findAboutStudent(int studentId) throws DataAccessException {
        String sql = "SELECT feedback_id, session_id, rated_by, rated_to, rating, comments, created_at "
                   + "FROM feedback WHERE rated_to = ? ORDER BY feedback_id DESC";
        List<Feedback> list = new ArrayList<>();
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
            throw new DataAccessException("Error retrieving feedback for student " + studentId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Feedback save(Feedback feedback) throws DataAccessException {
        if (feedback == null) throw new DataAccessException("Feedback cannot be null");
        String insertSql = "INSERT INTO feedback (session_id, rated_by, rated_to, rating, comments) "
                         + "VALUES (?, ?, ?, ?, ?)";
        String updateRatingSql = "UPDATE users SET rating_sum = rating_sum + ?, rating_count = rating_count + 1 WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdate = conn.prepareStatement(updateRatingSql)) {

                psInsert.setInt(1, feedback.getSession().getSessionId());
                psInsert.setInt(2, feedback.getRatedBy().getUserId());
                psInsert.setInt(3, feedback.getRatedTo().getUserId());
                psInsert.setInt(4, feedback.getRating());
                psInsert.setString(5, feedback.getComments());
                psInsert.executeUpdate();

                try (ResultSet keys = psInsert.getGeneratedKeys()) {
                    if (keys.next()) {
                        feedback.setFeedbackId(keys.getInt(1));
                    }
                }

                psUpdate.setInt(1, feedback.getRating());
                psUpdate.setInt(2, feedback.getRatedTo().getUserId());
                psUpdate.executeUpdate();

                conn.commit();

                // Update in-memory reference
                feedback.getRatedTo().addRating(feedback.getRating());
                return feedback;
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Error saving feedback transaction: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while saving feedback: " + e.getMessage(), e);
        }
    }

    private Feedback mapRow(ResultSet rs) throws SQLException, DataAccessException {
        int fbId = rs.getInt("feedback_id");
        int sessId = rs.getInt("session_id");
        int ratedById = rs.getInt("rated_by");
        int ratedToId = rs.getInt("rated_to");
        int rating = rs.getInt("rating");
        String comments = rs.getString("comments");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        Session session = sessionDAO.findById(sessId);
        Student ratedBy = userDAO.findById(ratedById);
        Student ratedTo = userDAO.findById(ratedToId);

        return new Feedback(fbId, session, ratedBy, ratedTo, rating, comments, created);
    }
}
