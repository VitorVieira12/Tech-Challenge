package com.techchallenge.core.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object para Placa de Veículo
 * Suporta formato antigo (ABC1234) e Mercosul (ABC1D23)
 */
@Getter
@EqualsAndHashCode
public class Placa {
    
    private final String valor;

    public Placa(String valor) {
        validar(valor);
        this.valor = valor.toUpperCase();
    }

    private void validar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Placa não pode ser nula ou vazia");
        }

        String placaLimpa = valor.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        
        if (placaLimpa.length() != 7) {
            throw new IllegalArgumentException("Placa deve conter 7 caracteres");
        }

        // Formato antigo: ABC1234 (3 letras + 4 números)
        boolean formatoAntigo = placaLimpa.matches("[A-Z]{3}\\d{4}");
        
        // Formato Mercosul: ABC1D23 (3 letras + 1 número + 1 letra + 2 números)
        boolean formatoMercosul = placaLimpa.matches("[A-Z]{3}\\d[A-Z]\\d{2}");

        if (!formatoAntigo && !formatoMercosul) {
            throw new IllegalArgumentException(
                "Placa inválida. Formatos aceitos: ABC1234 (antigo) ou ABC1D23 (Mercosul)"
            );
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}


