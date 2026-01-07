# Arquitetura - Value Objects no Domínio

## 📐 Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                        CAMADA DE APRESENTAÇÃO                    │
│                          (Controllers)                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ DTOs (tipos primitivos)
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      CAMADA DE APLICAÇÃO                         │
│                          (Services)                              │
│                                                                  │
│  • Converte DTOs → Value Objects                                │
│  • Orquestra operações de domínio                               │
│  • Converte Entidades → DTOs                                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Value Objects + Entidades
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                       CAMADA DE DOMÍNIO                          │
│                                                                  │
│  ┌──────────────────┐      ┌──────────────────┐                │
│  │  Value Objects   │      │    Entidades     │                │
│  │                  │      │                  │                │
│  │  • CpfCnpj       │◄─────┤  • Cliente       │                │
│  │  • Placa         │      │  • Veiculo       │                │
│  │  • ValorMonetario│      │  • OrdemServico  │                │
│  │  • AnoVeiculo    │      │  • PecaInsumo    │                │
│  │  • Contato       │      │  • Servico       │                │
│  └──────────────────┘      └──────────────────┘                │
│                                                                  │
│  ┌──────────────────────────────────────────┐                  │
│  │       Domain Exceptions                   │                  │
│  │  • DomainValidationException             │                  │
│  └──────────────────────────────────────────┘                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Entidades JPA
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    CAMADA DE PERSISTÊNCIA                        │
│                       (Repositories)                             │
│                                                                  │
│  • ClienteRepository                                            │
│  • VeiculoRepository                                            │
│  • OrdemDeServicoRepository                                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Estrutura de Pacotes

```
com.techchallenge
│
├── controller/                    # Camada de Apresentação
│   ├── ClienteController
│   ├── VeiculoController
│   ├── OrdemDeServicoController
│   ├── PecaInsumoController
│   └── ServicoController
│
├── domain/                        # Camada de Domínio
│   │
│   ├── model/                     # Entidades
│   │   ├── Cliente
│   │   ├── Veiculo
│   │   ├── OrdemDeServico
│   │   ├── OrdemServicoItem
│   │   ├── OrdemServicoPeca
│   │   ├── PecaInsumo
│   │   ├── Servico
│   │   └── StatusOrdemServico
│   │
│   ├── valueobject/               # Value Objects (NOVO)
│   │   ├── CpfCnpj
│   │   ├── Placa
│   │   ├── ValorMonetario
│   │   ├── AnoVeiculo
│   │   └── Contato
│   │
│   ├── exception/                 # Exceções de Domínio
│   │   ├── DomainValidationException (NOVO)
│   │   ├── ResourceNotFoundException
│   │   ├── DuplicateResourceException
│   │   └── EstoqueInsuficienteException
│   │
│   ├── dto/                       # Data Transfer Objects
│   │   ├── ClienteDTO
│   │   ├── ClienteResponseDTO
│   │   ├── VeiculoDTO
│   │   ├── VeiculoResponseDTO
│   │   └── ...
│   │
│   ├── service/                   # Serviços de Aplicação
│   │   ├── ClienteService
│   │   ├── VeiculoService
│   │   ├── OrdemDeServicoService
│   │   ├── PecaInsumoService
│   │   └── ServicoService
│   │
│   └── repository/                # Repositórios
│       ├── ClienteRepository
│       ├── VeiculoRepository
│       ├── OrdemDeServicoRepository
│       ├── PecaInsumoRepository
│       └── ServicoRepository
│
└── security/                      # Segurança
    ├── SecurityConfig
    ├── JwtService
    └── ...
```

---

## 🔄 Fluxo de Dados: Criação de Cliente

```
┌─────────────┐
│   Cliente   │  POST /api/clientes
│   (HTTP)    │  { "cpfCnpj": "12345678909", ... }
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ ClienteController   │
│                     │
│  @PostMapping       │
│  criar(ClienteDTO)  │
└──────┬──────────────┘
       │
       │ ClienteDTO (String cpfCnpj)
       │
       ▼
┌─────────────────────────────────────────┐
│ ClienteService                          │
│                                         │
│  1. new CpfCnpj(dto.getCpfCnpj())      │ ◄── Validação!
│     ├─ Valida formato                  │
│     ├─ Valida dígitos verificadores    │
│     └─ Lança exceção se inválido       │
│                                         │
│  2. new Contato(dto.getContato())      │ ◄── Validação!
│     ├─ Detecta tipo (email/telefone)   │
│     ├─ Valida formato                  │
│     └─ Lança exceção se inválido       │
│                                         │
│  3. Verifica duplicação                │
│     repository.existsByCpfCnpj(...)    │
│                                         │
│  4. Cria entidade                      │
│     cliente.setCpfCnpj(cpfCnpj)        │
│     cliente.setContato(contato)        │
│                                         │
│  5. Salva no banco                     │
│     repository.save(cliente)           │
│                                         │
│  6. Converte para DTO resposta         │
│     ClienteResponseDTO.fromEntity()    │
│     ├─ cpfCnpj.getValor()             │
│     └─ contato.getValor()             │
└──────┬──────────────────────────────────┘
       │
       │ ClienteResponseDTO (String cpfCnpj)
       │
       ▼
┌─────────────────────┐
│ ClienteController   │
│                     │
│  return 200 OK      │
│  { "id": 1, ... }   │
└─────────────────────┘
```

---

## 🎯 Diagrama de Classes: Cliente e Value Objects

```
┌─────────────────────────────────────┐
│           Cliente                    │
│         (Entidade)                   │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - nome: String                      │
│ - cpfCnpj: CpfCnpj                  │◄────┐
│ - contato: Contato                  │◄───┐│
│ - veiculos: List<Veiculo>           │    ││
├─────────────────────────────────────┤    ││
│ + getId(): Long                     │    ││
│ + getNome(): String                 │    ││
│ + getCpfCnpj(): CpfCnpj            │    ││
│ + getContato(): Contato            │    ││
└─────────────────────────────────────┘    ││
                                            ││
┌───────────────────────────────────┐     ││
│         CpfCnpj                    │     ││
│      (Value Object)                │◄────┘│
├───────────────────────────────────┤      │
│ - valor: String                   │      │
├───────────────────────────────────┤      │
│ + CpfCnpj(String)                 │      │
│ + getValor(): String              │      │
│ + getFormatado(): String          │      │
│ - validar(String): String         │      │
│ - validarCpf(String): void        │      │
│ - validarCnpj(String): void       │      │
└───────────────────────────────────┘      │
                                            │
┌───────────────────────────────────┐      │
│         Contato                    │      │
│      (Value Object)                │◄─────┘
├───────────────────────────────────┤
│ - valor: String                   │
│ - tipo: TipoContato               │
├───────────────────────────────────┤
│ + Contato(String)                 │
│ + getValor(): String              │
│ + getTipo(): TipoContato          │
│ + getFormatado(): String          │
│ + isEmail(): boolean              │
│ + isTelefone(): boolean           │
│ - validarEDefinirTipo(): void     │
└───────────────────────────────────┘
```

---

## 💰 Diagrama de Classes: ValorMonetario

```
┌─────────────────────────────────────┐
│       ValorMonetario                │
│      (Value Object)                 │
├─────────────────────────────────────┤
│ - valor: BigDecimal                 │
├─────────────────────────────────────┤
│ + ValorMonetario(BigDecimal)        │
│ + ValorMonetario(String)            │
│ + ValorMonetario(double)            │
│                                     │
│ + getValor(): BigDecimal            │
│ + getFormatado(): String            │
│                                     │
│ + somar(ValorMonetario): VM         │
│ + subtrair(ValorMonetario): VM      │
│ + multiplicar(BigDecimal): VM       │
│ + multiplicar(int): VM              │
│                                     │
│ + isMaiorQue(ValorMonetario): bool  │
│ + isMenorQue(ValorMonetario): bool  │
│ + isZero(): boolean                 │
│                                     │
│ + compareTo(ValorMonetario): int    │
│ - validar(BigDecimal): BigDecimal   │
└─────────────────────────────────────┘
         ▲
         │ usado por
         │
    ┌────┴────┬────────┬────────────┐
    │         │        │            │
┌───┴───┐ ┌──┴──┐ ┌───┴────┐ ┌────┴─────┐
│Servico│ │Peca │ │OS Item │ │OS Peca   │
│       │ │     │ │        │ │          │
│-preco │ │-pre │ │-precoU │ │-precoUnit│
│       │ │co   │ │-subto  │ │-subtotal │
└───────┘ └─────┘ └────────┘ └──────────┘
```

---

## 🚗 Diagrama de Classes: Veiculo e Value Objects

```
┌─────────────────────────────────────┐
│           Veiculo                    │
│         (Entidade)                   │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - placa: Placa                      │◄────┐
│ - marca: String                     │     │
│ - modelo: String                    │     │
│ - ano: AnoVeiculo                   │◄───┐│
│ - cliente: Cliente                  │    ││
├─────────────────────────────────────┤    ││
│ + getPlaca(): Placa                 │    ││
│ + getAno(): AnoVeiculo             │    ││
└─────────────────────────────────────┘    ││
                                            ││
┌───────────────────────────────────┐     ││
│          Placa                     │     ││
│      (Value Object)                │◄────┘│
├───────────────────────────────────┤      │
│ - valor: String                   │      │
├───────────────────────────────────┤      │
│ + Placa(String)                   │      │
│ + getValor(): String              │      │
│ + getFormatado(): String          │      │
│ + isMercosul(): boolean           │      │
│ + isFormatoAntigo(): boolean      │      │
│ - validar(String): String         │      │
└───────────────────────────────────┘      │
                                            │
┌───────────────────────────────────┐      │
│       AnoVeiculo                   │      │
│      (Value Object)                │◄─────┘
├───────────────────────────────────┤
│ - valor: Integer                  │
├───────────────────────────────────┤
│ + AnoVeiculo(Integer)             │
│ + getValor(): Integer             │
│ + getIdadeEmAnos(): int           │
│ + isAnoModelo(): boolean          │
│ + isClassico(): boolean           │
│ + compareTo(AnoVeiculo): int      │
│ - validar(Integer): Integer       │
└───────────────────────────────────┘
```

---

## 🔐 Validação em Camadas

```
┌──────────────────────────────────────────────────────────────┐
│                    CAMADA DE VALIDAÇÃO                        │
└──────────────────────────────────────────────────────────────┘

Nível 1: Validação de Entrada (DTOs)
┌─────────────────────────────────────┐
│  @NotBlank                          │
│  @Pattern                           │
│  @Min, @Max                         │
│  @Size                              │
└─────────────────────────────────────┘
        │
        │ Validação básica (formato, obrigatoriedade)
        ▼
Nível 2: Validação de Domínio (Value Objects)
┌─────────────────────────────────────┐
│  CpfCnpj.validar()                  │
│  ├─ Dígitos verificadores           │
│  └─ Lógica específica de CPF/CNPJ   │
│                                     │
│  Placa.validar()                    │
│  ├─ Formato brasileiro              │
│  └─ Mercosul vs Antigo              │
│                                     │
│  ValorMonetario.validar()           │
│  ├─ Não negativo                    │
│  └─ Precisão monetária              │
└─────────────────────────────────────┘
        │
        │ Validação de regras de negócio
        ▼
Nível 3: Validação de Consistência (Services)
┌─────────────────────────────────────┐
│  Verificação de duplicação          │
│  Verificação de estoque             │
│  Validação de relacionamentos       │
│  Regras de negócio complexas        │
└─────────────────────────────────────┘
```

---

## 🎭 Tratamento de Exceções

```
┌─────────────────────────────────────────────────────────────┐
│                   FLUXO DE EXCEÇÕES                          │
└─────────────────────────────────────────────────────────────┘

Value Object
    │
    │ new CpfCnpj("inválido")
    │
    ▼
┌─────────────────────────────┐
│ DomainValidationException   │
│ "CPF inválido"              │
└──────────┬──────────────────┘
           │
           │ propagação
           │
           ▼
┌─────────────────────────────┐
│ Service                     │
│ (não trata, propaga)        │
└──────────┬──────────────────┘
           │
           │ propagação
           │
           ▼
┌─────────────────────────────┐
│ Controller                  │
│ (não trata, propaga)        │
└──────────┬──────────────────┘
           │
           │ interceptação
           │
           ▼
┌─────────────────────────────────────────┐
│ GlobalExceptionHandler                  │
│                                         │
│ @ExceptionHandler(                      │
│   DomainValidationException.class)     │
│                                         │
│ return ResponseEntity                   │
│   .status(400)                          │
│   .body(ErrorResponse)                  │
└──────────┬──────────────────────────────┘
           │
           │ HTTP Response
           │
           ▼
┌─────────────────────────────┐
│ Cliente HTTP                │
│                             │
│ 400 Bad Request             │
│ {                           │
│   "message": "CPF inválido" │
│   "status": 400             │
│ }                           │
└─────────────────────────────┘
```

---

## 📊 Mapeamento JPA: Value Objects

```
┌──────────────────────────────────────────────────────────────┐
│                    MAPEAMENTO JPA                             │
└──────────────────────────────────────────────────────────────┘

Entidade com Value Objects:
┌─────────────────────────────┐
│ @Entity                     │
│ public class Cliente {      │
│                             │
│   @Embedded                 │
│   private CpfCnpj cpfCnpj;  │
│                             │
│   @Embedded                 │
│   private Contato contato;  │
│ }                           │
└─────────────────────────────┘
        │
        │ JPA Mapping
        │
        ▼
┌─────────────────────────────┐
│ Tabela: clientes            │
├─────────────────────────────┤
│ id: BIGINT                  │
│ nome: VARCHAR               │
│ cpf_cnpj: VARCHAR(14)       │ ◄── CpfCnpj.valor
│ contato: VARCHAR            │ ◄── Contato.valor
│ tipo_contato: VARCHAR       │ ◄── Contato.tipo
└─────────────────────────────┘

Value Object:
┌─────────────────────────────┐
│ @Embeddable                 │
│ public class CpfCnpj {      │
│                             │
│   @Column(                  │
│     name = "cpf_cnpj",      │
│     nullable = false,       │
│     unique = true,          │
│     length = 14             │
│   )                         │
│   private String valor;     │
│ }                           │
└─────────────────────────────┘

Múltiplos Value Objects do mesmo tipo:
┌─────────────────────────────────────┐
│ @Entity                             │
│ public class OrdemServicoItem {     │
│                                     │
│   @Embedded                         │
│   @AttributeOverride(               │
│     name = "valor",                 │
│     column = @Column(               │
│       name = "preco_unitario"       │
│     )                               │
│   )                                 │
│   private ValorMonetario precoUnit; │
│                                     │
│   @Embedded                         │
│   @AttributeOverride(               │
│     name = "valor",                 │
│     column = @Column(               │
│       name = "subtotal"             │
│     )                               │
│   )                                 │
│   private ValorMonetario subtotal;  │
│ }                                   │
└─────────────────────────────────────┘
```

---

## 🔍 Queries com Value Objects

```
Repository:
┌─────────────────────────────────────────────────────────────┐
│ @Repository                                                  │
│ public interface ClienteRepository {                        │
│                                                             │
│   @Query("SELECT c FROM Cliente c                          │
│           WHERE c.cpfCnpj.valor = :cpfCnpj")              │
│   Optional<Cliente> findByCpfCnpj(                         │
│       @Param("cpfCnpj") String cpfCnpj);                  │
│                                                             │
│   @Query("SELECT CASE WHEN COUNT(c) > 0                   │
│           THEN true ELSE false END                         │
│           FROM Cliente c                                   │
│           WHERE c.cpfCnpj.valor = :cpfCnpj")              │
│   boolean existsByCpfCnpj(                                 │
│       @Param("cpfCnpj") String cpfCnpj);                  │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘

SQL Gerado:
┌─────────────────────────────────────────────────────────────┐
│ SELECT c.id, c.nome, c.cpf_cnpj, c.contato                 │
│ FROM clientes c                                             │
│ WHERE c.cpf_cnpj = ?                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 Princípios de Design Aplicados

```
┌──────────────────────────────────────────────────────────────┐
│                   PRINCÍPIOS SOLID                            │
└──────────────────────────────────────────────────────────────┘

S - Single Responsibility
  ✅ Cada Value Object tem uma única responsabilidade
  ✅ CpfCnpj: validar e representar CPF/CNPJ
  ✅ ValorMonetario: operações monetárias

O - Open/Closed
  ✅ Value Objects fechados para modificação
  ✅ Abertos para extensão (novos Value Objects)

L - Liskov Substitution
  ✅ Value Objects são substituíveis por seus valores
  ✅ Comportamento consistente

I - Interface Segregation
  ✅ Interfaces específicas para cada tipo
  ✅ Comparable, Serializable quando necessário

D - Dependency Inversion
  ✅ Entidades dependem de abstrações (Value Objects)
  ✅ Não dependem de tipos primitivos
```

---

## 📈 Evolução da Arquitetura

```
FASE 1 (Antes)                    FASE 2 (Depois)
┌──────────────┐                  ┌──────────────┐
│ Controller   │                  │ Controller   │
└──────┬───────┘                  └──────┬───────┘
       │                                 │
       │ DTO (primitivos)                │ DTO (primitivos)
       │                                 │
┌──────▼───────┐                  ┌──────▼───────┐
│ Service      │                  │ Service      │
│              │                  │              │
│ Validação    │                  │ Conversão    │
│ espalhada    │                  │ DTO → VO     │
└──────┬───────┘                  └──────┬───────┘
       │                                 │
       │ Entidade                        │ Entidade + VO
       │ (primitivos)                    │
┌──────▼───────┐                  ┌──────▼───────────────┐
│ Entidade     │                  │ Entidade             │
│              │                  │                      │
│ String cpf   │                  │ CpfCnpj cpfCnpj     │
│ String placa │                  │ Placa placa         │
│ BigDecimal $ │                  │ ValorMonetario $    │
└──────────────┘                  │                      │
                                  │ ┌────────────────┐  │
                                  │ │ Value Objects  │  │
                                  │ │ • Validação    │  │
                                  │ │ • Formatação   │  │
                                  │ │ • Operações    │  │
                                  │ └────────────────┘  │
                                  └─────────────────────┘

❌ Problemas:                     ✅ Benefícios:
• Validação espalhada            • Validação centralizada
• Código duplicado               • Reutilização
• Difícil manutenção             • Fácil manutenção
• Tipos primitivos               • Tipos de domínio
• Sem expressividade             • Alta expressividade
```

---

## 🎯 Conclusão Arquitetural

A arquitetura agora segue os princípios de **Domain-Driven Design**:

✅ **Camada de Domínio Rica**: Value Objects com lógica de negócio  
✅ **Separação de Responsabilidades**: Cada camada tem seu papel  
✅ **Validação em Cascata**: DTOs → Value Objects → Services  
✅ **Imutabilidade**: Value Objects garantem consistência  
✅ **Expressividade**: Código reflete o domínio do negócio  

A arquitetura está preparada para evoluir com:
- Agregados e Entidades Raiz
- Eventos de Domínio
- Arquitetura Hexagonal
- CQRS (Command Query Responsibility Segregation)

---

**Versão:** Fase 2 - Value Objects  
**Data:** 06/01/2026  
**Status:** ✅ Implementado e Validado

