package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CpfCnpj - Value Object - Testes Unitários")
class CpfCnpjTest {

    @Test
    @DisplayName("Deve criar CPF válido")
    void deveCriarCpfValido() {
        // Arrange & Act
        CpfCnpj cpf = new CpfCnpj("111.444.777-35");
        
        // Assert
        assertThat(cpf.getValor()).isEqualTo("11144477735");
    }

    @Test
    @DisplayName("Deve criar CNPJ válido")
    void deveCriarCnpjValido() {
        // Arrange & Act
        CpfCnpj cnpj = new CpfCnpj("11.222.333/0001-81");
        
        // Assert
        assertThat(cnpj.getValor()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("Deve remover caracteres especiais ao criar CPF")
    void deveRemoverCaracteresEspeciaisAoCriarCpf() {
        // Arrange & Act
        CpfCnpj cpf = new CpfCnpj("111.444.777-35");
        
        // Assert
        assertThat(cpf.getValor()).doesNotContain(".", "-");
        assertThat(cpf.getValor()).hasSize(11);
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF nulo")
    void deveLancarExcecaoParaCpfNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj(null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF/CNPJ não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF vazio")
    void deveLancarExcecaoParaCpfVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj(""))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF/CNPJ não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF com todos dígitos iguais")
    void deveLancarExcecaoParaCpfComTodosDigitosIguais() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj("111.111.111-11"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF com dígito verificador inválido")
    void deveLancarExcecaoParaCpfComDigitoVerificadorInvalido() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj("123.456.789-00"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para CNPJ inválido")
    void deveLancarExcecaoParaCnpjInvalido() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj("11.222.333/0001-99"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CNPJ inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF/CNPJ com tamanho inválido")
    void deveLancarExcecaoParaTamanhoInvalido() {
        // Act & Assert
        assertThatThrownBy(() -> new CpfCnpj("123456"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF deve ter 11 dígitos e CNPJ deve ter 14 dígitos");
    }

    @Test
    @DisplayName("Deve considerar dois CPFs iguais como equals")
    void deveConsiderarDoisCpfsIguaisComoEquals() {
        // Arrange
        CpfCnpj cpf1 = new CpfCnpj("111.444.777-35");
        CpfCnpj cpf2 = new CpfCnpj("11144477735");
        
        // Assert
        assertThat(cpf1).isEqualTo(cpf2);
        assertThat(cpf1.hashCode()).isEqualTo(cpf2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois CPFs diferentes como não equals")
    void deveConsiderarDoisCpfsDiferentesComoNaoEquals() {
        // Arrange
        CpfCnpj cpf1 = new CpfCnpj("111.444.777-35");
        CpfCnpj cpf2 = new CpfCnpj("529.982.247-25");
        
        // Assert
        assertThat(cpf1).isNotEqualTo(cpf2);
    }
}


