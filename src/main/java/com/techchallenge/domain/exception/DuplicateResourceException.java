package com.techchallenge.domain.exception;

/**
 * Exceção lançada quando há tentativa de criar um recurso duplicado.
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
}

