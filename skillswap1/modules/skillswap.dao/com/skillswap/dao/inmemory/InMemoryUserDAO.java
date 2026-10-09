package com.skillswap.dao.inmemory;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.DataAccessException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillLevel;
import com.skillswap.model.Student;
import com.skillswap.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of UserDAO.
 */
public class InMemoryUserDAO implements UserDAO {

    private final List<Student> students = new ArrayList<>();
    private final AtomicInteger nextUserId = new AtomicInteger(1);
    private final SkillDAO skillDAO;

    public InMemoryUserDAO(SkillDAO skillDAO) {
        this.skillDAO = skillDAO;
    }

    public synchronized void seedStudents() {
        try {
            Skill java = skillDAO.findByName("Java Programming");
            Skill guitar = skillDAO.findByName("Guitar");
            Skill photoshop = skillDAO.findByName("Photoshop");
            Skill publicSpeaking = skillDAO.findByName("Public Speaking");

            String pass = PasswordUtil.hash("pass123");

            Student priyanshu = new Student(nextUserId.getAndIncrement(), "RA2511003030200",
                    "Priyanshu Sharma", "ps0612@srmist.edu.in", pass, 2, "CSE CORE");
            if (java != null) priyanshu.addTeachSkill(java, SkillLevel.ADVANCED);
            if (guitar != null) priyanshu.addLearnSkill(guitar, SkillLevel.BEGINNER);

            Student ananya = new Student(nextUserId.getAndIncrement(), "RA2511002",
                    "Ananya Rao", "ananya@srmist.edu.in", pass, 2, "CSE CORE");
            if (guitar != null) ananya.addTeachSkill(guitar, SkillLevel.EXPERT);
            if (java != null) ananya.addLearnSkill(java, SkillLevel.INTERMEDIATE);

            Student kabir = new Student(nextUserId.getAndIncrement(), "RA2511003",
                    "Kabir Mehta", "kabir@srmist.edu.in", pass, 3, "ECE");
            if (photoshop != null) kabir.addTeachSkill(photoshop, SkillLevel.ADVANCED);
            if (publicSpeaking != null) kabir.addLearnSkill(publicSpeaking, SkillLevel.BEGINNER);

            Student diya = new Student(nextUserId.getAndIncrement(), "RA2511004",
                    "Diya Nair", "diya@srmist.edu.in", pass, 1, "IT");
            if (publicSpeaking != null) diya.addTeachSkill(publicSpeaking, SkillLevel.INTERMEDIATE);
            if (photoshop != null) diya.addLearnSkill(photoshop, SkillLevel.BEGINNER);

            students.add(priyanshu);
            students.add(ananya);
            students.add(kabir);
            students.add(diya);
        } catch (DataAccessException e) {
            System.err.println("Could not seed students: " + e.getMessage());
        }
    }

    @Override
    public synchronized Student findById(int userId) throws DataAccessException {
        for (Student s : students) {
            if (s.getUserId() == userId) return s;
        }
        return null;
    }

    @Override
    public synchronized Student findByEmail(String email) throws DataAccessException {
        if (email == null) return null;
        for (Student s : students) {
            if (s.getEmail().equalsIgnoreCase(email.trim())) return s;
        }
        return null;
    }

    @Override
    public synchronized Student findByRegNo(String regNo) throws DataAccessException {
        if (regNo == null) return null;
        for (Student s : students) {
            if (s.getRegNo().equalsIgnoreCase(regNo.trim())) return s;
        }
        return null;
    }

    @Override
    public synchronized List<Student> findAllStudents() throws DataAccessException {
        return new ArrayList<>(students);
    }

    @Override
    public synchronized Student save(Student student) throws DataAccessException {
        if (student == null) throw new DataAccessException("Student cannot be null");
        if (student.getUserId() <= 0) {
            student.setUserId(nextUserId.getAndIncrement());
        }
        students.removeIf(s -> s.getUserId() == student.getUserId());
        students.add(student);
        return student;
    }

    @Override
    public synchronized void update(Student student) throws DataAccessException {
        if (student == null) return;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getUserId() == student.getUserId()) {
                students.set(i, student);
                return;
            }
        }
        students.add(student);
    }

    @Override
    public synchronized void addTeachSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.addTeachSkill(skill, level);
        }
    }

    @Override
    public synchronized void removeTeachSkill(int userId, Skill skill) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.removeTeachSkill(skill);
        }
    }

    @Override
    public synchronized void updateTeachSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.addTeachSkill(skill, level);
        }
    }

    @Override
    public synchronized void addLearnSkill(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.addLearnSkill(skill, level);
        }
    }

    @Override
    public synchronized void removeLearnSkill(int userId, Skill skill) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.removeLearnSkill(skill);
        }
    }

    @Override
    public synchronized void updateLearnSkillLevel(int userId, Skill skill, SkillLevel level) throws DataAccessException {
        Student s = findById(userId);
        if (s != null && skill != null) {
            s.addLearnSkill(skill, level);
        }
    }

    @Override
    public synchronized void updateRatings(int userId, int ratingSum, int ratingCount) throws DataAccessException {
        Student s = findById(userId);
        if (s != null) {
            s.setRatingStats(ratingSum, ratingCount);
        }
    }
}
