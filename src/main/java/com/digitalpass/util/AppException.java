package com.digitalpass.util;

/**
 * Custom application exception for user-friendly error messages.
 *
 * OOP Concept: Exception Handling
 * - Subclasses Exception to provide clear, actionable feedback to users and callers.
 */
public class AppException extends Exception {

    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
