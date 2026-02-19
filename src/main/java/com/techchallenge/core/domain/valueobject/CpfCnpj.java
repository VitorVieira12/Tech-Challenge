package com.techchallenge.core.domain.valueobject;

import com.techchallenge.core.domain.exception.DomainValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object para CPF/CNPJ - imutável e auto-validável
 */
@Getter
@EqualsAndHashCode
public class CpfCnpj {
    
    private final String valor;

    public CpfCnpj(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DomainValidationException("CPF/CNPJ não pode ser vazio");
        }

        String valorLimpo = valor.replaceAll("[^0-9]", "");
        
        if (!valorLimpo.matches("^\\d{11}$") && !valorLimpo.matches("^\\d{14}$")) {
            throw new DomainValidationException("CPF/CNPJ deve conter 11 ou 14 dígitos");
        }

        if (!validar(valorLimpo)) {
            throw new DomainValidationException("CPF/CNPJ inválido");
        }

        this.valor = valorLimpo;
    }

    private boolean validar(String valor) {
        if (valor.length() == 11) {
            return validarCPF(valor);
        } else {
            return validarCNPJ(valor);
        }
    }

    private boolean validarCPF(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = cpf.chars().map(c -> c - '0').toArray();

        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += digitos[i] * (10 - i);
        }
        int dig1 = 11 - (soma1 % 11);
        if (dig1 > 9) dig1 = 0;

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += digitos[i] * (11 - i);
        }
        int dig2 = 11 - (soma2 % 11);
        if (dig2 > 9) dig2 = 0;

        return digitos[9] == dig1 && digitos[10] == dig2;
    }

    private boolean validarCNPJ(String cnpj) {
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = cnpj.chars().map(c -> c - '0').toArray();
        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma1 = 0;
        for (int i = 0; i < 12; i++) {
            soma1 += digitos[i] * peso1[i];
        }
        int dig1 = soma1 % 11 < 2 ? 0 : 11 - (soma1 % 11);

        int soma2 = 0;
        for (int i = 0; i < 13; i++) {
            soma2 += digitos[i] * peso2[i];
        }
        int dig2 = soma2 % 11 < 2 ? 0 : 11 - (soma2 % 11);

        return digitos[12] == dig1 && digitos[13] == dig2;
    }

    public boolean isCPF() {
        return valor.length() == 11;
    }

    public boolean isCNPJ() {
        return valor.length() == 14;
    }

    @Override
    public String toString() {
        return valor;
    }
}

