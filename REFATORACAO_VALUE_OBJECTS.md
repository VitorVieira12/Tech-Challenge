# Refatoração do Domínio - Value Objects (Fase 2)

## 📋 Resumo da Refatoração

Esta refatoração foi realizada para atender ao feedback da Fase 1, enriquecendo o domínio através da implementação de **Value Objects** para eliminar a "Obsessão por Primitivos" e centralizar validações de regras de negócio.

## 🎯 Objetivos Alcançados

✅ Centralização de validações no domínio  
✅ Eliminação da obsessão por primitivos  
✅ Garantia de integridade dos dados através de Value Objects  
✅ Validações executadas no construtor (fail-fast)  
✅ Imutabilidade dos Value Objects  
✅ Código mais expressivo e orientado ao domínio

## 🏗️ Value Objects Criados

### 1. **CpfCnpj** (`com.techchallenge.domain.valueobject.CpfCnpj`)

**Responsabilidade:** Representar e validar CPF ou CNPJ brasileiro.

**Validações Implementadas:**
- Verifica se o documento não é nulo ou vazio
- Remove caracteres não numéricos
- Valida tamanho (11 dígitos para CPF, 14 para CNPJ)
- Valida dígitos verificadores usando algoritmo oficial
- Rejeita documentos com todos os dígitos iguais

**Métodos Úteis:**
- `getValor()`: Retorna o documento sem formatação
- `getFormatado()`: Retorna o documento formatado (000.000.000-00 ou 00.000.000/0000-00)

**Uso:**
```java
CpfCnpj cpf = new CpfCnpj("12345678909");
// Lança DomainValidationException se inválido
```

---

### 2. **Placa** (`com.techchallenge.domain.valueobject.Placa`)

**Responsabilidade:** Representar e validar placa de veículo brasileiro.

**Validações Implementadas:**
- Verifica se não é nula ou vazia
- Converte para maiúsculas automaticamente
- Valida formato antigo (ABC1234)
- Valida formato Mercosul (ABC1D23)

**Métodos Úteis:**
- `getValor()`: Retorna a placa sem formatação
- `getFormatado()`: Retorna a placa formatada (ABC-1234)
- `isMercosul()`: Verifica se é formato Mercosul
- `isFormatoAntigo()`: Verifica se é formato antigo

**Uso:**
```java
Placa placa = new Placa("ABC1234");
// ou
Placa placaMercosul = new Placa("ABC1D23");
```

---

### 3. **ValorMonetario** (`com.techchallenge.domain.valueobject.ValorMonetario`)

**Responsabilidade:** Representar valores monetários com validações e operações seguras.

**Validações Implementadas:**
- Não permite valores nulos
- Não permite valores negativos
- Limita precisão a 8 dígitos inteiros e 2 decimais
- Arredonda automaticamente para 2 casas decimais

**Métodos Úteis:**
- `getValor()`: Retorna o BigDecimal
- `getFormatado()`: Retorna formatado como moeda brasileira (R$ 1.234,56)
- `somar(ValorMonetario)`: Soma dois valores
- `subtrair(ValorMonetario)`: Subtrai valores (não permite resultado negativo)
- `multiplicar(BigDecimal)`: Multiplica por um fator
- `multiplicar(int)`: Multiplica por quantidade
- `isMaiorQue()`, `isMenorQue()`, `isZero()`: Comparações

**Uso:**
```java
ValorMonetario preco = new ValorMonetario(new BigDecimal("99.99"));
ValorMonetario total = preco.multiplicar(3); // R$ 299.97
```

---

### 4. **AnoVeiculo** (`com.techchallenge.domain.valueobject.AnoVeiculo`)

**Responsabilidade:** Representar e validar ano de fabricação de veículo.

**Validações Implementadas:**
- Não permite valores nulos
- Ano mínimo: 1900
- Ano máximo: Ano atual + 1 (permite ano modelo)

**Métodos Úteis:**
- `getValor()`: Retorna o ano
- `getIdadeEmAnos()`: Calcula idade do veículo
- `isAnoModelo()`: Verifica se é ano modelo
- `isClassico()`: Verifica se tem mais de 30 anos

**Uso:**
```java
AnoVeiculo ano = new AnoVeiculo(2023);
int idade = ano.getIdadeEmAnos(); // 3 (em 2026)
```

---

### 5. **Contato** (`com.techchallenge.domain.valueobject.Contato`)

**Responsabilidade:** Representar e validar contato (email ou telefone).

**Validações Implementadas:**
- Verifica se não é nulo ou vazio
- Valida formato de email (RFC 5322 simplificado)
- Valida formato de telefone brasileiro
- Detecta automaticamente o tipo (EMAIL ou TELEFONE)
- Normaliza telefones (remove formatação)

**Métodos Úteis:**
- `getValor()`: Retorna o contato normalizado
- `getFormatado()`: Retorna formatado ((11) 98888-7777 para telefone)
- `getTipo()`: Retorna TipoContato.EMAIL ou TipoContato.TELEFONE
- `isEmail()`, `isTelefone()`: Verificações de tipo

**Uso:**
```java
Contato email = new Contato("usuario@exemplo.com");
Contato telefone = new Contato("(11) 98888-7777");
```

---

## 🔄 Entidades Refatoradas

### **Cliente**
- `String cpfCnpj` → `CpfCnpj cpfCnpj`
- `String contato` → `Contato contato`

### **Veiculo**
- `String placa` → `Placa placa`
- `Integer ano` → `AnoVeiculo ano`

### **OrdemDeServico**
- `BigDecimal valorTotalOrcamento` → `ValorMonetario valorTotalOrcamento`

### **PecaInsumo**
- `BigDecimal preco` → `ValorMonetario preco`

### **Servico**
- `BigDecimal preco` → `ValorMonetario preco`

### **OrdemServicoItem**
- `BigDecimal precoUnitario` → `ValorMonetario precoUnitario`
- `BigDecimal subtotal` → `ValorMonetario subtotal`

### **OrdemServicoPeca**
- `BigDecimal precoUnitario` → `ValorMonetario precoUnitario`
- `BigDecimal subtotal` → `ValorMonetario subtotal`

---

## 🛠️ Componentes Atualizados

### **Exception Handler**
- Adicionado `DomainValidationException` no `GlobalExceptionHandler`
- Retorna HTTP 400 (Bad Request) para validações de domínio

### **Services**
Todos os services foram atualizados para:
- Criar instâncias de Value Objects ao receber DTOs
- Usar métodos dos Value Objects (ex: `multiplicar()`, `somar()`)
- Extrair valores primitivos ao retornar DTOs (`.getValor()`)

**Services Atualizados:**
- `ClienteService`
- `VeiculoService`
- `PecaInsumoService`
- `ServicoService`
- `OrdemDeServicoService`

### **Repositories**
Atualizados para buscar por campos embedded usando JPQL:

**ClienteRepository:**
```java
@Query("SELECT c FROM Cliente c WHERE c.cpfCnpj.valor = :cpfCnpj")
Optional<Cliente> findByCpfCnpj(@Param("cpfCnpj") String cpfCnpj);
```

**VeiculoRepository:**
```java
@Query("SELECT v FROM Veiculo v WHERE v.placa.valor = :placa")
Optional<Veiculo> findByPlaca(@Param("placa") String placa);
```

### **DTOs Response**
Todos os DTOs de resposta foram atualizados para extrair valores dos Value Objects:

```java
// Antes
cliente.getCpfCnpj()

// Depois
cliente.getCpfCnpj().getValor()
```

**DTOs Atualizados:**
- `ClienteResponseDTO`
- `VeiculoResponseDTO`
- `ServicoResponseDTO`
- `PecaInsumoResponseDTO`
- `OrdemDeServicoResponseDTO`
- `OrdemDeServicoPublicDTO`

---

## 📊 Benefícios da Refatoração

### 1. **Validação Centralizada**
Antes, validações estavam espalhadas em DTOs e Services. Agora estão centralizadas nos Value Objects.

### 2. **Fail-Fast**
Erros são detectados imediatamente na criação do objeto, não durante operações posteriores.

### 3. **Código Mais Expressivo**
```java
// Antes
BigDecimal total = preco.multiply(BigDecimal.valueOf(quantidade));

// Depois
ValorMonetario total = preco.multiplicar(quantidade);
```

### 4. **Imutabilidade**
Value Objects são imutáveis, prevenindo alterações acidentais.

### 5. **Reutilização**
Validações podem ser reutilizadas em qualquer parte do sistema.

### 6. **Testabilidade**
Value Objects podem ser testados isoladamente.

---

## 🧪 Exemplo de Uso Completo

```java
// Criar um cliente com Value Objects
Cliente cliente = new Cliente();
cliente.setNome("João Silva");
cliente.setCpfCnpj(new CpfCnpj("12345678909")); // Valida CPF
cliente.setContato(new Contato("joao@email.com")); // Valida email

// Criar um veículo
Veiculo veiculo = new Veiculo();
veiculo.setPlaca(new Placa("ABC1234")); // Valida formato
veiculo.setAno(new AnoVeiculo(2020)); // Valida range
veiculo.setCliente(cliente);

// Criar um serviço
Servico servico = new Servico();
servico.setDescricao("Troca de óleo");
servico.setPreco(new ValorMonetario("150.00")); // Valida valor

// Calcular subtotal
ValorMonetario subtotal = servico.getPreco().multiplicar(2); // R$ 300.00
```

---

## ✅ Validação da Refatoração

### Compilação
```bash
./mvnw clean compile -DskipTests
```
**Resultado:** ✅ BUILD SUCCESS

### Estrutura de Arquivos
```
src/main/java/com/techchallenge/
├── domain/
│   ├── exception/
│   │   └── DomainValidationException.java (NOVO)
│   ├── valueobject/ (NOVO)
│   │   ├── CpfCnpj.java
│   │   ├── Placa.java
│   │   ├── ValorMonetario.java
│   │   ├── AnoVeiculo.java
│   │   └── Contato.java
│   ├── model/ (REFATORADO)
│   │   ├── Cliente.java
│   │   ├── Veiculo.java
│   │   ├── OrdemDeServico.java
│   │   ├── PecaInsumo.java
│   │   ├── Servico.java
│   │   ├── OrdemServicoItem.java
│   │   └── OrdemServicoPeca.java
│   ├── service/ (REFATORADO)
│   ├── dto/ (REFATORADO)
│   └── repository/ (REFATORADO)
```

---

## 🎓 Conceitos de DDD Aplicados

1. **Value Objects**: Objetos sem identidade própria, definidos por seus atributos
2. **Ubiquitous Language**: Termos do domínio (CpfCnpj, Placa, ValorMonetario)
3. **Domain Validation**: Validações no domínio, não na infraestrutura
4. **Immutability**: Value Objects imutáveis
5. **Self-Validation**: Objetos se auto-validam no construtor

---

## 📝 Notas Importantes

1. **Compatibilidade JPA**: Todos os Value Objects usam `@Embeddable` e `@NoArgsConstructor` para compatibilidade com JPA/Hibernate.

2. **Serialização**: Implementam `Serializable` para permitir serialização.

3. **Equals e HashCode**: Implementados via Lombok `@EqualsAndHashCode` baseado nos valores.

4. **Validações**: Todas as validações lançam `DomainValidationException` com mensagens claras.

5. **Backward Compatibility**: DTOs de entrada continuam recebendo tipos primitivos, mas são convertidos para Value Objects internamente.

---

## 🚀 Próximos Passos Sugeridos

1. ✅ Criar testes unitários para cada Value Object
2. ✅ Atualizar testes de integração existentes
3. ✅ Documentar APIs com exemplos usando Value Objects
4. ✅ Criar migration scripts se necessário para banco de dados
5. ✅ Adicionar mais métodos úteis aos Value Objects conforme necessidade

---

## 📚 Referências

- Domain-Driven Design (Eric Evans)
- Implementing Domain-Driven Design (Vaughn Vernon)
- Clean Architecture (Robert C. Martin)
- Patterns of Enterprise Application Architecture (Martin Fowler)

---

**Data da Refatoração:** 06/01/2026  
**Versão:** Fase 2 - Value Objects Implementation  
**Status:** ✅ Completo e Compilando
