# Correções Necessárias nos Testes

## 📋 Resumo
31 erros de compilação nos testes que precisam ser corrigidos para usar os novos Value Objects.

## 🔧 Correções Necessárias

### Padrão de Substituição Geral

| Erro | Substituir | Por |
|------|-----------|-----|
| `.setCpfCnpj("...")` | `.setCpfCnpj("12345678901")` | `.setCpfCnpj(new CpfCnpj("12345678901"))` |
| `.setContato("...")` | `.setContato("email@test.com")` | `.setContato(new Contato("email@test.com"))` |
| `.setPlaca("...")` | `.setPlaca("ABC1234")` | `.setPlaca(new Placa("ABC1234"))` |
| `.setAno(2020)` | `.setAno(2020)` | `.setAno(new AnoVeiculo(2020))` |
| `.setPreco(new BigDecimal(...))` | `.setPreco(new BigDecimal("100.00"))` | `.setPreco(new ValorMonetario(new BigDecimal("100.00")))` |

### Imports Necessários

Adicione estes imports nos arquivos de teste que ainda não têm:

```java
import com.techchallenge.domain.valueobject.CpfCnpj;
import com.techchallenge.domain.valueobject.Contato;
import com.techchallenge.domain.valueobject.Placa;
import com.techchallenge.domain.valueobject.AnoVeiculo;
import com.techchallenge.domain.valueobject.ValorMonetario;
```

---

## 📁 Arquivos que Precisam ser Corrigidos

### 1. OrdemDeServicoControllerIntegrationTest.java (4 erros)
**Linhas:** 160, 161, 166, 171

### 2. PecaInsumoControllerIntegrationTest.java (8 erros)
**Linhas:** 79, 105, 112, 129, 156, 173, 190, 206

### 3. ServicoControllerIntegrationTest.java (5 erros)
**Linhas:** 74, 98, 103, 118, 153

### 4. OrdemDeServicoServiceTest.java (8 erros)
**Linhas:** 57, 58, 62, 65, 71, 76, 155, 202

### 5. VeiculoServiceTest.java (6 erros)
**Linhas:** 50, 51, 62, 65, 167, 170

---

## 🚀 Opção Rápida: Find & Replace Global

Se você quiser fazer todas as correções de uma vez, use Find & Replace no seu IDE com regex:

### Find & Replace Patterns (Regex)

1. **CpfCnpj:**
   - Find: `\.setCpfCnpj\("([^"]+)"\)`
   - Replace: `.setCpfCnpj(new CpfCnpj("$1"))`

2. **Contato:**
   - Find: `\.setContato\("([^"]+)"\)`
   - Replace: `.setContato(new Contato("$1"))`

3. **Placa:**
   - Find: `\.setPlaca\("([^"]+)"\)`
   - Replace: `.setPlaca(new Placa("$1"))`

4. **AnoVeiculo:**
   - Find: `\.setAno\((\d+)\)`
   - Replace: `.setAno(new AnoVeiculo($1))`

5. **ValorMonetario:**
   - Find: `\.setPreco\(new BigDecimal\("([^"]+)"\)\)`
   - Replace: `.setPreco(new ValorMonetario(new BigDecimal("$1")))`

**Nota:** Aplique essas substituições APENAS nos arquivos de teste em `src/test/java/`

---

## 📝 Exemplo de Correção Manual

### Antes:
```java
cliente.setCpfCnpj("12345678901");
cliente.setContato("email@test.com");
veiculo.setPlaca("ABC1234");
veiculo.setAno(2020);
servico.setPreco(new BigDecimal("150.00"));
```

### Depois:
```java
cliente.setCpfCnpj(new CpfCnpj("12345678901"));
cliente.setContato(new Contato("email@test.com"));
veiculo.setPlaca(new Placa("ABC1234"));
veiculo.setAno(new AnoVeiculo(2020));
servico.setPreco(new ValorMonetario(new BigDecimal("150.00")));
```

---

## ✅ Verificação

Após fazer as correções, compile os testes:

```bash
.\mvnw.cmd clean test-compile
```

Se tudo estiver correto, você verá:
```
[INFO] BUILD SUCCESS
```

---

## 🎯 Status

- ✅ ClienteControllerIntegrationTest - Corrigido
- ✅ VeiculoControllerIntegrationTest - Corrigido  
- ✅ ClienteServiceTest - Corrigido
- ✅ ServicoServiceTest - Corrigido
- ✅ PecaInsumoServiceTest - Corrigido
- ❌ OrdemDeServicoControllerIntegrationTest - Pendente
- ❌ PecaInsumoControllerIntegrationTest - Pendente
- ❌ ServicoControllerIntegrationTest - Pendente
- ❌ OrdemDeServicoServiceTest - Pendente
- ❌ VeiculoServiceTest - Pendente

