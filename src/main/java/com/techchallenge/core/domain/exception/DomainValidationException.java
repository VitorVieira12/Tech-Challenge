package com.techchallenge.core.domain.exception;

/**
 * Exceção de domínio para violações de regras de negócio
 */
public class DomainValidationException extends RuntimeException {
    
    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


