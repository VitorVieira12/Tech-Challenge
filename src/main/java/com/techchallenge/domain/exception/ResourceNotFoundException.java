package com.techchallenge.domain.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s com ID %d não encontrado", resourceName, id));
    }
}

