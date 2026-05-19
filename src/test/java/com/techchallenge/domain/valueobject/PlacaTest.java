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

    @Test
    @DisplayName("Deve lançar exceção para placa só com espaços")
    void deveLancarExcecaoParaPlacaSoComEspacos() {
        assertThatThrownBy(() -> new Placa("   "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("isFormatoAntigo deve identificar corretamente placa ABC1234")
    void deveIdentificarFormatoAntigo() {
        Placa placa = new Placa("ABC1234");

        assertThat(placa.isFormatoAntigo()).isTrue();
        assertThat(placa.isMercosul()).isFalse();
    }

    @Test
    @DisplayName("isMercosul deve identificar corretamente placa ABC1D23")
    void deveIdentificarFormatoMercosul() {
        Placa placa = new Placa("ABC1D23");

        assertThat(placa.isMercosul()).isTrue();
        assertThat(placa.isFormatoAntigo()).isFalse();
    }

    @Test
    @DisplayName("getFormatado deve retornar placa formatada com hífen (antigo)")
    void getFormatadoDeveAdicionarHifenAntigo() {
        Placa placa = new Placa("ABC1234");

        assertThat(placa.getFormatado()).isEqualTo("ABC-1234");
    }

    @Test
    @DisplayName("getFormatado deve retornar placa formatada com hífen (Mercosul)")
    void getFormatadoDeveAdicionarHifenMercosul() {
        Placa placa = new Placa("ABC1D23");

        assertThat(placa.getFormatado()).isEqualTo("ABC-1D23");
    }

    @Test
    @DisplayName("toString deve retornar o valor da placa")
    void toStringDeveRetornarValor() {
        Placa placa = new Placa("ABC1234");

        assertThat(placa.toString()).isEqualTo("ABC1234");
    }

    @Test
    @DisplayName("equals e hashCode devem funcionar")
    void equalsEHashCodeDevemFuncionar() {
        Placa a = new Placa("ABC1234");
        Placa b = new Placa("ABC1234");
        Placa c = new Placa("XYZ9876");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}

