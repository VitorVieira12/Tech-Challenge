package com.techchallenge.domain.exception;

/**
 * Exceção lançada quando uma validação de regra de negócio no domínio falha.
 * Utilizada principalmente por Value Objects para garantir a integridade dos dados.
 */
public class DomainValidationException extends RuntimeException {
    
    public DomainValidationException(String message) {
        super(message);
    }
    
    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
