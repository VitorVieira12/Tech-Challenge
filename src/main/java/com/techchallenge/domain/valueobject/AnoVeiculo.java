package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Year;

/**
 * Value Object que representa o Ano de fabricação de um veículo.
 * Garante que o ano seja válido dentro de um range aceitável.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor // Requerido pelo JPA
public class AnoVeiculo implements Serializable, Comparable<AnoVeiculo> {
    
    private static final long serialVersionUID = 1L;
    private static final int ANO_MINIMO = 1900;
    private static final int ANO_MAXIMO_FUTURO = 1; // Permite até 1 ano no futuro
    
    @Column(name = "ano", nullable = false)
    private Integer valor;
    
    public AnoVeiculo(Integer valor) {
        this.valor = validar(valor);
    }
    
    private Integer validar(Integer ano) {
        if (ano == null) {
            throw new DomainValidationException("Ano do veículo não pode ser nulo");
        }
        
        if (ano < ANO_MINIMO) {
            throw new DomainValidationException(
                String.format("Ano do veículo não pode ser anterior a %d", ANO_MINIMO)
            );
        }
        
        int anoAtual = Year.now().getValue();
        int anoMaximo = anoAtual + ANO_MAXIMO_FUTURO;
        
        if (ano > anoMaximo) {
            throw new DomainValidationException(
                String.format("Ano do veículo não pode ser superior a %d", anoMaximo)
            );
        }
        
        return ano;
    }
    
    public int getIdadeEmAnos() {
        return Year.now().getValue() - valor;
    }
    
    public boolean isAnoModelo() {
        return valor == Year.now().getValue() || valor == Year.now().getValue() + 1;
    }
    
    public boolean isClassico() {
        return getIdadeEmAnos() >= 30;
    }
    
    @Override
    public int compareTo(AnoVeiculo outro) {
        return this.valor.compareTo(outro.valor);
    }
    
    @Override
    public String toString() {
        return valor.toString();
    }
}

