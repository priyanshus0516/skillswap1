package com.skillswap.exception;

/**
 * Base checked exception for all SkillSwap business-rule and runtime application errors.
 */
public class SkillSwapException extends Exception {

    public SkillSwapException(String message) {
        super(message);
    }

    public SkillSwapException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkillSwapException(Throwable cause) {
        super(cause);
    }
}
