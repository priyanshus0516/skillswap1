package com.skillswap.service;

import com.skillswap.dao.DAOFactory;
import com.skillswap.dao.UserDAO;
import com.skillswap.exception.AuthenticationException;
import com.skillswap.exception.DataAccessException;
import com.skillswap.exception.DuplicateUserException;
import com.skillswap.model.Student;
import com.skillswap.util.FileLogger;
import com.skillswap.util.PasswordUtil;

import java.util.List;

/**
 * Handles student registration, authentication, and user retrieval.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this(DAOFactory.getInstance().getUserDAO());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Student register(String regNo, String name, String email, String rawPassword,
                            int year, String department) throws DuplicateUserException, DataAccessException {
        if (userDAO.findByEmail(email) != null) {
            throw new DuplicateUserException("A student with email '" + email + "' already exists.");
        }
        if (userDAO.findByRegNo(regNo) != null) {
            throw new DuplicateUserException("A student with registration number '" + regNo + "' already exists.");
        }

        Student student = new Student(0, regNo, name, email,
                PasswordUtil.hash(rawPassword), year, department);
        Student saved = userDAO.save(student);
        FileLogger.log("REGISTER: " + saved);
        return saved;
    }

    public Student login(String email, String rawPassword) throws AuthenticationException, DataAccessException {
        Student student = userDAO.findByEmail(email);
        if (student == null) {
            throw new AuthenticationException("No account found for that email.");
        }
        if (!PasswordUtil.matches(rawPassword, student.getPasswordHash())) {
            throw new AuthenticationException("Incorrect password.");
        }
        FileLogger.log("LOGIN: " + student.getEmail());
        return student;
    }

    public List<Student> getAllStudents() throws DataAccessException {
        return userDAO.findAllStudents();
    }

    public Student getStudentById(int userId) throws DataAccessException {
        return userDAO.findById(userId);
    }
}
