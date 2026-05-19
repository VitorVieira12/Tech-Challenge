package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AnoVeiculo - Value Object - Testes Unitários")
class AnoVeiculoTest {

    private static final int ANO_ATUAL = Year.now().getValue();

    @Test
    @DisplayName("Deve criar ano válido dentro do range")
    void deveCriarAnoValido() {
        AnoVeiculo ano = new AnoVeiculo(2020);

        assertThat(ano.getValor()).isEqualTo(2020);
    }

    @Test
    @DisplayName("Deve aceitar ano mínimo (1900)")
    void deveAceitarAnoMinimo() {
        AnoVeiculo ano = new AnoVeiculo(1900);

        assertThat(ano.getValor()).isEqualTo(1900);
    }

    @Test
    @DisplayName("Deve aceitar ano atual + 1 (modelo do próximo ano)")
    void deveAceitarAnoModeloFuturo() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL + 1);

        assertThat(ano.getValor()).isEqualTo(ANO_ATUAL + 1);
    }

    @Test
    @DisplayName("Deve lançar exceção para ano nulo")
    void deveLancarExcecaoParaAnoNulo() {
        assertThatThrownBy(() -> new AnoVeiculo(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para ano anterior a 1900")
    void deveLancarExcecaoParaAnoAnteriorA1900() {
        assertThatThrownBy(() -> new AnoVeiculo(1899))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("1900");
    }

    @Test
    @DisplayName("Deve lançar exceção para ano muito futuro")
    void deveLancarExcecaoParaAnoMuitoFuturo() {
        assertThatThrownBy(() -> new AnoVeiculo(ANO_ATUAL + 5))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("não pode ser superior");
    }

    @Test
    @DisplayName("Deve calcular idade em anos corretamente")
    void deveCalcularIdadeEmAnos() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL - 10);

        assertThat(ano.getIdadeEmAnos()).isEqualTo(10);
    }

    @Test
    @DisplayName("isAnoModelo deve retornar true para ano atual")
    void anoAtualEhAnoModelo() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL);

        assertThat(ano.isAnoModelo()).isTrue();
    }

    @Test
    @DisplayName("isAnoModelo deve retornar true para próximo ano")
    void proximoAnoEhAnoModelo() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL + 1);

        assertThat(ano.isAnoModelo()).isTrue();
    }

    @Test
    @DisplayName("isAnoModelo deve retornar false para ano antigo")
    void anoAntigoNaoEhAnoModelo() {
        AnoVeiculo ano = new AnoVeiculo(2010);

        assertThat(ano.isAnoModelo()).isFalse();
    }

    @Test
    @DisplayName("isClassico deve retornar true para carro com 30+ anos")
    void carroComMaisDe30AnosEhClassico() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL - 30);

        assertThat(ano.isClassico()).isTrue();
    }

    @Test
    @DisplayName("isClassico deve retornar false para carro recente")
    void carroRecenteNaoEhClassico() {
        AnoVeiculo ano = new AnoVeiculo(ANO_ATUAL - 5);

        assertThat(ano.isClassico()).isFalse();
    }

    @Test
    @DisplayName("compareTo deve ordenar anos corretamente")
    void deveOrdenarAnosCorretamente() {
        AnoVeiculo anterior = new AnoVeiculo(2010);
        AnoVeiculo posterior = new AnoVeiculo(2020);

        assertThat(anterior.compareTo(posterior)).isNegative();
        assertThat(posterior.compareTo(anterior)).isPositive();
        assertThat(anterior.compareTo(new AnoVeiculo(2010))).isZero();
    }

    @Test
    @DisplayName("toString deve retornar o ano como string")
    void toStringDeveRetornarAnoComoString() {
        AnoVeiculo ano = new AnoVeiculo(2020);

        assertThat(ano.toString()).isEqualTo("2020");
    }

    @Test
    @DisplayName("equals e hashCode devem funcionar")
    void equalsHashCodeDevemFuncionar() {
        AnoVeiculo a = new AnoVeiculo(2020);
        AnoVeiculo b = new AnoVeiculo(2020);
        AnoVeiculo c = new AnoVeiculo(2019);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
