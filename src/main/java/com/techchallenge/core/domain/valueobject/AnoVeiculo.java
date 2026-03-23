package com.techchallenge.core.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Year;

/**
 * Value Object para Ano do Veículo
 * Garante validações e imutabilidade
 */
@Getter
@EqualsAndHashCode
public class AnoVeiculo {
    
    private final Integer valor;

    public AnoVeiculo(Integer valor) {
        validar(valor);
        this.valor = valor;
    }

    private void validar(Integer valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Ano do veículo não pode ser nulo");
        }
        
        int anoMinimo = 1900;
        int anoMaximo = Year.now().getValue() + 1; // Permite ano-modelo seguinte
        
        if (valor < anoMinimo || valor > anoMaximo) {
            throw new IllegalArgumentException(
                String.format("Ano do veículo deve estar entre %d e %d", anoMinimo, anoMaximo)
            );
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}


