package com.skillswap.exception;

/**
 * Thrown when registering with an email or registration number that already exists.
 */
public class DuplicateUserException extends SkillSwapException {

    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
