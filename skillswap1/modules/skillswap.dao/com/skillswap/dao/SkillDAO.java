package com.skillswap.dao;

import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;

import java.util.List;

/**
 * Data Access Object interface for Skill catalogue operations.
 */
public interface SkillDAO {
    Skill findById(int skillId) throws DataAccessException;
    Skill findByName(String name) throws DataAccessException;
    List<Skill> findAll() throws DataAccessException;
    Skill save(Skill skill) throws DataAccessException;
    Skill findOrCreate(String name, String category) throws DataAccessException;
}
