package com.skillswap.dao;

import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;

import java.util.List;

/**
 * Data Access Object interface for User and Student operations.
 */
public interface UserDAO {
    Student findById(int userId) throws DataAccessException;
    Student findByEmail(String email) throws DataAccessException;
    Student findByRegNo(String regNo) throws DataAccessException;
    List<Student> findAllStudents() throws DataAccessException;
    Student save(Student student) throws DataAccessException;
    void update(Student student) throws DataAccessException;

    void addTeachSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException;
    void removeTeachSkill(int userId, Skill skill) throws DataAccessException;
    void updateTeachSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException;

    void addLearnSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException;
    void removeLearnSkill(int userId, Skill skill) throws DataAccessException;
    void updateLearnSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException;

    void updateRatings(int userId, int ratingSum, int ratingCount) throws DataAccessException;
}
