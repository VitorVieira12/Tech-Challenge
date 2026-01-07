package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Value Object que representa um Valor Monetário.
 * Garante que valores monetários sejam sempre válidos e não negativos.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor // Requerido pelo JPA
public class ValorMonetario implements Serializable, Comparable<ValorMonetario> {
    
    private static final long serialVersionUID = 1L;
    private static final int ESCALA_MONETARIA = 2;
    
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    public ValorMonetario(BigDecimal valor) {
        this.valor = validar(valor);
    }
    
    public ValorMonetario(String valor) {
        this(new BigDecimal(valor));
    }
    
    public ValorMonetario(double valor) {
        this(BigDecimal.valueOf(valor));
    }
    
    private BigDecimal validar(BigDecimal valor) {
        if (valor == null) {
            throw new DomainValidationException("Valor monetário não pode ser nulo");
        }
        
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Valor monetário não pode ser negativo");
        }
        
        // Valida a quantidade máxima de dígitos
        if (valor.precision() - valor.scale() > 8) {
            throw new DomainValidationException("Valor monetário excede o limite permitido");
        }
        
        return valor.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }
    
    public ValorMonetario somar(ValorMonetario outro) {
        if (outro == null) {
            throw new DomainValidationException("Não é possível somar com valor nulo");
        }
        return new ValorMonetario(this.valor.add(outro.valor));
    }
    
    public ValorMonetario subtrair(ValorMonetario outro) {
        if (outro == null) {
            throw new DomainValidationException("Não é possível subtrair valor nulo");
        }
        BigDecimal resultado = this.valor.subtract(outro.valor);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Resultado da subtração não pode ser negativo");
        }
        return new ValorMonetario(resultado);
    }
    
    public ValorMonetario multiplicar(BigDecimal fator) {
        if (fator == null) {
            throw new DomainValidationException("Fator de multiplicação não pode ser nulo");
        }
        if (fator.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Fator de multiplicação não pode ser negativo");
        }
        return new ValorMonetario(this.valor.multiply(fator));
    }
    
    public ValorMonetario multiplicar(int quantidade) {
        if (quantidade < 0) {
            throw new DomainValidationException("Quantidade não pode ser negativa");
        }
        return new ValorMonetario(this.valor.multiply(BigDecimal.valueOf(quantidade)));
    }
    
    public boolean isMaiorQue(ValorMonetario outro) {
        return this.valor.compareTo(outro.valor) > 0;
    }
    
    public boolean isMenorQue(ValorMonetario outro) {
        return this.valor.compareTo(outro.valor) < 0;
    }
    
    public boolean isZero() {
        return this.valor.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public String getFormatado() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formatter.format(valor);
    }
    
    @Override
    public int compareTo(ValorMonetario outro) {
        return this.valor.compareTo(outro.valor);
    }
    
    @Override
    public String toString() {
        return valor.toPlainString();
    }
}

