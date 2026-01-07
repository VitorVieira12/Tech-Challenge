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
 * Value Object que representa um Contato (Email ou Telefone).
 * Valida o formato do contato fornecido.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor // Requerido pelo JPA
public class Contato implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Padrão para validação de email (RFC 5322 simplificado)
    private static final Pattern PATTERN_EMAIL = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Padrão para telefone brasileiro (com ou sem formatação)
    // Aceita: (11) 98888-7777, 11988887777, +5511988887777, etc
    private static final Pattern PATTERN_TELEFONE = Pattern.compile(
        "^(?:\\+?55)?\\s?\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$"
    );
    
    @Column(name = "contato", nullable = false)
    private String valor;
    
    @Column(name = "tipo_contato")
    private TipoContato tipo;
    
    public Contato(String valor) {
        validarEDefinirTipo(valor);
    }
    
    private void validarEDefinirTipo(String contato) {
        if (contato == null || contato.trim().isEmpty()) {
            throw new DomainValidationException("Contato não pode ser nulo ou vazio");
        }
        
        String contatoLimpo = contato.trim();
        
        if (isEmail(contatoLimpo)) {
            this.valor = contatoLimpo.toLowerCase();
            this.tipo = TipoContato.EMAIL;
        } else if (isTelefone(contatoLimpo)) {
            this.valor = limparTelefone(contatoLimpo);
            this.tipo = TipoContato.TELEFONE;
        } else {
            throw new DomainValidationException(
                "Contato inválido. Deve ser um email válido ou telefone brasileiro"
            );
        }
    }
    
    private boolean isEmail(String contato) {
        return PATTERN_EMAIL.matcher(contato).matches();
    }
    
    private boolean isTelefone(String contato) {
        return PATTERN_TELEFONE.matcher(contato).matches();
    }
    
    private String limparTelefone(String telefone) {
        // Remove todos os caracteres não numéricos exceto o + no início
        String limpo = telefone.replaceAll("[^0-9+]", "");
        
        // Se começar com +55, remove para padronizar
        if (limpo.startsWith("+55")) {
            limpo = limpo.substring(3);
        } else if (limpo.startsWith("55") && limpo.length() > 11) {
            limpo = limpo.substring(2);
        }
        
        return limpo;
    }
    
    public String getFormatado() {
        if (tipo == TipoContato.EMAIL) {
            return valor;
        } else {
            // Formata telefone brasileiro
            if (valor.length() == 11) {
                return String.format("(%s) %s-%s",
                        valor.substring(0, 2),
                        valor.substring(2, 7),
                        valor.substring(7, 11));
            } else if (valor.length() == 10) {
                return String.format("(%s) %s-%s",
                        valor.substring(0, 2),
                        valor.substring(2, 6),
                        valor.substring(6, 10));
            }
            return valor;
        }
    }
    
    public boolean isEmail() {
        return tipo == TipoContato.EMAIL;
    }
    
    public boolean isTelefone() {
        return tipo == TipoContato.TELEFONE;
    }
    
    @Override
    public String toString() {
        return valor;
    }
    
    public enum TipoContato {
        EMAIL,
        TELEFONE
    }
}

