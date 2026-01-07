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
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(new BigDecimal("100.50"));
        
        // Assert
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Deve criar valor monetário a partir de String")
    void deveCriarValorMonetarioAPartirDeString() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario("250.75");
        
        // Assert
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("250.75"));
    }

    @Test
    @DisplayName("Deve criar valor monetário a partir de double")
    void deveCriarValorMonetarioAPartirDeDouble() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(150.99);
        
        // Assert
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("150.99"));
    }

    @Test
    @DisplayName("Deve arredondar para 2 casas decimais")
    void deveArredondarParaDuasCasasDecimais() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(new BigDecimal("100.555"));
        
        // Assert
        assertThat(valor.getValor().scale()).isEqualTo(2);
        assertThat(valor.getValor()).isEqualByComparingTo(new BigDecimal("100.56"));
    }

    @Test
    @DisplayName("Deve somar dois valores monetários")
    void deveSomarDoisValoresMonetarios() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.50");
        ValorMonetario valor2 = new ValorMonetario("50.25");
        
        // Act
        ValorMonetario resultado = valor1.somar(valor2);
        
        // Assert
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("150.75"));
    }

    @Test
    @DisplayName("Deve subtrair dois valores monetários")
    void deveSubtrairDoisValoresMonetarios() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("30.00");
        
        // Act
        ValorMonetario resultado = valor1.subtrair(valor2);
        
        // Assert
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("Deve multiplicar valor monetário por inteiro")
    void deveMultiplicarValorMonetarioPorInteiro() {
        // Arrange
        ValorMonetario valor = new ValorMonetario("50.00");
        
        // Act
        ValorMonetario resultado = valor.multiplicar(3);
        
        // Assert
        assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve formatar valor monetário em formato brasileiro")
    void deveFormatarValorMonetarioEmFormatoBrasileiro() {
        // Arrange
        ValorMonetario valor = new ValorMonetario("1234.56");
        
        // Act
        String formatado = valor.getFormatado();
        
        // Assert
        assertThat(formatado).contains("1.234,56");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor nulo")
    void deveLancarExcecaoParaValorNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ValorMonetario((BigDecimal) null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Valor monetário não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void deveLancarExcecaoParaValorNegativo() {
        // Act & Assert
        assertThatThrownBy(() -> new ValorMonetario(new BigDecimal("-50.00")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Valor monetário não pode ser negativo");
    }

    @Test
    @DisplayName("Deve lançar exceção ao subtrair valor maior")
    void deveLancarExcecaoAoSubtrairValorMaior() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("50.00");
        ValorMonetario valor2 = new ValorMonetario("100.00");
        
        // Act & Assert
        assertThatThrownBy(() -> valor1.subtrair(valor2))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Resultado da subtração não pode ser negativo");
    }

    @Test
    @DisplayName("Deve verificar se valor é maior que outro")
    void deveVerificarSeValorEMaiorQueOutro() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("50.00");
        
        // Assert
        assertThat(valor1.isMaiorQue(valor2)).isTrue();
        assertThat(valor2.isMaiorQue(valor1)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se valor é zero")
    void deveVerificarSeValorEZero() {
        // Arrange
        ValorMonetario zero = new ValorMonetario(BigDecimal.ZERO);
        ValorMonetario naoZero = new ValorMonetario("10.00");
        
        // Assert
        assertThat(zero.isZero()).isTrue();
        assertThat(naoZero.isZero()).isFalse();
    }

    @Test
    @DisplayName("Deve comparar valores monetários corretamente")
    void deveCompararValoresMonetariosCorretamente() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("100.00");
        ValorMonetario valor3 = new ValorMonetario("50.00");
        
        // Assert
        assertThat(valor1).isEqualTo(valor2);
        assertThat(valor1).isNotEqualTo(valor3);
        assertThat(valor1.compareTo(valor3)).isGreaterThan(0);
    }
}

