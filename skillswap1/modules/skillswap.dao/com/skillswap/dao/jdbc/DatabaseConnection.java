package com.skillswap.dao.jdbc;

import com.skillswap.dao.DAOFactory;
import com.skillswap.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC connections to MySQL database using configuration from storage.properties.
 */
public final class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;
    private static String driver;
    private static boolean driverLoaded = false;

    private DatabaseConnection() { }

    private static synchronized void initConfig() {
        if (url != null) return;
        Properties props = DAOFactory.loadProperties();
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/skillswap_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        user = props.getProperty("db.user", "root");
        password = props.getProperty("db.password", "rootpassword");
        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL driver class not found: " + driver);
            driverLoaded = false;
        }
    }

    public static Connection getConnection() throws DataAccessException {
        initConfig();
        if (!driverLoaded) {
            try {
                Class.forName(driver);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new DataAccessException("MySQL JDBC driver (" + driver + ") not found in classpath. "
                        + "Please ensure mysql-connector-j.jar is added to the module path.", e);
            }
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to connect to MySQL database at " + url + ": " + e.getMessage(), e);
        }
    }
}
