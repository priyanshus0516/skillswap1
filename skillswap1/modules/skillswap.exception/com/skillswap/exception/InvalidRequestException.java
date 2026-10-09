package com.skillswap.exception;

/**
 * Thrown when an exchange request or session violates a business rule
 * (e.g. sending a request to oneself, or acting on an unpermitted status).
 */
public class InvalidRequestException extends SkillSwapException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
