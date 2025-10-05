# Tech Challenge - Sistema de Gestão de Oficina

Sistema de gerenciamento de oficina mecânica desenvolvido com Spring Boot 3.5.6, Java 21 e PostgreSQL.

## 📋 Funcionalidades

- ✅ **Gestão de Clientes**: Cadastro completo com validação de CPF/CNPJ
- ✅ **Gestão de Veículos**: Cadastro com validação de placa brasileira (formato antigo e Mercosul)
- ✅ **Gestão de Peças e Insumos**: Controle de estoque e preços
- ✅ **Gestão de Serviços**: Catálogo de serviços oferecidos
- ✅ **Criação de Ordem de Serviço**: Fluxo completo automatizado
  - Identificação do cliente por CPF/CNPJ
  - Cadastro automático de veículo (se necessário)
  - Validação de estoque de peças
  - Geração automática de orçamento
  - Controle de status da OS
  - Baixa automática no estoque
- ✅ **Validações Robustas**: Validação de dados sensíveis (CPF/CNPJ, placas)
- ✅ **Tratamento de Exceções**: Respostas de erro padronizadas
- ✅ **API RESTful**: Seguindo convenções REST

## 🛠 Tecnologias

- **Java 21**
- **Spring Boot 3.5.6**
  - Spring Data JPA
  - Spring Web
  - Spring Validation
  - Spring Security
- **PostgreSQL**
- **Lombok**
- **Maven**

## 📦 Pré-requisitos

- JDK 21+
- PostgreSQL 12+
- Maven 3.6+

## 🚀 Configuração e Execução

### 1. Configurar o Banco de Dados

Crie o banco de dados PostgreSQL:

```sql
CREATE DATABASE tech_challenge;
```

### 2. Configurar Credenciais

As configurações do banco estão no arquivo `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tech_challenge
    username: postgres
    password: 123456
```

**Ajuste as credenciais conforme seu ambiente.**

### 3. Executar o Projeto

```bash
# Compilar o projeto
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### 4. Criar as Tabelas

O Hibernate está configurado com `ddl-auto: update`, então as tabelas serão criadas automaticamente na primeira execução.

Alternativamente, você pode executar o script SQL:

```bash
psql -U postgres -d tech_challenge -f src/main/resources/scripts/create-database.sql
```

## 🔐 Autenticação

A aplicação utiliza Spring Security com autenticação básica:

- **Usuário**: `admin`
- **Senha**: `admin`

## 📚 Documentação da API

Consulte o arquivo [API_DOCUMENTATION.md](API_DOCUMENTATION.md) para detalhes completos sobre todos os endpoints.

### Endpoints Principais

- **Clientes**: `/api/clientes`
- **Veículos**: `/api/veiculos`
- **Peças/Insumos**: `/api/pecas-insumos`
- **Serviços**: `/api/servicos`
- **Ordens de Serviço**: `/api/ordens-servico`

### Exemplo de Requisição

```bash
# Criar um cliente
curl -X POST http://localhost:8080/api/clientes \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "cpfCnpj": "12345678901",
    "contato": "joao@email.com"
  }'
```

## 🏗 Arquitetura

```
src/main/java/com/techchallenge/
├── controller/              # Controladores REST
│   ├── ClienteController.java
│   ├── VeiculoController.java
│   ├── PecaInsumoController.java
│   └── ServicoController.java
├── domain/
│   ├── dto/                 # Data Transfer Objects
│   │   ├── ClienteDTO.java
│   │   ├── ClienteResponseDTO.java
│   │   └── ...
│   ├── exception/           # Exceções customizadas
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── model/               # Entidades JPA
│   │   ├── Cliente.java
│   │   ├── Veiculo.java
│   │   ├── PecaInsumo.java
│   │   └── Servico.java
│   ├── repository/          # Repositórios JPA
│   │   ├── ClienteRepository.java
│   │   ├── VeiculoRepository.java
│   │   ├── PecaInsumoRepository.java
│   │   └── ServicoRepository.java
│   └── service/             # Lógica de negócio
│       ├── ClienteService.java
│       ├── VeiculoService.java
│       ├── PecaInsumoService.java
│       └── ServicoService.java
└── TechChallengeApplication.java
```

## ✅ Validações Implementadas

### Cliente
- Nome: 3-100 caracteres
- CPF/CNPJ: 11 ou 14 dígitos numéricos
- Contato: 8-100 caracteres
- CPF/CNPJ único no sistema

### Veículo
- Placa: formato ABC1234 ou ABC1D23
- Marca: 2-50 caracteres
- Modelo: 2-50 caracteres
- Ano: 1900-2100
- Placa única no sistema

### Peça/Insumo
- Nome: 3-100 caracteres
- Descrição: até 500 caracteres
- Preço: > 0, máx 8 dígitos inteiros e 2 decimais
- Quantidade em estoque: >= 0

### Serviço
- Descrição: 5-200 caracteres
- Preço: > 0, máx 8 dígitos inteiros e 2 decimais

## 🚀 Fluxo Principal: Criação de Ordem de Serviço

O sistema permite criar uma OS completa com um único endpoint:

```bash
# 1. Primeiro, cadastre um cliente
curl -X POST http://localhost:8080/api/clientes \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"nome": "João Silva", "cpfCnpj": "12345678901", "contato": "joao@email.com"}'

# 2. Cadastre serviços e peças (se ainda não existirem)
curl -X POST http://localhost:8080/api/servicos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"descricao": "Troca de óleo e filtro", "preco": 150.00}'

curl -X POST http://localhost:8080/api/pecas-insumos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"nome": "Filtro de Óleo", "descricao": "Original", "preco": 45.90, "quantidadeEstoque": 100}'

# 3. Crie a Ordem de Serviço (fluxo completo automático)
curl -X POST http://localhost:8080/api/ordens-servico \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCnpjCliente": "12345678901",
    "veiculo": {
      "placa": "ABC1234",
      "marca": "Toyota",
      "modelo": "Corolla",
      "ano": 2020
    },
    "servicos": [{"servicoId": 1, "quantidade": 1}],
    "pecas": [{"pecaInsumoId": 1, "quantidade": 2}],
    "observacoes": "Revisão completa"
  }'
```

**O que acontece automaticamente:**
1. ✅ Valida se o cliente existe
2. ✅ Verifica se o veículo existe; se não, cadastra
3. ✅ Valida se há estoque suficiente das peças
4. ✅ Calcula o orçamento total
5. ✅ Cria a OS com status RECEBIDA
6. ✅ Baixa as peças do estoque
7. ✅ Simula envio do orçamento (muda para AGUARDANDO_APROVACAO)

## 🧪 Testando a API

### Criar Cliente
```bash
curl -X POST http://localhost:8080/api/clientes \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"nome": "Maria Santos", "cpfCnpj": "98765432100", "contato": "maria@email.com"}'
```

### Criar Veículo
```bash
curl -X POST http://localhost:8080/api/veiculos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"placa": "XYZ5678", "marca": "Honda", "modelo": "Civic", "ano": 2023, "clienteId": 1}'
```

### Criar Peça/Insumo
```bash
curl -X POST http://localhost:8080/api/pecas-insumos \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"nome": "Filtro de Ar", "descricao": "Filtro de ar original", "preco": 35.50, "quantidadeEstoque": 50}'
```

### Atualizar Estoque
```bash
# Adicionar 20 unidades
curl -X PATCH "http://localhost:8080/api/pecas-insumos/1/estoque?quantidadeAjuste=20" \
  -u admin:admin

# Remover 5 unidades
curl -X PATCH "http://localhost:8080/api/pecas-insumos/1/estoque?quantidadeAjuste=-5" \
  -u admin:admin
```

### Listar Todos os Clientes
```bash
curl -X GET http://localhost:8080/api/clientes \
  -u admin:admin
```

### Listar Veículos de um Cliente
```bash
curl -X GET "http://localhost:8080/api/veiculos?clienteId=1" \
  -u admin:admin
```

## 🔧 Configurações Adicionais

### Logs SQL
O sistema está configurado para mostrar as queries SQL no console:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
```

### Charset UTF-8
Configurado para garantir suporte a caracteres especiais.

## 📝 Notas de Desenvolvimento

### Abordagem PUT
O sistema utiliza PUT para atualizações completas de recursos, exigindo que o cliente envie todos os campos obrigatórios. Esta abordagem foi escolhida por ser mais simples e previsível.

A única exceção é o endpoint PATCH para atualização de estoque de peças, que permite ajustes incrementais.

### Transações
Todas as operações de escrita (criar, atualizar, deletar) são transacionais, garantindo consistência dos dados.

### Cascata
Ao deletar um cliente, seus veículos são deletados automaticamente devido à configuração `orphanRemoval=true`.

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto é um trabalho acadêmico desenvolvido para fins educacionais.

## 👥 Autores

Desenvolvido como parte do Tech Challenge.

---

**Observação**: Este é um projeto em desenvolvimento. Para uso em produção, considere:
- Implementar autenticação JWT
- Adicionar testes unitários e de integração
- Implementar cache
- Adicionar documentação Swagger/OpenAPI
- Configurar profiles (dev, test, prod)
- Implementar CI/CD

