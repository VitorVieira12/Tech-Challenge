package com.techchallenge.core.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object para Valor Monetário
 * Garante validações, imutabilidade e operações seguras
 */
@Getter
@EqualsAndHashCode
public class ValorMonetario {
    
    private final BigDecimal valor;

    public ValorMonetario(BigDecimal valor) {
        validar(valor);
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void validar(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor monetário não pode ser nulo");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor monetário não pode ser negativo");
        }
    }

    /**
     * Somar valores monetários
     */
    public ValorMonetario somar(ValorMonetario outro) {
        return new ValorMonetario(this.valor.add(outro.valor));
    }

    /**
     * Subtrair valores monetários
     */
    public ValorMonetario subtrair(ValorMonetario outro) {
        BigDecimal resultado = this.valor.subtract(outro.valor);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resultado de subtração não pode ser negativo");
        }
        return new ValorMonetario(resultado);
    }

    /**
     * Multiplicar por quantidade
     */
    public ValorMonetario multiplicar(Integer quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
        return new ValorMonetario(this.valor.multiply(BigDecimal.valueOf(quantidade)));
    }

    /**
     * Multiplicar por valor decimal
     */
    public ValorMonetario multiplicar(BigDecimal multiplicador) {
        return new ValorMonetario(this.valor.multiply(multiplicador));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}


