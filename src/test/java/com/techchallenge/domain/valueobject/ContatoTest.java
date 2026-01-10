package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Contato - Value Object - Testes Unitários")
class ContatoTest {

    @Test
    @DisplayName("Deve criar contato com email válido")
    void deveCriarContatoComEmailValido() {
        Contato contato = new Contato("joao@email.com");
        
        assertThat(contato.getValor()).isEqualTo("joao@email.com");
        assertThat(contato.isEmail()).isTrue();
        assertThat(contato.isTelefone()).isFalse();
    }

    @Test
    @DisplayName("Deve criar contato com telefone válido")
    void deveCriarContatoComTelefoneValido() {
        Contato contato = new Contato("(11) 98765-4321");
        
        assertThat(contato.getValor()).isEqualTo("11987654321"); // Valor armazenado sem formatação
        assertThat(contato.getFormatado()).isEqualTo("(11) 98765-4321"); // Valor formatado
        assertThat(contato.isTelefone()).isTrue();
        assertThat(contato.isEmail()).isFalse();
    }

    @Test
    @DisplayName("Deve aceitar telefone sem formatação")
    void deveAceitarTelefoneSemFormatacao() {
        Contato contato = new Contato("11987654321");
        
        assertThat(contato.getValor()).isEqualTo("11987654321");
        assertThat(contato.isTelefone()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção para contato nulo")
    void deveLancarExcecaoParaContatoNulo() {
        assertThatThrownBy(() -> new Contato(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Contato não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção para contato vazio")
    void deveLancarExcecaoParaContatoVazio() {
        assertThatThrownBy(() -> new Contato(""))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Contato não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção para email inválido")
    void deveLancarExcecaoParaEmailInvalido() {
        assertThatThrownBy(() -> new Contato("email-invalido"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Contato inválido. Deve ser um email válido ou telefone brasileiro");
    }

    @Test
    @DisplayName("Deve lançar exceção para telefone inválido")
    void deveLancarExcecaoParaTelefoneInvalido() {
        assertThatThrownBy(() -> new Contato("123"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Contato inválido. Deve ser um email válido ou telefone brasileiro");
    }
}

