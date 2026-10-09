package com.skillswap.dao.jdbc;

import com.skillswap.dao.SkillDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of SkillDAO against MySQL skills table.
 */
public class JdbcSkillDAO implements SkillDAO {

    @Override
    public Skill findById(int skillId) throws DataAccessException {
        String sql = "SELECT skill_id, skill_name, category FROM skills WHERE skill_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding skill by id " + skillId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Skill findByName(String name) throws DataAccessException {
        if (name == null) return null;
        String sql = "SELECT skill_id, skill_name, category FROM skills WHERE LOWER(skill_name) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding skill by name '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public List<Skill> findAll() throws DataAccessException {
        String sql = "SELECT skill_id, skill_name, category FROM skills ORDER BY skill_name ASC";
        List<Skill> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving skills list: " + e.getMessage(), e);
        }
    }

    @Override
    public Skill save(Skill skill) throws DataAccessException {
        if (skill == null) throw new DataAccessException("Skill cannot be null");
        if (skill.getSkillId() > 0) {
            String sql = "UPDATE skills SET skill_name = ?, category = ? WHERE skill_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, skill.getName());
                ps.setString(2, skill.getCategory());
                ps.setInt(3, skill.getSkillId());
                ps.executeUpdate();
                return skill;
            } catch (SQLException e) {
                throw new DataAccessException("Error updating skill: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO skills (skill_name, category) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, skill.getName());
                ps.setString(2, skill.getCategory());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        skill.setSkillId(keys.getInt(1));
                    }
                }
                return skill;
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting skill: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Skill findOrCreate(String name, String category) throws DataAccessException {
        if (name == null || name.trim().isEmpty()) {
            throw new DataAccessException("Skill name cannot be empty");
        }
        Skill existing = findByName(name.trim());
        if (existing != null) {
            return existing;
        }
        Skill newSkill = new Skill(0, name.trim(), category != null ? category.trim() : "Other");
        return save(newSkill);
    }

    private Skill mapRow(ResultSet rs) throws SQLException {
        return new Skill(
                rs.getInt("skill_id"),
                rs.getString("skill_name"),
                rs.getString("category")
        );
    }
}
