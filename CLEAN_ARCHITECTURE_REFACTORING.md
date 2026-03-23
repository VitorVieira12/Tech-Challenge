# Clean Architecture Refactoring - Fase 3

## 📋 Feedback do Professor (Fase 2)

O professor apontou as seguintes violações de Clean Architecture:

1. ❌ Controller não instancia dependências (Gateways e Presenters)
2. ❌ Uso direto de frameworks em componentes internos
3. ❌ Falta separação entre Controller REST e Controller Clean Arch
4. ❌ Gateway expõe entidades do domínio (deveria usar DTOs)
5. ❌ Presenters não implementados

## ✅ Solução Implementada

### Arquitetura Hexagonal Completa

```
┌─────────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                       │
│                    (Framework Specific)                      │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  REST Controllers (ClienteController)                 │  │
│  │  - Recebe HTTP requests                               │  │
│  │  - Valida dados (Bean Validation)                     │  │
│  │  - Converte Request DTO → Input DTO                   │  │
│  │  - Invoca Use Cases                                   │  │
│  │  - Converte Output DTO → Response DTO                 │  │
│  │  - Retorna HTTP responses                             │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Configuration (ClienteUseCaseConfig)                 │  │
│  │  - Instancia Use Cases                                │  │
│  │  - Injeta Gateways e Presenters                       │  │
│  │  - Gerencia dependências Spring                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Exception Handlers (GlobalExceptionHandler)          │  │
│  │  - Traduz exceções de domínio para HTTP              │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                     ADAPTERS LAYER                           │
│                   (Interface Adapters)                       │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Gateways (ClienteGatewayImpl)                        │  │
│  │  - Implementa interface ClienteGateway                │  │
│  │  - Converte Domain Entity ↔ JPA Entity              │  │
│  │  - Adapta Spring Data JPA Repository                 │  │
│  │  - NUNCA expõe JPA entities para fora                │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Presenters (ClientePresenterImpl)                    │  │
│  │  - Implementa interface ClientePresenter              │  │
│  │  - Converte Domain Entity → Output DTO               │  │
│  │  - Formata dados para apresentação                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Persistence (ClienteJpaEntity, ClienteJpaRepository) │  │
│  │  - Entidades JPA (separadas do domínio)              │  │
│  │  - Spring Data JPA repositories                       │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                       CORE LAYER                             │
│                   (Business Logic)                           │
│                  LIVRE DE FRAMEWORKS                         │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Use Cases                                            │  │
│  │  - CriarClienteUseCase                                │  │
│  │  - BuscarClienteUseCase                               │  │
│  │  - ListarClientesUseCase                              │  │
│  │  - AtualizarClienteUseCase                            │  │
│  │  - DeletarClienteUseCase                              │  │
│  │                                                        │  │
│  │  Orquestram lógica de aplicação                       │  │
│  │  Usam Gateways e Presenters (interfaces)              │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Domain Entities (Cliente - pure Java)                │  │
│  │  - Regras de negócio                                  │  │
│  │  - Value Objects (CpfCnpj, Contato)                  │  │
│  │  - SEM anotações de framework                         │  │
│  │  - Imutáveis e auto-validáveis                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Interfaces (Ports)                                    │  │
│  │  - ClienteGateway (output port)                       │  │
│  │  - ClientePresenter (output port)                     │  │
│  │  - DTOs (Input/Output)                                │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## 🔧 Estrutura de Pacotes

```
src/main/java/com/techchallenge/
│
├── core/                                    # CORE LAYER (domínio)
│   ├── domain/
│   │   ├── Cliente.java                     # Entidade pura (SEM JPA)
│   │   ├── valueobject/
│   │   │   ├── CpfCnpj.java                 # Value Object
│   │   │   └── Contato.java                 # Value Object
│   │   └── exception/
│   │       └── DomainValidationException.java
│   │
│   └── usecase/
│       └── cliente/
│           ├── CriarClienteUseCase.java     # Caso de uso
│           ├── BuscarClienteUseCase.java
│           ├── ListarClientesUseCase.java
│           ├── AtualizarClienteUseCase.java
│           ├── DeletarClienteUseCase.java
│           ├── dto/
│           │   ├── ClienteInputDTO.java     # DTO entrada (pure)
│           │   └── ClienteOutputDTO.java    # DTO saída (pure)
│           ├── gateway/
│           │   └── ClienteGateway.java      # Interface (port)
│           ├── presenter/
│           │   └── ClientePresenter.java    # Interface (port)
│           └── exception/
│               ├── ResourceNotFoundException.java
│               └── DuplicateResourceException.java
│
├── adapters/                                # ADAPTERS LAYER
│   ├── gateway/
│   │   └── ClienteGatewayImpl.java          # Implementa gateway
│   ├── presenter/
│   │   └── ClientePresenterImpl.java        # Implementa presenter
│   └── persistence/
│       ├── ClienteJpaEntity.java            # Entidade JPA (separada!)
│       └── ClienteJpaRepository.java        # Spring Data JPA
│
└── infrastructure/                          # INFRASTRUCTURE LAYER
    ├── web/
    │   ├── controller/
    │   │   └── ClienteController.java       # REST Controller
    │   ├── dto/
    │   │   ├── ClienteRequestDTO.java       # DTO HTTP request
    │   │   └── ClienteResponseDTO.java      # DTO HTTP response
    │   └── exception/
    │       ├── GlobalExceptionHandler.java
    │       ├── ErrorResponse.java
    │       └── ValidationErrorResponse.java
    └── config/
        └── ClienteUseCaseConfig.java        # Configuração Spring
```

## 🎯 Correções Aplicadas

### 1. ✅ Controller Instancia Dependências

**Antes (❌):**
```java
@RestController
public class ClienteController {
    private final ClienteService service; // Service faz tudo
    
    // Controller apenas delega para service
}
```

**Depois (✅):**
```java
@RestController
public class ClienteController {
    private final CriarClienteUseCase criarUseCase;
    private final BuscarClienteUseCase buscarUseCase;
    // ... outros use cases
    
    public ClienteController(
            CriarClienteUseCase criarUseCase,
            BuscarClienteUseCase buscarUseCase, ...) {
        // Injeta use cases via construtor
        // Use cases são instanciados em ClienteUseCaseConfig
    }
}
```

### 2. ✅ Sem Frameworks em Componentes Internos

**Antes (❌):**
```java
@Entity  // JPA no domínio!
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue
    private Long id;
    // Domínio acoplado ao JPA
}
```

**Depois (✅):**
```java
// core/domain/Cliente.java - Pure Java
public class Cliente {
    private Long id;
    private String nome;
    // SEM anotações de framework!
}

// adapters/persistence/ClienteJpaEntity.java - JPA separado
@Entity
@Table(name = "clientes")
public class ClienteJpaEntity {
    @Id
    @GeneratedValue
    private Long id;
    // Framework isolado na camada de adapters
}
```

### 3. ✅ Separação Controller REST vs Controller Clean Arch

**Estrutura:**
- `infrastructure/web/controller/ClienteController.java` → Controller REST (Spring)
- `core/usecase/cliente/CriarClienteUseCase.java` → Lógica de aplicação (Clean Arch)

**Responsabilidades claras:**
- **REST Controller**: HTTP, validação, conversão de DTOs
- **Use Case**: Lógica de aplicação, orquestração
- **Domain Entity**: Regras de negócio

### 4. ✅ Gateway NÃO Expõe Entidades

**Antes (❌):**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Retorna entidade JPA diretamente
}

@Service
public class ClienteService {
    public Cliente criar(ClienteDTO dto) {
        Cliente cliente = new Cliente(); // JPA entity
        return repository.save(cliente); // Vaza JPA
    }
}
```

**Depois (✅):**
```java
// Interface Gateway (core)
public interface ClienteGateway {
    Cliente salvar(Cliente cliente); // Cliente do DOMÍNIO
    Optional<Cliente> buscarPorId(Long id);
}

// Implementação Gateway (adapters)
@Component
public class ClienteGatewayImpl implements ClienteGateway {
    private final ClienteJpaRepository jpaRepository;
    
    @Override
    public Cliente salvar(Cliente clienteDominio) {
        // 1. Converte Domain → JPA
        ClienteJpaEntity entity = paraEntity(clienteDominio);
        
        // 2. Salva JPA
        ClienteJpaEntity saved = jpaRepository.save(entity);
        
        // 3. Converte JPA → Domain
        return paraDominio(saved); // NUNCA retorna JPA entity!
    }
}
```

### 5. ✅ Presenters Implementados

**Antes (❌):**
```java
// DTO converte diretamente da entidade
public class ClienteResponseDTO {
    public static ClienteResponseDTO fromEntity(Cliente entity) {
        // Acoplamento direto
    }
}
```

**Depois (✅):**
```java
// Interface Presenter (core)
public interface ClientePresenter {
    ClienteOutputDTO paraOutput(Cliente cliente);
}

// Implementação Presenter (adapters)
@Component
public class ClientePresenterImpl implements ClientePresenter {
    @Override
    public ClienteOutputDTO paraOutput(Cliente cliente) {
        return new ClienteOutputDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpfCnpj().getValor(),
            cliente.getContato().getValor(),
            cliente.isAtivo()
        );
    }
}
```

## 🔄 Fluxo de Dados Completo

### Criação de Cliente (POST /api/clientes)

```
1. HTTP Request
   ↓
2. ClienteController (infrastructure)
   - Valida ClienteRequestDTO (Bean Validation)
   - Converte ClienteRequestDTO → ClienteInputDTO
   ↓
3. CriarClienteUseCase (core)
   - Verifica duplicação via ClienteGateway
   - Cria entidade Cliente (domínio)
   - Aplica regras de negócio
   - Salva via ClienteGateway
   - Formata via ClientePresenter
   ↓
4. ClienteGatewayImpl (adapters)
   - Converte Cliente (domínio) → ClienteJpaEntity
   - Salva via ClienteJpaRepository
   - Converte ClienteJpaEntity → Cliente (domínio)
   ↓
5. ClientePresenterImpl (adapters)
   - Converte Cliente (domínio) → ClienteOutputDTO
   ↓
6. ClienteController (infrastructure)
   - Converte ClienteOutputDTO → ClienteResponseDTO
   - Retorna HTTP Response 201 Created
```

## 🧪 Testabilidade

A arquitetura hexagonal facilita MUITO os testes:

### Testes de Use Case (sem framework)

```java
@Test
void deveCriarCliente() {
    // Mocks simples (sem Spring)
    ClienteGateway gateway = mock(ClienteGateway.class);
    ClientePresenter presenter = mock(ClientePresenter.class);
    
    // Use Case puro
    CriarClienteUseCase useCase = new CriarClienteUseCase(gateway, presenter);
    
    // Teste
    ClienteInputDTO input = new ClienteInputDTO("João", "12345678901", "joao@email.com");
    useCase.executar(input);
    
    verify(gateway).salvar(any());
}
```

### Testes de Controller (com Spring)

```java
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {
    @MockBean CriarClienteUseCase criarUseCase;
    
    @Test
    void deveCriarCliente() throws Exception {
        mockMvc.perform(post("/api/clientes")
            .content("{\"nome\":\"João\",\"cpfCnpj\":\"12345678901\",\"contato\":\"joao@email.com\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }
}
```

## 📚 Benefícios da Refatoração

### 1. **Separação de Responsabilidades**
- Core: Lógica de negócio
- Adapters: Conversões e adaptações
- Infrastructure: Frameworks e I/O

### 2. **Testabilidade**
- Use Cases testáveis SEM Spring
- Domain testável SEM banco de dados
- Mocks simples

### 3. **Manutenibilidade**
- Mudanças de framework não afetam core
- Regras de negócio centralizadas
- Código mais limpo e organizado

### 4. **Inversão de Dependências (SOLID)**
- Core define interfaces (ports)
- Adapters implementam interfaces
- Core NÃO depende de frameworks

### 5. **Flexibilidade**
- Fácil trocar JPA por MongoDB
- Fácil trocar REST por GraphQL
- Fácil adicionar novos casos de uso

## 🔜 Próximos Passos

Para aplicar este padrão aos demais módulos:

1. **Veiculo**: Seguir mesmo padrão
2. **PecaInsumo**: Seguir mesmo padrão
3. **Servico**: Seguir mesmo padrão
4. **OrdemDeServico**: Seguir mesmo padrão (mais complexo)

## 📖 Referências

- [Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [DDD: Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [SOLID Principles](https://www.digitalocean.com/community/conceptual_articles/s-o-l-i-d-the-first-five-principles-of-object-oriented-design)

## ✅ Feedback do Professor - Resolvido

| Problema | Status | Solução |
|----------|--------|---------|
| Controller não instancia dependências | ✅ Resolvido | ClienteUseCaseConfig injeta dependências |
| Uso direto de frameworks em componentes internos | ✅ Resolvido | Core livre de frameworks, JPA isolado em adapters |
| Falta separação Controller REST vs Clean Arch | ✅ Resolvido | REST Controller separado de Use Cases |
| Gateway expõe entidades do domínio | ✅ Resolvido | Gateway converte JPA ↔ Domain internamente |
| Presenters não implementados | ✅ Resolvido | ClientePresenter interface + implementação |

---

**Arquitetura implementada seguindo feedback do professor e best practices de Clean Architecture!** 🎉


