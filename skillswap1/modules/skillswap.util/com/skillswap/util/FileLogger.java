package com.skillswap.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Appends significant user events (registration, login, requests, sessions, feedback)
 * to an activity log file on disk. Fulfills the file handling requirement.
 */
public final class FileLogger {

    private static final String LOG_FILE = "skillswap_activity.log";

    private FileLogger() { }

    public static synchronized void log(String action) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("[" + LocalDateTime.now() + "] " + action);
        } catch (IOException e) {
            // Logging failure should never crash the core application
            System.err.println("FileLogger error: " + e.getMessage());
        }
    }
}
