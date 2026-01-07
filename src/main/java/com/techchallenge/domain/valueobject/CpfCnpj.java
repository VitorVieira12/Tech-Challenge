package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Value Object que representa um CPF ou CNPJ válido.
 * Garante a integridade através de validações no construtor.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor // Requerido pelo JPA
public class CpfCnpj implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 14)
    private String valor;
    
    public CpfCnpj(String valor) {
        this.valor = validar(valor);
    }
    
    private String validar(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new DomainValidationException("CPF/CNPJ não pode ser nulo ou vazio");
        }
        
        // Remove caracteres não numéricos
        String documentoLimpo = documento.replaceAll("[^0-9]", "");
        
        if (documentoLimpo.length() == 11) {
            validarCpf(documentoLimpo);
        } else if (documentoLimpo.length() == 14) {
            validarCnpj(documentoLimpo);
        } else {
            throw new DomainValidationException("CPF deve ter 11 dígitos e CNPJ deve ter 14 dígitos");
        }
        
        return documentoLimpo;
    }
    
    private void validarCpf(String cpf) {
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            throw new DomainValidationException("CPF inválido");
        }
        
        // Valida primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        
        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            throw new DomainValidationException("CPF inválido");
        }
        
        // Valida segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        
        if (segundoDigito != Character.getNumericValue(cpf.charAt(10))) {
            throw new DomainValidationException("CPF inválido");
        }
    }
    
    private void validarCnpj(String cnpj) {
        // Verifica se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) {
            throw new DomainValidationException("CNPJ inválido");
        }
        
        // Valida primeiro dígito verificador
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesos1[i];
        }
        int primeiroDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        
        if (primeiroDigito != Character.getNumericValue(cnpj.charAt(12))) {
            throw new DomainValidationException("CNPJ inválido");
        }
        
        // Valida segundo dígito verificador
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesos2[i];
        }
        int segundoDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        
        if (segundoDigito != Character.getNumericValue(cnpj.charAt(13))) {
            throw new DomainValidationException("CNPJ inválido");
        }
    }
    
    public String getFormatado() {
        if (valor.length() == 11) {
            return String.format("%s.%s.%s-%s",
                    valor.substring(0, 3),
                    valor.substring(3, 6),
                    valor.substring(6, 9),
                    valor.substring(9, 11));
        } else {
            return String.format("%s.%s.%s/%s-%s",
                    valor.substring(0, 2),
                    valor.substring(2, 5),
                    valor.substring(5, 8),
                    valor.substring(8, 12),
                    valor.substring(12, 14));
        }
    }
    
    @Override
    public String toString() {
        return valor;
    }
}
