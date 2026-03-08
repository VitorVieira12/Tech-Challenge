package com.techchallenge.core.domain.valueobject;

import com.techchallenge.core.domain.exception.DomainValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object para Contato (email ou telefone) - imutável e auto-validável
 */
@Getter
@EqualsAndHashCode
public class Contato {
    
    private final String valor;

    public Contato(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DomainValidationException("Contato não pode ser vazio");
        }

        if (valor.length() < 8 || valor.length() > 100) {
            throw new DomainValidationException("Contato deve ter entre 8 e 100 caracteres");
        }

        this.valor = valor.trim();
    }

    @Override
    public String toString() {
        return valor;
    }
}


