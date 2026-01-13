package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValorMonetario - Value Object - Testes Unitários")
class ValorMonetarioTest {

    @Test
    @DisplayName("Deve criar valor monetário válido")
    void deveCriarValorMonetarioValido() {
        ValorMonetario valor = new ValorMonetario(new BigDecimal("100.50"));
        
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Deve criar valor monetário a partir de String")
    void deveCriarValorMonetarioAPartirDeString() {
        ValorMonetario valor = new ValorMonetario("250.75");
        
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("250.75"));
    }

    @Test
    @DisplayName("Deve criar valor monetário a partir de double")
    void deveCriarValorMonetarioAPartirDeDouble() {
        ValorMonetario valor = new ValorMonetario(150.99);
        
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("150.99"));
    }

    @Test
    @DisplayName("Deve arredondar para 2 casas decimais")
    void deveArredondarParaDuasCasasDecimais() {
        ValorMonetario valor = new ValorMonetario(new BigDecimal("100.555"));
        
        assertThat(valor.getValor().scale()).isEqualTo(2);
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("100.56"));
    }

    @Test
    @DisplayName("Deve somar dois valores monetários")
    void deveSomarDoisValoresMonetarios() {
        ValorMonetario valor1 = new ValorMonetario("100.50");
        ValorMonetario valor2 = new ValorMonetario("50.25");
        
        ValorMonetario resultado = valor1.somar(valor2);
        
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("150.75"));
    }

    @Test
    @DisplayName("Deve subtrair dois valores monetários")
    void deveSubtrairDoisValoresMonetarios() {
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("30.00");
        
        ValorMonetario resultado = valor1.subtrair(valor2);
        
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("Deve multiplicar valor monetário por inteiro")
    void deveMultiplicarValorMonetarioPorInteiro() {
        ValorMonetario valor = new ValorMonetario("50.00");
        
        ValorMonetario resultado = valor.multiplicar(3);
        
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve formatar valor monetário em formato brasileiro")
    void deveFormatarValorMonetarioEmFormatoBrasileiro() {
        ValorMonetario valor = new ValorMonetario("1234.56");
        
        String formatado = valor.getFormatado();
        
        assertThat(formatado).contains("1.234,56");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor nulo")
    void deveLancarExcecaoParaValorNulo() {
        assertThatThrownBy(() -> new ValorMonetario((BigDecimal) null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Valor monetário não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void deveLancarExcecaoParaValorNegativo() {
        assertThatThrownBy(() -> new ValorMonetario(new BigDecimal("-50.00")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Valor monetário não pode ser negativo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao subtrair valor maior")
    void deveLancarExcecaoAoSubtrairValorMaior() {
        ValorMonetario valor1 = new ValorMonetario("50.00");
        ValorMonetario valor2 = new ValorMonetario("100.00");
        
        assertThatThrownBy(() -> valor1.subtrair(valor2))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Resultado da subtração não pode ser negativo");
    }

    @Test
    @DisplayName("Deve verificar se valor é maior que outro")
    void deveVerificarSeValorEMaiorQueOutro() {
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("50.00");
        
        assertThat(valor1.isMaiorQue(valor2)).isTrue();
        assertThat(valor2.isMaiorQue(valor1)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se valor é zero")
    void deveVerificarSeValorEZero() {
        ValorMonetario zero = new ValorMonetario(BigDecimal.ZERO);
        ValorMonetario naoZero = new ValorMonetario("10.00");
        
        assertThat(zero.isZero()).isTrue();
        assertThat(naoZero.isZero()).isFalse();
    }

    @Test
    @DisplayName("Deve comparar valores monetários corretamente")
    void deveCompararValoresMonetariosCorretamente() {
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("100.00");
        ValorMonetario valor3 = new ValorMonetario("50.00");
        
        assertThat(valor1).isEqualTo(valor2);
        assertThat(valor1).isNotEqualTo(valor3);
        assertThat(valor1.compareTo(valor3)).isGreaterThan(0);
    }
}


