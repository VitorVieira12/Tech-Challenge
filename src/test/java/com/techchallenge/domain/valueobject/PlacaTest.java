package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Placa - Value Object - Testes Unitários")
class PlacaTest {

    @Test
    @DisplayName("Deve criar placa antiga válida (ABC1234)")
    void deveCriarPlacaAntigaValida() {
        Placa placa = new Placa("ABC1234");
        
        assertThat(placa.getValor()).isEqualTo("ABC1234");
    }

    @Test
    @DisplayName("Deve criar placa Mercosul válida (ABC1D23)")
    void deveCriarPlacaMercosulValida() {
        Placa placa = new Placa("ABC1D23");
        
        assertThat(placa.getValor()).isEqualTo("ABC1D23");
    }

    @Test
    @DisplayName("Deve converter placa para maiúsculo")
    void deveConverterPlacaParaMaiusculo() {
        Placa placa = new Placa("abc1234");
        
        assertThat(placa.getValor()).isEqualTo("ABC1234");
    }

    @Test
    @DisplayName("Deve remover caracteres especiais da placa")
    void deveRemoverCaracteresEspeciaisDaPlaca() {
        Placa placa = new Placa("ABC-1234");
        
        assertThat(placa.getValor()).isEqualTo("ABC1234");
        assertThat(placa.getValor()).doesNotContain("-");
    }

    @Test
    @DisplayName("Deve lançar exceção para placa nula")
    void deveLancarExcecaoParaPlacaNula() {
        assertThatThrownBy(() -> new Placa(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Placa não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção para placa vazia")
    void deveLancarExcecaoParaPlacaVazia() {
        assertThatThrownBy(() -> new Placa(""))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Placa não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção para placa com formato inválido")
    void deveLancarExcecaoParaPlacaComFormatoInvalido() {
        assertThatThrownBy(() -> new Placa("ABCD1234"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Placa deve seguir o formato brasileiro: ABC1234 (antigo) ou ABC1D23 (Mercosul)");
    }

    @Test
    @DisplayName("Deve lançar exceção para placa com números no lugar errado")
    void deveLancarExcecaoParaPlacaComNumerosNoLugarErrado() {
        assertThatThrownBy(() -> new Placa("123ABCD"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Placa deve seguir o formato brasileiro: ABC1234 (antigo) ou ABC1D23 (Mercosul)");
    }
}

