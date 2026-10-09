package com.skillswap.exception;

/**
 * Checked exception wrapping persistence and data access failures (e.g. SQLException,
 * connection drops, storage config errors). Keeps SQL and database implementation
 * details decoupled from the service and presentation (GUI) layers.
 */
public class DataAccessException extends SkillSwapException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
