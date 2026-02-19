package com.techchallenge.core.usecase.exception;

/**
 * Exceção para recurso duplicado (use case layer)
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}

