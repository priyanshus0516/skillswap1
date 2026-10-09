package com.skillswap.dao.jdbc;

import com.skillswap.dao.*;

/**
 * Concrete DAOFactory supplying JDBC DAO instances backed by MySQL.
 */
public class JdbcDAOFactory extends DAOFactory {

    private static final JdbcDAOFactory INSTANCE = new JdbcDAOFactory();

    private final JdbcUserDAO userDAO;
    private final JdbcSkillDAO skillDAO;
    private final JdbcRequestDAO requestDAO;
    private final JdbcSessionDAO sessionDAO;
    private final JdbcFeedbackDAO feedbackDAO;

    private JdbcDAOFactory() {
        this.userDAO = new JdbcUserDAO();
        this.skillDAO = new JdbcSkillDAO();
        this.requestDAO = new JdbcRequestDAO(userDAO, skillDAO);
        this.sessionDAO = new JdbcSessionDAO(requestDAO);
        this.feedbackDAO = new JdbcFeedbackDAO(sessionDAO, userDAO);
    }

    public static JdbcDAOFactory getInstance() {
        return INSTANCE;
    }

    @Override public UserDAO getUserDAO() { return userDAO; }
    @Override public SkillDAO getSkillDAO() { return skillDAO; }
    @Override public RequestDAO getRequestDAO() { return requestDAO; }
    @Override public SessionDAO getSessionDAO() { return sessionDAO; }
    @Override public FeedbackDAO getFeedbackDAO() { return feedbackDAO; }
}
