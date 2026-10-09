package com.skillswap.exception;

/**
 * Thrown when login credentials or user authentication fails.
 */
public class AuthenticationException extends SkillSwapException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
