# Exemplos de Testes para Value Objects

Este documento fornece exemplos de testes unitários para os Value Objects criados na refatoração.

## 🧪 Estrutura de Testes

Os testes devem ser criados em: `src/test/java/com/techchallenge/domain/valueobject/`

---

## 1. Testes para CpfCnpj

```java
package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CpfCnpjTest {

    @Test
    void deveCriarCpfValido() {
        // Arrange & Act
        CpfCnpj cpf = new CpfCnpj("12345678909");
        
        // Assert
        assertEquals("12345678909", cpf.getValor());
    }

    @Test
    void deveCriarCnpjValido() {
        // Arrange & Act
        CpfCnpj cnpj = new CpfCnpj("11222333000181");
        
        // Assert
        assertEquals("11222333000181", cnpj.getValor());
    }

    @Test
    void deveRemoverFormatacaoDoCpf() {
        // Arrange & Act
        CpfCnpj cpf = new CpfCnpj("123.456.789-09");
        
        // Assert
        assertEquals("12345678909", cpf.getValor());
    }

    @Test
    void deveFormatarCpfCorretamente() {
        // Arrange
        CpfCnpj cpf = new CpfCnpj("12345678909");
        
        // Act
        String formatado = cpf.getFormatado();
        
        // Assert
        assertEquals("123.456.789-09", formatado);
    }

    @Test
    void deveFormatarCnpjCorretamente() {
        // Arrange
        CpfCnpj cnpj = new CpfCnpj("11222333000181");
        
        // Act
        String formatado = cnpj.getFormatado();
        
        // Assert
        assertEquals("11.222.333/0001-81", formatado);
    }

    @Test
    void deveLancarExcecaoParaCpfNulo() {
        // Act & Assert
        DomainValidationException exception = assertThrows(
            DomainValidationException.class,
            () -> new CpfCnpj(null)
        );
        
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    @Test
    void deveLancarExcecaoParaCpfVazio() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new CpfCnpj("")
        );
    }

    @Test
    void deveLancarExcecaoParaCpfComTamanhoInvalido() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new CpfCnpj("123456789")
        );
    }

    @Test
    void deveLancarExcecaoParaCpfComDigitosIguais() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new CpfCnpj("11111111111")
        );
    }

    @Test
    void deveLancarExcecaoParaCpfComDigitoVerificadorInvalido() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new CpfCnpj("12345678900") // Dígito verificador incorreto
        );
    }
}
```

---

## 2. Testes para Placa

```java
package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacaTest {

    @Test
    void deveCriarPlacaFormatoAntigo() {
        // Arrange & Act
        Placa placa = new Placa("ABC1234");
        
        // Assert
        assertEquals("ABC1234", placa.getValor());
        assertTrue(placa.isFormatoAntigo());
        assertFalse(placa.isMercosul());
    }

    @Test
    void deveCriarPlacaFormatoMercosul() {
        // Arrange & Act
        Placa placa = new Placa("ABC1D23");
        
        // Assert
        assertEquals("ABC1D23", placa.getValor());
        assertTrue(placa.isMercosul());
        assertFalse(placa.isFormatoAntigo());
    }

    @Test
    void deveConverterParaMaiusculas() {
        // Arrange & Act
        Placa placa = new Placa("abc1234");
        
        // Assert
        assertEquals("ABC1234", placa.getValor());
    }

    @Test
    void deveRemoverCaracteresEspeciais() {
        // Arrange & Act
        Placa placa = new Placa("ABC-1234");
        
        // Assert
        assertEquals("ABC1234", placa.getValor());
    }

    @Test
    void deveFormatarPlaca() {
        // Arrange
        Placa placa = new Placa("ABC1234");
        
        // Act
        String formatada = placa.getFormatado();
        
        // Assert
        assertEquals("ABC-1234", formatada);
    }

    @Test
    void deveLancarExcecaoParaPlacaNula() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Placa(null)
        );
    }

    @Test
    void deveLancarExcecaoParaPlacaVazia() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Placa("")
        );
    }

    @Test
    void deveLancarExcecaoParaPlacaComFormatoInvalido() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Placa("1234ABC")
        );
    }
}
```

---

## 3. Testes para ValorMonetario

```java
package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ValorMonetarioTest {

    @Test
    void deveCriarValorMonetarioValido() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(new BigDecimal("99.99"));
        
        // Assert
        assertEquals(new BigDecimal("99.99"), valor.getValor());
    }

    @Test
    void deveCriarValorMonetarioAPartirDeString() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario("150.50");
        
        // Assert
        assertEquals(new BigDecimal("150.50"), valor.getValor());
    }

    @Test
    void deveCriarValorMonetarioAPartirDeDouble() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(99.99);
        
        // Assert
        assertEquals(new BigDecimal("99.99"), valor.getValor());
    }

    @Test
    void deveArredondarParaDuasCasasDecimais() {
        // Arrange & Act
        ValorMonetario valor = new ValorMonetario(new BigDecimal("99.999"));
        
        // Assert
        assertEquals(new BigDecimal("100.00"), valor.getValor());
    }

    @Test
    void deveSomarValores() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("50.50");
        
        // Act
        ValorMonetario resultado = valor1.somar(valor2);
        
        // Assert
        assertEquals(new BigDecimal("150.50"), resultado.getValor());
    }

    @Test
    void deveSubtrairValores() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("100.00");
        ValorMonetario valor2 = new ValorMonetario("30.00");
        
        // Act
        ValorMonetario resultado = valor1.subtrair(valor2);
        
        // Assert
        assertEquals(new BigDecimal("70.00"), resultado.getValor());
    }

    @Test
    void deveMultiplicarPorFator() {
        // Arrange
        ValorMonetario valor = new ValorMonetario("50.00");
        
        // Act
        ValorMonetario resultado = valor.multiplicar(new BigDecimal("2.5"));
        
        // Assert
        assertEquals(new BigDecimal("125.00"), resultado.getValor());
    }

    @Test
    void deveMultiplicarPorQuantidade() {
        // Arrange
        ValorMonetario valor = new ValorMonetario("25.00");
        
        // Act
        ValorMonetario resultado = valor.multiplicar(4);
        
        // Assert
        assertEquals(new BigDecimal("100.00"), resultado.getValor());
    }

    @Test
    void deveCompararValores() {
        // Arrange
        ValorMonetario maior = new ValorMonetario("100.00");
        ValorMonetario menor = new ValorMonetario("50.00");
        
        // Assert
        assertTrue(maior.isMaiorQue(menor));
        assertTrue(menor.isMenorQue(maior));
        assertFalse(maior.isMenorQue(menor));
    }

    @Test
    void deveVerificarSeEhZero() {
        // Arrange
        ValorMonetario zero = new ValorMonetario("0.00");
        ValorMonetario naoZero = new ValorMonetario("10.00");
        
        // Assert
        assertTrue(zero.isZero());
        assertFalse(naoZero.isZero());
    }

    @Test
    void deveFormatarComoMoeda() {
        // Arrange
        ValorMonetario valor = new ValorMonetario("1234.56");
        
        // Act
        String formatado = valor.getFormatado();
        
        // Assert
        assertTrue(formatado.contains("1.234,56") || formatado.contains("1234.56"));
    }

    @Test
    void deveLancarExcecaoParaValorNulo() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new ValorMonetario((BigDecimal) null)
        );
    }

    @Test
    void deveLancarExcecaoParaValorNegativo() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new ValorMonetario("-10.00")
        );
    }

    @Test
    void deveLancarExcecaoParaSubtracaoComResultadoNegativo() {
        // Arrange
        ValorMonetario valor1 = new ValorMonetario("50.00");
        ValorMonetario valor2 = new ValorMonetario("100.00");
        
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> valor1.subtrair(valor2)
        );
    }
}
```

---

## 4. Testes para AnoVeiculo

```java
package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import java.time.Year;
import static org.junit.jupiter.api.Assertions.*;

class AnoVeiculoTest {

    @Test
    void deveCriarAnoVeiculoValido() {
        // Arrange & Act
        AnoVeiculo ano = new AnoVeiculo(2020);
        
        // Assert
        assertEquals(2020, ano.getValor());
    }

    @Test
    void deveCalcularIdadeDoVeiculo() {
        // Arrange
        int anoAtual = Year.now().getValue();
        AnoVeiculo ano = new AnoVeiculo(anoAtual - 5);
        
        // Act
        int idade = ano.getIdadeEmAnos();
        
        // Assert
        assertEquals(5, idade);
    }

    @Test
    void deveIdentificarAnoModelo() {
        // Arrange
        int anoAtual = Year.now().getValue();
        AnoVeiculo anoAtualVeiculo = new AnoVeiculo(anoAtual);
        AnoVeiculo proximoAno = new AnoVeiculo(anoAtual + 1);
        AnoVeiculo anoPassado = new AnoVeiculo(anoAtual - 1);
        
        // Assert
        assertTrue(anoAtualVeiculo.isAnoModelo());
        assertTrue(proximoAno.isAnoModelo());
        assertFalse(anoPassado.isAnoModelo());
    }

    @Test
    void deveIdentificarVeiculoClassico() {
        // Arrange
        int anoAtual = Year.now().getValue();
        AnoVeiculo classico = new AnoVeiculo(anoAtual - 30);
        AnoVeiculo naoClassico = new AnoVeiculo(anoAtual - 10);
        
        // Assert
        assertTrue(classico.isClassico());
        assertFalse(naoClassico.isClassico());
    }

    @Test
    void deveLancarExcecaoParaAnoNulo() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new AnoVeiculo(null)
        );
    }

    @Test
    void deveLancarExcecaoParaAnoAnteriorA1900() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new AnoVeiculo(1899)
        );
    }

    @Test
    void deveLancarExcecaoParaAnoMuitoFuturo() {
        // Arrange
        int anoMuitoFuturo = Year.now().getValue() + 10;
        
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new AnoVeiculo(anoMuitoFuturo)
        );
    }

    @Test
    void deveCompararAnos() {
        // Arrange
        AnoVeiculo ano2020 = new AnoVeiculo(2020);
        AnoVeiculo ano2021 = new AnoVeiculo(2021);
        
        // Assert
        assertTrue(ano2021.compareTo(ano2020) > 0);
        assertTrue(ano2020.compareTo(ano2021) < 0);
        assertEquals(0, ano2020.compareTo(new AnoVeiculo(2020)));
    }
}
```

---

## 5. Testes para Contato

```java
package com.techchallenge.domain.valueobject;

import com.techchallenge.domain.exception.DomainValidationException;
import com.techchallenge.domain.valueobject.Contato.TipoContato;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContatoTest {

    @Test
    void deveCriarContatoComEmail() {
        // Arrange & Act
        Contato contato = new Contato("usuario@exemplo.com");
        
        // Assert
        assertEquals("usuario@exemplo.com", contato.getValor());
        assertEquals(TipoContato.EMAIL, contato.getTipo());
        assertTrue(contato.isEmail());
        assertFalse(contato.isTelefone());
    }

    @Test
    void deveCriarContatoComTelefone() {
        // Arrange & Act
        Contato contato = new Contato("11988887777");
        
        // Assert
        assertEquals("11988887777", contato.getValor());
        assertEquals(TipoContato.TELEFONE, contato.getTipo());
        assertTrue(contato.isTelefone());
        assertFalse(contato.isEmail());
    }

    @Test
    void deveNormalizarTelefoneComFormatacao() {
        // Arrange & Act
        Contato contato = new Contato("(11) 98888-7777");
        
        // Assert
        assertEquals("11988887777", contato.getValor());
    }

    @Test
    void deveRemoverCodigoPaisDeTelefone() {
        // Arrange & Act
        Contato contato = new Contato("+5511988887777");
        
        // Assert
        assertEquals("11988887777", contato.getValor());
    }

    @Test
    void deveFormatarTelefone() {
        // Arrange
        Contato contato = new Contato("11988887777");
        
        // Act
        String formatado = contato.getFormatado();
        
        // Assert
        assertEquals("(11) 98888-7777", formatado);
    }

    @Test
    void deveFormatarTelefoneFixo() {
        // Arrange
        Contato contato = new Contato("1133334444");
        
        // Act
        String formatado = contato.getFormatado();
        
        // Assert
        assertEquals("(11) 3333-4444", formatado);
    }

    @Test
    void deveManterEmailSemFormatacao() {
        // Arrange
        Contato contato = new Contato("usuario@exemplo.com");
        
        // Act
        String formatado = contato.getFormatado();
        
        // Assert
        assertEquals("usuario@exemplo.com", formatado);
    }

    @Test
    void deveConverterEmailParaMinusculas() {
        // Arrange & Act
        Contato contato = new Contato("USUARIO@EXEMPLO.COM");
        
        // Assert
        assertEquals("usuario@exemplo.com", contato.getValor());
    }

    @Test
    void deveLancarExcecaoParaContatoNulo() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Contato(null)
        );
    }

    @Test
    void deveLancarExcecaoParaContatoVazio() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Contato("")
        );
    }

    @Test
    void deveLancarExcecaoParaEmailInvalido() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Contato("email_invalido")
        );
    }

    @Test
    void deveLancarExcecaoParaTelefoneInvalido() {
        // Act & Assert
        assertThrows(
            DomainValidationException.class,
            () -> new Contato("123")
        );
    }
}
```

---

## 🎯 Cobertura de Testes

Cada Value Object deve ter testes para:

1. ✅ **Criação válida** - Casos de sucesso
2. ✅ **Validações** - Todos os casos de erro
3. ✅ **Formatação** - Métodos de formatação
4. ✅ **Operações** - Métodos de negócio (quando aplicável)
5. ✅ **Comparações** - Equals, hashCode, compareTo
6. ✅ **Edge cases** - Casos limites

---

## 🚀 Executando os Testes

```bash
# Executar todos os testes
./mvnw test

# Executar apenas testes de Value Objects
./mvnw test -Dtest="*ValueObject*"

# Executar com cobertura
./mvnw test jacoco:report
```

---

## 📊 Exemplo de Relatório de Cobertura Esperado

```
Value Objects Coverage:
- CpfCnpj: 100%
- Placa: 100%
- ValorMonetario: 100%
- AnoVeiculo: 100%
- Contato: 100%
```

---

## 💡 Dicas para Testes

1. **Use nomes descritivos**: `deveCriarCpfValido()` é melhor que `testCpf1()`
2. **Siga AAA**: Arrange, Act, Assert
3. **Um assert por teste**: Quando possível
4. **Teste casos limites**: Valores mínimos, máximos, nulos
5. **Teste mensagens de erro**: Verifique se as exceções têm mensagens claras

---

**Nota:** Estes são exemplos. Adapte conforme necessário para seu projeto!



