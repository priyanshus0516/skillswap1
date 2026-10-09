package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;

import java.util.List;

/**
 * Service managing skill catalogue operations and student skill assignments.
 */
public class SkillService {

    private final SkillDAO skillDAO;
    private final UserDAO userDAO;

    public SkillService() {
        this(DAOFactory.getInstance().getSkillDAO(), DAOFactory.getInstance().getUserDAO());
    }

    public SkillService(SkillDAO skillDAO, UserDAO userDAO) {
        this.skillDAO = skillDAO;
        this.userDAO = userDAO;
    }

    public Skill findOrCreateSkill(String name, String category) throws DataAccessException {
        return skillDAO.findOrCreate(name, category);
    }

    public List<Skill> getAllSkills() throws DataAccessException {
        return skillDAO.findAll();
    }

    public Skill getSkillById(int id) throws DataAccessException {
        return skillDAO.findById(id);
    }

    public void addTeachSkill(Student student, Skill skill, SkillLevel level) throws DataAccessException {
        student.addTeachSkill(skill, level);
        userDAO.addTeachSkill(student.getUserId(), skill, level);
    }

    public void removeTeachSkill(Student student, Skill skill) throws DataAccessException {
        student.removeTeachSkill(skill);
        userDAO.removeTeachSkill(student.getUserId(), skill);
    }

    public void updateTeachSkillLevel(Student student, Skill skill, SkillLevel level) throws DataAccessException {
        student.addTeachSkill(skill, level);
        userDAO.updateTeachSkillLevel(student.getUserId(), skill, level);
    }

    public void addLearnSkill(Student student, Skill skill, SkillLevel level) throws DataAccessException {
        student.addLearnSkill(skill, level);
        userDAO.addLearnSkill(student.getUserId(), skill, level);
    }

    public void removeLearnSkill(Student student, Skill skill) throws DataAccessException {
        student.removeLearnSkill(skill);
        userDAO.removeLearnSkill(student.getUserId(), skill);
    }

    public void updateLearnSkillLevel(Student student, Skill skill, SkillLevel level) throws DataAccessException {
        student.addLearnSkill(skill, level);
        userDAO.updateLearnSkillLevel(student.getUserId(), skill, level);
    }
}
