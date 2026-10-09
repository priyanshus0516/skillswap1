package com.skillswap.dao.inmemory;

import com.skillswap.dao.*;

/**
 * Concrete DAOFactory supplying In-Memory DAO instances.
 */
public class InMemoryDAOFactory extends DAOFactory {

    private static final InMemoryDAOFactory INSTANCE = new InMemoryDAOFactory();

    private final InMemorySkillDAO skillDAO;
    private final InMemoryUserDAO userDAO;
    private final InMemoryRequestDAO requestDAO;
    private final InMemorySessionDAO sessionDAO;
    private final InMemoryFeedbackDAO feedbackDAO;

    private InMemoryDAOFactory() {
        this.skillDAO = new InMemorySkillDAO();
        this.userDAO = new InMemoryUserDAO(skillDAO);
        this.requestDAO = new InMemoryRequestDAO();
        this.sessionDAO = new InMemorySessionDAO();
        this.feedbackDAO = new InMemoryFeedbackDAO(userDAO);

        // Seed initial demo data
        seedDemoData();
    }

    public static InMemoryDAOFactory getInstance() {
        return INSTANCE;
    }

    @Override public UserDAO getUserDAO() { return userDAO; }
    @Override public SkillDAO getSkillDAO() { return skillDAO; }
    @Override public RequestDAO getRequestDAO() { return requestDAO; }
    @Override public SessionDAO getSessionDAO() { return sessionDAO; }
    @Override public FeedbackDAO getFeedbackDAO() { return feedbackDAO; }

    private void seedDemoData() {
        userDAO.seedStudents();
    }
}
