package com.skillswap.dao.inmemory;

import com.skillswap.dao.SkillDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of SkillDAO.
 */
public class InMemorySkillDAO implements SkillDAO {

    private final List<Skill> skills = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public InMemorySkillDAO() {
        seedInitialSkills();
    }

    private void seedInitialSkills() {
        try {
            findOrCreate("Java Programming", "Technical");
            findOrCreate("Guitar", "Music");
            findOrCreate("Photoshop", "Design");
            findOrCreate("Public Speaking", "Soft Skill");
            findOrCreate("Python", "Technical");
            findOrCreate("Spoken French", "Language");
            findOrCreate("Chess", "Games");
        } catch (DataAccessException e) {
            System.err.println("Could not seed skills: " + e.getMessage());
        }
    }

    @Override
    public synchronized Skill findById(int skillId) throws DataAccessException {
        for (Skill s : skills) {
            if (s.getSkillId() == skillId) return s;
        }
        return null;
    }

    @Override
    public synchronized Skill findByName(String name) throws DataAccessException {
        if (name == null) return null;
        for (Skill s : skills) {
            if (s.getName().equalsIgnoreCase(name.trim())) return s;
        }
        return null;
    }

    @Override
    public synchronized List<Skill> findAll() throws DataAccessException {
        return new ArrayList<>(skills);
    }

    @Override
    public synchronized Skill save(Skill skill) throws DataAccessException {
        if (skill == null) throw new DataAccessException("Skill cannot be null");
        if (skill.getSkillId() <= 0) {
            skill.setSkillId(nextId.getAndIncrement());
        }
        skills.removeIf(s -> s.getSkillId() == skill.getSkillId() || s.getName().equalsIgnoreCase(skill.getName()));
        skills.add(skill);
        return skill;
    }

    @Override
    public synchronized Skill findOrCreate(String name, String category) throws DataAccessException {
        if (name == null || name.trim().isEmpty()) {
            throw new DataAccessException("Skill name cannot be empty");
        }
        Skill existing = findByName(name.trim());
        if (existing != null) return existing;

        Skill created = new Skill(nextId.getAndIncrement(), name.trim(), category != null ? category.trim() : "Other");
        skills.add(created);
        return created;
    }
}
