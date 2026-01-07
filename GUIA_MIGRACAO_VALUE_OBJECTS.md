# Guia de Migração para Value Objects

Este guia ajuda desenvolvedores a entender como trabalhar com os novos Value Objects no projeto.

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Mudanças nas Entidades](#mudanças-nas-entidades)
3. [Como Usar nos Services](#como-usar-nos-services)
4. [Como Usar nos Controllers](#como-usar-nos-controllers)
5. [Tratamento de Erros](#tratamento-de-erros)
6. [Exemplos Práticos](#exemplos-práticos)
7. [Checklist de Migração](#checklist-de-migração)

---

## 🎯 Visão Geral

### Antes (Fase 1)
```java
Cliente cliente = new Cliente();
cliente.setCpfCnpj("12345678909"); // String simples
```

### Depois (Fase 2)
```java
Cliente cliente = new Cliente();
cliente.setCpfCnpj(new CpfCnpj("12345678909")); // Value Object com validação
```

**Benefício:** Validação automática no momento da criação!

---

## 🔄 Mudanças nas Entidades

### Cliente

| Campo | Antes | Depois |
|-------|-------|--------|
| cpfCnpj | `String` | `CpfCnpj` |
| contato | `String` | `Contato` |

**Exemplo de uso:**
```java
// Criar
Cliente cliente = new Cliente();
cliente.setCpfCnpj(new CpfCnpj("12345678909"));
cliente.setContato(new Contato("cliente@email.com"));

// Ler
String cpf = cliente.getCpfCnpj().getValor();
String email = cliente.getContato().getValor();
```

---

### Veiculo

| Campo | Antes | Depois |
|-------|-------|--------|
| placa | `String` | `Placa` |
| ano | `Integer` | `AnoVeiculo` |

**Exemplo de uso:**
```java
// Criar
Veiculo veiculo = new Veiculo();
veiculo.setPlaca(new Placa("ABC1234"));
veiculo.setAno(new AnoVeiculo(2023));

// Ler
String placa = veiculo.getPlaca().getValor();
Integer ano = veiculo.getAno().getValor();

// Métodos úteis
boolean isMercosul = veiculo.getPlaca().isMercosul();
int idade = veiculo.getAno().getIdadeEmAnos();
```

---

### Entidades com Valores Monetários

| Entidade | Campo | Antes | Depois |
|----------|-------|-------|--------|
| OrdemDeServico | valorTotalOrcamento | `BigDecimal` | `ValorMonetario` |
| PecaInsumo | preco | `BigDecimal` | `ValorMonetario` |
| Servico | preco | `BigDecimal` | `ValorMonetario` |
| OrdemServicoItem | precoUnitario, subtotal | `BigDecimal` | `ValorMonetario` |
| OrdemServicoPeca | precoUnitario, subtotal | `BigDecimal` | `ValorMonetario` |

**Exemplo de uso:**
```java
// Criar
Servico servico = new Servico();
servico.setPreco(new ValorMonetario("150.00"));

// Operações
ValorMonetario total = servico.getPreco().multiplicar(3);
ValorMonetario desconto = new ValorMonetario("50.00");
ValorMonetario final = total.subtrair(desconto);

// Ler
BigDecimal valor = servico.getPreco().getValor();
String formatado = servico.getPreco().getFormatado(); // "R$ 150,00"
```

---

## 🛠️ Como Usar nos Services

### Padrão de Conversão

**DTO → Value Object → Entidade**

```java
@Service
public class ClienteService {
    
    @Transactional
    public ClienteResponseDTO criar(ClienteDTO dto) {
        // 1. Converter DTO para Value Objects
        CpfCnpj cpfCnpj = new CpfCnpj(dto.getCpfCnpj());
        Contato contato = new Contato(dto.getContato());
        
        // 2. Validar duplicação (usar .getValor() para buscar no banco)
        if (repository.existsByCpfCnpj(cpfCnpj.getValor())) {
            throw new DuplicateResourceException("CPF/CNPJ já cadastrado");
        }
        
        // 3. Criar entidade com Value Objects
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setContato(contato);
        
        // 4. Salvar e retornar
        return ClienteResponseDTO.fromEntity(repository.save(cliente));
    }
}
```

### Operações com ValorMonetario

```java
@Service
public class OrdemDeServicoService {
    
    public void calcularTotal(OrdemDeServico os) {
        // Iniciar com zero
        ValorMonetario total = new ValorMonetario(BigDecimal.ZERO);
        
        // Somar serviços
        for (OrdemServicoItem item : os.getItensServico()) {
            ValorMonetario subtotal = item.getPrecoUnitario()
                .multiplicar(item.getQuantidade());
            total = total.somar(subtotal);
        }
        
        // Somar peças
        for (OrdemServicoPeca item : os.getItensPeca()) {
            ValorMonetario subtotal = item.getPrecoUnitario()
                .multiplicar(item.getQuantidade());
            total = total.somar(subtotal);
        }
        
        os.setValorTotalOrcamento(total);
    }
}
```

---

## 🎮 Como Usar nos Controllers

### Controllers não mudam!

Os DTOs de entrada continuam recebendo tipos primitivos:

```java
@PostMapping
public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteDTO dto) {
    // dto.getCpfCnpj() ainda é String
    // A conversão para CpfCnpj acontece no Service
    return ResponseEntity.ok(clienteService.criar(dto));
}
```

### DTOs de Resposta

Os DTOs de resposta também retornam tipos primitivos:

```java
@Data
public class ClienteResponseDTO {
    private Long id;
    private String nome;
    private String cpfCnpj;  // String, não CpfCnpj
    private String contato;   // String, não Contato
    
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpfCnpj().getValor(),  // Extrair valor
            cliente.getContato().getValor()    // Extrair valor
        );
    }
}
```

---

## ⚠️ Tratamento de Erros

### DomainValidationException

Todas as validações de Value Objects lançam `DomainValidationException`:

```java
try {
    CpfCnpj cpf = new CpfCnpj("12345678900"); // CPF inválido
} catch (DomainValidationException e) {
    // Mensagem: "CPF inválido"
    // HTTP Status: 400 Bad Request (tratado pelo GlobalExceptionHandler)
}
```

### Mensagens de Erro Claras

```java
// CPF/CNPJ
"CPF/CNPJ não pode ser nulo ou vazio"
"CPF deve ter 11 dígitos e CNPJ deve ter 14 dígitos"
"CPF inválido"
"CNPJ inválido"

// Placa
"Placa não pode ser nula ou vazia"
"Placa deve seguir o formato brasileiro: ABC1234 (antigo) ou ABC1D23 (Mercosul)"

// ValorMonetario
"Valor monetário não pode ser nulo"
"Valor monetário não pode ser negativo"
"Valor monetário excede o limite permitido"
"Resultado da subtração não pode ser negativo"

// AnoVeiculo
"Ano do veículo não pode ser nulo"
"Ano do veículo não pode ser anterior a 1900"
"Ano do veículo não pode ser superior a 2027"

// Contato
"Contato não pode ser nulo ou vazio"
"Contato inválido. Deve ser um email válido ou telefone brasileiro"
```

---

## 💡 Exemplos Práticos

### Exemplo 1: Criar Cliente Completo

```java
@Service
public class ClienteService {
    
    @Transactional
    public ClienteResponseDTO criar(ClienteDTO dto) {
        // Validação automática ao criar Value Objects
        CpfCnpj cpfCnpj = new CpfCnpj(dto.getCpfCnpj());
        Contato contato = new Contato(dto.getContato());
        
        // Verificar duplicação
        if (clienteRepository.existsByCpfCnpj(cpfCnpj.getValor())) {
            throw new DuplicateResourceException(
                "Já existe um cliente cadastrado com este CPF/CNPJ"
            );
        }
        
        // Criar e salvar
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setContato(contato);
        
        Cliente salvo = clienteRepository.save(cliente);
        return ClienteResponseDTO.fromEntity(salvo);
    }
}
```

### Exemplo 2: Criar Ordem de Serviço

```java
@Service
public class OrdemDeServicoService {
    
    @Transactional
    public OrdemDeServicoResponseDTO criar(OrdemDeServicoInputDTO dto) {
        // Buscar cliente por CPF/CNPJ
        Cliente cliente = clienteRepository
            .findByCpfCnpj(dto.getCpfCnpjCliente())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        
        // Buscar ou criar veículo
        Placa placa = new Placa(dto.getVeiculo().getPlaca());
        Veiculo veiculo = veiculoRepository
            .findByPlaca(placa.getValor())
            .orElseGet(() -> criarVeiculo(dto.getVeiculo(), cliente));
        
        // Criar OS
        OrdemDeServico os = new OrdemDeServico();
        os.setCliente(cliente);
        os.setVeiculo(veiculo);
        
        // Calcular total
        ValorMonetario total = new ValorMonetario(BigDecimal.ZERO);
        
        // Adicionar serviços
        for (ItemServicoDTO itemDTO : dto.getServicos()) {
            Servico servico = servicoRepository.findById(itemDTO.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
            
            OrdemServicoItem item = new OrdemServicoItem();
            item.setServico(servico);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(servico.getPreco());
            item.setSubtotal(servico.getPreco().multiplicar(itemDTO.getQuantidade()));
            
            os.adicionarItemServico(item);
            total = total.somar(item.getSubtotal());
        }
        
        os.setValorTotalOrcamento(total);
        
        OrdemDeServico salva = ordemDeServicoRepository.save(os);
        return OrdemDeServicoResponseDTO.fromEntity(salva);
    }
    
    private Veiculo criarVeiculo(VeiculoInputDTO dto, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(new Placa(dto.getPlaca()));
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAno(new AnoVeiculo(dto.getAno()));
        veiculo.setCliente(cliente);
        return veiculoRepository.save(veiculo);
    }
}
```

### Exemplo 3: Atualizar Preço com Desconto

```java
@Service
public class PecaInsumoService {
    
    @Transactional
    public void aplicarDesconto(Long pecaId, BigDecimal percentualDesconto) {
        PecaInsumo peca = pecaInsumoRepository.findById(pecaId)
            .orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada"));
        
        // Calcular desconto
        BigDecimal fatorDesconto = BigDecimal.ONE.subtract(
            percentualDesconto.divide(BigDecimal.valueOf(100))
        );
        
        // Aplicar desconto
        ValorMonetario novoPreco = peca.getPreco().multiplicar(fatorDesconto);
        peca.setPreco(novoPreco);
        
        pecaInsumoRepository.save(peca);
    }
}
```

---

## ✅ Checklist de Migração

### Para Desenvolvedores

- [ ] Entendi que DTOs de entrada continuam com tipos primitivos
- [ ] Entendi que a conversão para Value Objects acontece nos Services
- [ ] Entendi que DTOs de resposta extraem valores com `.getValor()`
- [ ] Sei como criar instâncias de Value Objects
- [ ] Sei como usar métodos dos Value Objects (somar, multiplicar, etc.)
- [ ] Sei como tratar DomainValidationException
- [ ] Atualizei meus testes para trabalhar com Value Objects

### Para Novos Serviços

Ao criar um novo serviço, lembre-se:

1. ✅ Converter DTOs para Value Objects no início do método
2. ✅ Usar `.getValor()` ao buscar no banco de dados
3. ✅ Usar métodos dos Value Objects para operações (não BigDecimal diretamente)
4. ✅ Extrair valores primitivos ao retornar DTOs
5. ✅ Deixar que DomainValidationException seja tratada pelo GlobalExceptionHandler

---

## 🔍 Troubleshooting

### Problema: "Cannot convert String to CpfCnpj"

**Solução:** Você está tentando passar String diretamente. Crie o Value Object:
```java
// ❌ Errado
cliente.setCpfCnpj(dto.getCpfCnpj());

// ✅ Correto
cliente.setCpfCnpj(new CpfCnpj(dto.getCpfCnpj()));
```

### Problema: "Cannot convert CpfCnpj to String"

**Solução:** Use `.getValor()` para extrair o valor:
```java
// ❌ Errado
return cliente.getCpfCnpj();

// ✅ Correto
return cliente.getCpfCnpj().getValor();
```

### Problema: "Query failed - no such column 'cpf_cnpj'"

**Solução:** O campo agora é embedded. Use `cpfCnpj.valor` na query:
```java
// ❌ Errado
@Query("SELECT c FROM Cliente c WHERE c.cpfCnpj = :cpf")

// ✅ Correto
@Query("SELECT c FROM Cliente c WHERE c.cpfCnpj.valor = :cpf")
```

### Problema: "Cannot multiply BigDecimal by ValorMonetario"

**Solução:** Use os métodos do ValorMonetario:
```java
// ❌ Errado
BigDecimal total = preco.multiply(BigDecimal.valueOf(quantidade));

// ✅ Correto
ValorMonetario total = preco.multiplicar(quantidade);
```

---

## 📚 Referências Rápidas

### Criar Value Objects

```java
new CpfCnpj("12345678909")
new Placa("ABC1234")
new ValorMonetario("99.99")
new AnoVeiculo(2023)
new Contato("email@exemplo.com")
```

### Extrair Valores

```java
.getValor()           // Todos os Value Objects
.getFormatado()       // CpfCnpj, Placa, ValorMonetario, Contato
.getTipo()            // Contato (EMAIL ou TELEFONE)
.getIdadeEmAnos()     // AnoVeiculo
.isMercosul()         // Placa
.isEmail()            // Contato
```

### Operações com ValorMonetario

```java
.somar(outro)
.subtrair(outro)
.multiplicar(fator)
.multiplicar(quantidade)
.isMaiorQue(outro)
.isMenorQue(outro)
.isZero()
```

---

## 🎓 Próximos Passos

1. ✅ Revise o código existente
2. ✅ Atualize seus testes
3. ✅ Pratique com os exemplos
4. ✅ Leia a documentação completa em `REFATORACAO_VALUE_OBJECTS.md`
5. ✅ Consulte os exemplos de testes em `EXEMPLOS_TESTES_VALUE_OBJECTS.md`

---

**Dúvidas?** Consulte a equipe ou abra uma issue no repositório!


