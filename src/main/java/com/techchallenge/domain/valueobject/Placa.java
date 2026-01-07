package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object que representa uma Placa de Veículo brasileira válida.
 * Suporta formato antigo (ABC1234) e Mercosul (ABC1D23).
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor // Requerido pelo JPA
public class Placa implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Padrão formato antigo: ABC1234
    private static final Pattern FORMATO_ANTIGO = Pattern.compile("^[A-Z]{3}\\d{4}$");
    
    // Padrão formato Mercosul: ABC1D23
    private static final Pattern FORMATO_MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");
    
    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String valor;
    
    public Placa(String valor) {
        this.valor = validar(valor);
    }
    
    private String validar(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new DomainValidationException("Placa não pode ser nula ou vazia");
        }
        
        // Converte para maiúsculas e remove espaços
        String placaLimpa = placa.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        
        if (!FORMATO_ANTIGO.matcher(placaLimpa).matches() && 
            !FORMATO_MERCOSUL.matcher(placaLimpa).matches()) {
            throw new DomainValidationException(
                "Placa deve seguir o formato brasileiro: ABC1234 (antigo) ou ABC1D23 (Mercosul)"
            );
        }
        
        return placaLimpa;
    }
    
    public String getFormatado() {
        if (isMercosul()) {
            return String.format("%s-%s",
                    valor.substring(0, 3),
                    valor.substring(3, 7));
        } else {
            return String.format("%s-%s",
                    valor.substring(0, 3),
                    valor.substring(3, 7));
        }
    }
    
    public boolean isMercosul() {
        return FORMATO_MERCOSUL.matcher(valor).matches();
    }
    
    public boolean isFormatoAntigo() {
        return FORMATO_ANTIGO.matcher(valor).matches();
    }
    
    @Override
    public String toString() {
        return valor;
    }
}
