package com.skillswap.dao;

import com.skillswap.dao.inmemory.InMemoryDAOFactory;
import com.skillswap.dao.jdbc.JdbcDAOFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory / Strategy class for obtaining DAO instances.
 * Dynamically switches between In-Memory storage (for zero-setup instant demo)
 * and MySQL/JDBC storage based on:
 *   1) JVM system property: -Dskillswap.storage=jdbc (or mysql)
 *   2) Configuration file: storage.properties (storage.type=jdbc / inmemory)
 */
public abstract class DAOFactory {

    private static volatile DAOFactory instance;

    public abstract UserDAO getUserDAO();
    public abstract SkillDAO getSkillDAO();
    public abstract RequestDAO getRequestDAO();
    public abstract SessionDAO getSessionDAO();
    public abstract FeedbackDAO getFeedbackDAO();

    public static DAOFactory getInstance() {
        if (instance == null) {
            synchronized (DAOFactory.class) {
                if (instance == null) {
                    instance = createFactory();
                }
            }
        }
        return instance;
    }

    public static void setInstance(DAOFactory customFactory) {
        synchronized (DAOFactory.class) {
            instance = customFactory;
        }
    }

    public static void reset() {
        synchronized (DAOFactory.class) {
            instance = null;
        }
    }

    private static DAOFactory createFactory() {
        // 1. Check system property: -Dskillswap.storage=jdbc / mysql
        String storageType = System.getProperty("skillswap.storage");

        // 2. Check storage.properties
        if (storageType == null || storageType.trim().isEmpty()) {
            Properties props = loadProperties();
            storageType = props.getProperty("storage.type", "inmemory");
        }

        storageType = storageType.trim().toLowerCase();
        if ("jdbc".equals(storageType) || "mysql".equals(storageType)) {
            System.out.println("[DAOFactory] Initializing MySQL JDBC DAO Layer...");
            return JdbcDAOFactory.getInstance();
        } else {
            System.out.println("[DAOFactory] Initializing In-Memory Seeded DAO Layer...");
            return InMemoryDAOFactory.getInstance();
        }
    }

    public static Properties loadProperties() {
        Properties props = new Properties();
        File file = new File("storage.properties");
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                return props;
            } catch (Exception ignored) { }
        }

        try (InputStream in = DAOFactory.class.getResourceAsStream("/storage.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) { }

        return props;
    }
}
