package com.skillswap.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA-256 password hashing.
 * Ensures plain-text passwords are never stored in memory or databases.
 */
public final class PasswordUtil {

    private PasswordUtil() { } // Utility class, prevent instantiation

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            rawPassword = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(storedHash);
    }
}
