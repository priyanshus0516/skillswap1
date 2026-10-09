package com.skillswap.dao.jdbc;

import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDAO for Student accounts in MySQL.
 */
public class JdbcUserDAO implements UserDAO {

    @Override
    public Student findById(int userId) throws DataAccessException {
        String sql = "SELECT user_id, reg_no, name, email, password_hash, year_of_study, department, "
                   + "rating_sum, rating_count, created_at FROM users WHERE user_id = ? AND role = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapStudent(rs);
                    loadSkills(conn, s);
                    return s;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by id " + userId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Student findByEmail(String email) throws DataAccessException {
        if (email == null) return null;
        String sql = "SELECT user_id, reg_no, name, email, password_hash, year_of_study, department, "
                   + "rating_sum, rating_count, created_at FROM users WHERE LOWER(email) = LOWER(?) AND role = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapStudent(rs);
                    loadSkills(conn, s);
                    return s;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by email '" + email + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Student findByRegNo(String regNo) throws DataAccessException {
        if (regNo == null) return null;
        String sql = "SELECT user_id, reg_no, name, email, password_hash, year_of_study, department, "
                   + "rating_sum, rating_count, created_at FROM users WHERE LOWER(reg_no) = LOWER(?) AND role = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapStudent(rs);
                    loadSkills(conn, s);
                    return s;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by regNo '" + regNo + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> findAllStudents() throws DataAccessException {
        String sql = "SELECT user_id, reg_no, name, email, password_hash, year_of_study, department, "
                   + "rating_sum, rating_count, created_at FROM users WHERE role = 'STUDENT' ORDER BY user_id ASC";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
            for (Student s : list) {
                loadSkills(conn, s);
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all students: " + e.getMessage(), e);
        }
    }

    @Override
    public Student save(Student student) throws DataAccessException {
        if (student == null) throw new DataAccessException("Student cannot be null");
        String sql = "INSERT INTO users (reg_no, name, email, password_hash, role, year_of_study, department, rating_sum, rating_count) "
                   + "VALUES (?, ?, ?, ?, 'STUDENT', ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getRegNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPasswordHash());
            ps.setInt(5, student.getYearOfStudy());
            ps.setString(6, student.getDepartment());
            ps.setInt(7, student.getRatingSum());
            ps.setInt(8, student.getRatingCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setUserId(keys.getInt(1));
                }
            }
            return student;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving student " + student.getEmail() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Student student) throws DataAccessException {
        if (student == null) return;
        String sql = "UPDATE users SET reg_no = ?, name = ?, email = ?, password_hash = ?, "
                   + "year_of_study = ?, department = ?, rating_sum = ?, rating_count = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRegNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPasswordHash());
            ps.setInt(5, student.getYearOfStudy());
            ps.setString(6, student.getDepartment());
            ps.setInt(7, student.getRatingSum());
            ps.setInt(8, student.getRatingCount());
            ps.setInt(9, student.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating student: " + e.getMessage(), e);
        }
    }

    @Override
    public void addTeachSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        if (skill == null || level == null) return;
        String sql = "INSERT INTO user_teach_skills (user_id, skill_id, proficiency) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE proficiency = VALUES(proficiency)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, skill.getSkillId());
            ps.setString(3, level.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding teach skill: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeTeachSkill(int userId, Skill skill) throws DataAccessException {
        if (skill == null) return;
        String sql = "DELETE FROM user_teach_skills WHERE user_id = ? AND skill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, skill.getSkillId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error removing teach skill: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTeachSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        addTeachSkill(userId, skill, level);
    }

    @Override
    public void addLearnSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        if (skill == null || level == null) return;
        String sql = "INSERT INTO user_learn_skills (user_id, skill_id, desired_level) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE desired_level = VALUES(desired_level)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, skill.getSkillId());
            ps.setString(3, level.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding learn skill: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeLearnSkill(int userId, Skill skill) throws DataAccessException {
        if (skill == null) return;
        String sql = "DELETE FROM user_learn_skills WHERE user_id = ? AND skill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, skill.getSkillId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error removing learn skill: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLearnSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        addLearnSkill(userId, skill, level);
    }

    @Override
    public void updateRatings(int userId, int ratingSum, int ratingCount) throws DataAccessException {
        String sql = "UPDATE users SET rating_sum = ?, rating_count = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ratingSum);
            ps.setInt(2, ratingCount);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating user ratings: " + e.getMessage(), e);
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Student(
                rs.getInt("user_id"),
                rs.getString("reg_no"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("year_of_study"),
                rs.getString("department"),
                rs.getInt("rating_sum"),
                rs.getInt("rating_count"),
                created
        );
    }

    private void loadSkills(Connection conn, Student s) throws SQLException {
        s.clearTeachSkills();
        s.clearLearnSkills();

        String teachSql = "SELECT s.skill_id, s.skill_name, s.category, t.proficiency "
                        + "FROM user_teach_skills t JOIN skills s ON t.skill_id = s.skill_id WHERE t.user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(teachSql)) {
            ps.setInt(1, s.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill sk = new Skill(rs.getInt("skill_id"), rs.getString("skill_name"), rs.getString("category"));
                    SkillLevel lvl = SkillLevel.valueOf(rs.getString("proficiency"));
                    s.addTeachSkill(sk, lvl);
                }
            }
        }

        String learnSql = "SELECT s.skill_id, s.skill_name, s.category, l.desired_level "
                        + "FROM user_learn_skills l JOIN skills s ON l.skill_id = s.skill_id WHERE l.user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(learnSql)) {
            ps.setInt(1, s.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill sk = new Skill(rs.getInt("skill_id"), rs.getString("skill_name"), rs.getString("category"));
                    SkillLevel lvl = SkillLevel.valueOf(rs.getString("desired_level"));
                    s.addLearnSkill(sk, lvl);
                }
            }
        }
    }
}
