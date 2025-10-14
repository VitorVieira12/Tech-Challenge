# 🚗 Tech Challenge - Sistema de Gestão de Oficina Mecânica

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

API RESTful para gerenciamento completo de oficina mecânica, desenvolvida com Spring Boot 3, JWT Authentication, documentação Swagger/OpenAPI e containerização Docker.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
- [Documentação da API](#documentação-da-api)
- [Autenticação](#autenticação)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Contribuindo](#contribuindo)

---

## 🎯 Sobre o Projeto

O **Tech Challenge** é uma aplicação completa de gerenciamento de oficina mecânica que permite:

- ✅ Gestão completa de **Clientes**, **Veículos**, **Peças** e **Serviços**
- ✅ Criação e acompanhamento de **Ordens de Serviço** (OS)
- ✅ **Controle de estoque** de peças com validação automática
- ✅ **Gestão de status** de OS com validação de transições
- ✅ **Consulta pública** para clientes acompanharem suas OSs
- ✅ **Monitoramento** de tempo médio de execução
- ✅ **Autenticação JWT** para endpoints administrativos
- ✅ **Documentação interativa** com Swagger/OpenAPI

---

## 🚀 Tecnologias

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.6** - Framework principal
- **Spring Security** - Segurança e autenticação
- **JWT (JSON Web Tokens)** - Autenticação stateless
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15** - Banco de dados relacional
- **Hibernate** - ORM
- **Bean Validation** - Validação de dados
- **Lombok** - Redução de boilerplate

### Documentação
- **SpringDoc OpenAPI** - Documentação automática da API
- **Swagger UI** - Interface interativa para testes

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocks para testes unitários
- **Testcontainers** - Testes de integração com PostgreSQL real
- **JaCoCo** - Cobertura de código (target: 80%)
- **Spring Boot Test** - Testes de integração

### DevOps
- **Docker** - Containerização
- **Docker Compose** - Orquestração de containers
- **Maven** - Gerenciamento de dependências

---

## ✨ Funcionalidades

### 1. Gestão de Clientes
- CRUD completo de clientes
- Validação de CPF/CNPJ

### 2. Gestão de Veículos
- CRUD completo de veículos
- Associação automática com clientes
- Validação de placas (formato antigo e Mercosul)

### 3. Gestão de Peças e Insumos
- CRUD completo de peças
- **Controle de estoque** em tempo real
- Ajuste incremental de estoque

### 4. Gestão de Serviços
- CRUD completo de serviços oferecidos
- Precificação de serviços

### 5. Ordens de Serviço (OS)
- **Criação automatizada** com:
  - Identificação de cliente por CPF/CNPJ
  - Cadastro automático de veículo (se novo)
  - Validação de estoque de peças
  - Geração automática de orçamento
  - Baixa automática em estoque

- **Gestão de Status** com validação:
  - `RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO`
  - `AGUARDANDO_APROVACAO` → `EM_EXECUCAO`
  - `EM_EXECUCAO` → `FINALIZADA` → `ENTREGUE`

- **Consulta Pública**:
  - Clientes podem consultar suas OSs via CPF/CNPJ
  - Endpoint público (sem JWT)
  - Dados seguros (sem informações sensíveis)

- **Monitoramento**:
  - Tempo médio de execução
  - Estatísticas (mín, máx, quantidade)

### 6. Segurança
- **Autenticação JWT** em todos os endpoints administrativos
- Endpoint público para consulta de clientes
- Credenciais padrão: `admin/admin`

---

## 📦 Pré-requisitos

### Opção 1: Executar com Docker (Recomendado)
- **Docker** 20.10+
- **Docker Compose** 2.0+

### Opção 2: Executar localmente
- **Java 21**
- **Maven 3.9+**
- **PostgreSQL 15+**

---

## 🎮 Instalação e Execução

### 🐳 Opção 1: Docker Compose (Recomendado)

A maneira mais simples de executar o projeto:

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/tech-challenge.git
cd tech-challenge

# 2. Execute o projeto
docker-compose up -d

# 3. Aguarde a aplicação iniciar (cerca de 60 segundos)
# Acompanhe os logs:
docker-compose logs -f app

# 4. Acesse a aplicação
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**Pronto!** A aplicação e o banco de dados estão rodando.

#### Comandos Úteis Docker

```bash
# Parar os containers
docker-compose down

# Parar e remover volumes (limpa banco de dados)
docker-compose down -v

# Rebuild da aplicação
docker-compose up --build

# Ver logs
docker-compose logs -f

# Ver apenas logs da aplicação
docker-compose logs -f app

# Ver apenas logs do banco
docker-compose logs -f postgres
```

---

### 💻 Opção 2: Executar Localmente

#### 1. Configurar o Banco de Dados

```bash
# Criar banco de dados PostgreSQL
createdb tech_challenge

# Ou via psql:
psql -U postgres
CREATE DATABASE tech_challenge;
\q
```

#### 2. Configurar application.yml

Edite `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tech_challenge
    username: seu_usuario
    password: sua_senha
```

#### 3. Executar a Aplicação

```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou com Maven instalado:
mvn spring-boot:run
```

#### 4. Acessar

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 📚 Documentação da API

### Swagger UI (Recomendado)

Acesse a documentação interativa em: **http://localhost:8080/swagger-ui.html**

**Funcionalidades do Swagger:**
- ✅ Visualização de todos os endpoints
- ✅ Testes interativos
- ✅ Autenticação JWT integrada
- ✅ Exemplos de request/response

### OpenAPI JSON

Acesse: **http://localhost:8080/v3/api-docs**

### Documentação Markdown

Consulte também:
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Referência completa
- **[API_EXAMPLES.http](API_EXAMPLES.http)** - Exemplos práticos
- **[GESTAO_OS_GUIDE.md](GESTAO_OS_GUIDE.md)** - Guia de Gestão de OS

---

## 🔐 Autenticação

### 1. Obter Token JWT

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "expiresIn": 86400000
}
```

### 2. Usar o Token

#### Via cURL:
```bash
curl -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  http://localhost:8080/api/clientes
```

#### Via Swagger UI:
1. Clique no botão **"Authorize"** (🔓)
2. Cole o token no campo
3. Clique em **"Authorize"**
4. Agora todos os endpoints protegidos funcionarão

#### Via Postman/Insomnia:
1. Aba **Authorization**
2. Type: **Bearer Token**
3. Cole o token

### 3. Endpoints Públicos (sem JWT)

- `POST /api/auth/login` - Login
- `GET /api/ordens-servico/status/{id}?cpfCnpj=xxx` - Consulta pública

---

## 🧪 Testes

### Executar Todos os Testes

```bash
# Com Maven Wrapper
./mvnw test

# Com Maven instalado
mvn test
```

### Executar Testes com Cobertura

```bash
./mvnw clean test jacoco:report
```

**Relatório gerado em:** `target/site/jacoco/index.html`

### Tipos de Testes

1. **Testes Unitários** (`src/test/java/.../service/`)
   - Testa lógica de negócio isoladamente
   - Usa Mockito para mocks
   - Rápidos e independentes

2. **Testes de Integração** (`src/test/java/.../integration/`)
   - Testa fluxo completo da API
   - Usa Testcontainers (PostgreSQL real)
   - Testa autenticação JWT
   - Testa validações end-to-end

### Cobertura de Código

- **Target:** 80% de cobertura
- **Plugin:** JaCoCo
- Verificação automática no build

---

## 📁 Estrutura do Projeto

```
tech-challenge/
├── src/
│   ├── main/
│   │   ├── java/com/techchallenge/
│   │   │   ├── config/              # Configurações (OpenAPI)
│   │   │   ├── controller/          # Camada de controle (REST)
│   │   │   ├── domain/
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── exception/       # Exceções customizadas
│   │   │   │   ├── model/           # Entidades JPA
│   │   │   │   ├── repository/      # Repositórios JPA
│   │   │   │   └── service/         # Lógica de negócio
│   │   │   ├── security/            # Segurança JWT
│   │   │   └── TechChallengeApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configurações
│   │       └── scripts/             # Scripts SQL
│   └── test/
│       ├── java/com/techchallenge/
│       │   ├── service/             # Testes unitários
│       │   └── integration/         # Testes de integração
│       └── resources/
│           └── application-test.yml # Config de testes
├── Dockerfile                       # Imagem Docker
├── docker-compose.yml               # Orquestração
├── pom.xml                          # Dependências Maven
└── README.md                        # Este arquivo
```

---

## 🎯 Fluxo de Uso Típico

### 1. Autenticação
```bash
POST /api/auth/login
Body: {"username": "admin", "password": "admin"}
```

### 2. Cadastrar Cliente
```bash
POST /api/clientes
Header: Authorization: Bearer TOKEN
Body: {
  "nome": "João Silva",
  "cpfCnpj": "12345678901",
  "contato": "joao@email.com"
}
```

### 3. Cadastrar Peças e Serviços
```bash
POST /api/pecas-insumos
POST /api/servicos
```

### 4. Criar Ordem de Serviço
```bash
POST /api/ordens-servico
Body: {
  "cpfCnpjCliente": "12345678901",
  "veiculo": {...},
  "servicos": [...],
  "pecas": [...]
}
```

### 5. Cliente Consulta Status (Público)
```bash
GET /api/ordens-servico/status/1?cpfCnpj=12345678901
# Não precisa de token JWT!
```

### 6. Gerenciar Status da OS
```bash
PATCH /api/ordens-servico/1/status
Body: {
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Cliente aprovou"
}
```

---

## 📊 Endpoints Principais

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| **Autenticação** |
| POST | `/api/auth/login` | Login (obter JWT) | ❌ |
| **Clientes** |
| GET | `/api/clientes` | Listar clientes | ✅ |
| POST | `/api/clientes` | Criar cliente | ✅ |
| GET | `/api/clientes/{id}` | Buscar por ID | ✅ |
| PUT | `/api/clientes/{id}` | Atualizar | ✅ |
| DELETE | `/api/clientes/{id}` | Deletar | ✅ |
| **Veículos** |
| GET | `/api/veiculos` | Listar veículos | ✅ |
| POST | `/api/veiculos` | Criar veículo | ✅ |
| **Peças** |
| GET | `/api/pecas-insumos` | Listar peças | ✅ |
| POST | `/api/pecas-insumos` | Criar peça | ✅ |
| PATCH | `/api/pecas-insumos/{id}/estoque` | Ajustar estoque | ✅ |
| **Serviços** |
| GET | `/api/servicos` | Listar serviços | ✅ |
| POST | `/api/servicos` | Criar serviço | ✅ |
| **Ordens de Serviço** |
| POST | `/api/ordens-servico` | Criar OS | ✅ |
| GET | `/api/ordens-servico` | Listar OSs | ✅ |
| GET | `/api/ordens-servico/{id}` | Buscar por ID | ✅ |
| PATCH | `/api/ordens-servico/{id}/status` | Alterar status | ✅ |
| GET | `/api/ordens-servico/status/{id}` | Consulta pública | ❌ |
| GET | `/api/ordens-servico/monitoramento/tempo-medio` | Estatísticas | ✅ |

**Legenda:** ✅ Requer JWT | ❌ Público

---

## 🔧 Variáveis de Ambiente

### Docker Compose

Já configurado no `docker-compose.yml`. Para personalizar, edite o arquivo:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/tech_challenge
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: 123456
  JWT_SECRET: sua_chave_secreta_aqui
  JWT_EXPIRATION: 86400000
```

### Execução Local

Configure no `application.yml` ou via variáveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tech_challenge
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=123456
export JWT_SECRET=sua_chave_secreta
export JWT_EXPIRATION=86400000
```

---

## 🐛 Troubleshooting

### Problema: Porta 8080 já em uso
```bash
# Mude a porta no docker-compose.yml:
ports:
  - "8081:8080"  # Host:Container
```

### Problema: Erro de conexão com banco
```bash
# Verifique se o PostgreSQL está rodando:
docker-compose ps

# Veja logs do banco:
docker-compose logs postgres

# Reinicie os containers:
docker-compose restart
```

### Problema: Testes falhando
```bash
# Certifique-se que o Docker está rodando (para Testcontainers)
docker info

# Execute testes individualmente:
./mvnw test -Dtest=AuthControllerIntegrationTest
```

### Problema: Token JWT inválido
- Verifique se o token não expirou (24 horas)
- Faça login novamente para obter novo token
- Certifique-se de usar `Bearer TOKEN` no header

---

## 📖 Documentação Adicional

- **[GESTAO_OS_GUIDE.md](GESTAO_OS_GUIDE.md)** - Guia completo de Gestão de OS
- **[TESTE_RAPIDO_GESTAO_OS.md](TESTE_RAPIDO_GESTAO_OS.md)** - Testes práticos
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Referência da API
- **[API_EXAMPLES.http](API_EXAMPLES.http)** - Exemplos HTTP
- **[CHANGELOG_GESTAO_OS.md](CHANGELOG_GESTAO_OS.md)** - Histórico de mudanças
- **[RESUMO_IMPLEMENTACAO.md](RESUMO_IMPLEMENTACAO.md)** - Resumo técnico

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👥 Autores

- **Tech Challenge Team** - [GitHub](https://github.com/seu-usuario)

---

## 🙏 Agradecimentos

- Spring Boot Team
- PostgreSQL Community
- Docker Community
- Testcontainers Team

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Consulte a [documentação](#documentação-da-api)
2. Veja o [troubleshooting](#-troubleshooting)
3. Abra uma [issue](https://github.com/seu-usuario/tech-challenge/issues)

---

**⭐ Se este projeto foi útil, deixe uma estrela!**

**🚀 Desenvolvido com dedicação para o Tech Challenge**
