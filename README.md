# 🚗 Tech Challenge - Sistema de Gestão de Oficina Mecânica

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

API RESTful para gerenciamento completo de oficina mecânica, desenvolvida com Spring Boot 3, JWT Authentication, documentação Swagger/OpenAPI e containerização Docker.

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Funcionalidades](#-funcionalidades)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Documentação da API](#-documentação-da-api)
- [Autenticação](#-autenticação)
- [Testes](#-testes)
- [Arquitetura](#-arquitetura)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Endpoints Principais](#-endpoints-principais)
- [Troubleshooting](#-troubleshooting)
- [Documentação Adicional](#-documentação-adicional)

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
- ✅ **Containerização** com Docker e Docker Compose
- ✅ **Deploy em Kubernetes** com Horizontal Pod Autoscaler (HPA)
- ✅ **Infraestrutura como Código** com Terraform (AWS)

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

### DevOps & Cloud
- **Docker** - Containerização
- **Docker Compose** - Orquestração local
- **Kubernetes** - Orquestração em produção
- **Terraform** - Infraestrutura como código (AWS EKS)
- **Maven** - Gerenciamento de dependências e build

---

## ✨ Funcionalidades

### 1. Gestão de Clientes
- CRUD completo de clientes
- Validação de CPF/CNPJ com algoritmo oficial

### 2. Gestão de Veículos
- CRUD completo de veículos
- Associação automática com clientes
- Validação de placas (formato antigo e Mercosul)

### 3. Gestão de Peças e Insumos
- CRUD completo de peças
- **Controle de estoque** em tempo real
- Ajuste incremental de estoque
- Validação de disponibilidade

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

- **Aprovação de Orçamento**:
  - Fluxo de aprovação/recusa pelo cliente
  - Histórico de observações

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
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Referência completa da API
- **[API_EXAMPLES.http](API_EXAMPLES.http)** - Exemplos práticos com REST Client

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

3. **Testes de Value Objects** (`src/test/java/.../valueobject/`)
   - Testa validações de domínio
   - CPF/CNPJ, Placas, Valores Monetários

### Cobertura de Código

- **Target:** 80% de cobertura
- **Plugin:** JaCoCo
- Verificação automática no build

---

## 🏗️ Arquitetura

O projeto segue princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**.

### Camadas da Aplicação

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← REST Controllers
│  (API Endpoints, DTOs, Validation)  │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│          Service Layer              │  ← Business Logic
│   (Use Cases, Domain Services)      │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│          Domain Layer               │  ← Domain Models
│  (Entities, Value Objects, Rules)   │
└───────────────┬─────────────────────┘
                │
┌───────────────▼─────────────────────┐
│       Repository Layer              │  ← Data Access
│   (JPA Repositories, Database)      │
└─────────────────────────────────────┘
```

### Value Objects

O projeto utiliza **Value Objects** para garantir validação e integridade:

- **CpfCnpj** - Validação de CPF e CNPJ com algoritmo oficial
- **Placa** - Validação de placas (formato antigo e Mercosul)
- **ValorMonetario** - Representação segura de valores monetários
- **Contato** - Validação de email
- **AnoVeiculo** - Validação de ano de veículo

### Documentação Detalhada

- **[ARQUITETURA.md](ARQUITETURA.md)** - Arquitetura completa do sistema
- **[ESCALABILIDADE_ANALISE.md](ESCALABILIDADE_ANALISE.md)** - Análise de escalabilidade

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
│   │   │   │   ├── service/         # Lógica de negócio
│   │   │   │   ├── usecase/         # Casos de uso
│   │   │   │   └── valueobject/     # Value Objects (DDD)
│   │   │   ├── security/            # Segurança JWT
│   │   │   └── TechChallengeApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configurações
│   │       └── scripts/             # Scripts SQL
│   └── test/
│       ├── java/com/techchallenge/
│       │   ├── application/         # Testes de use cases
│       │   ├── domain/              # Testes de value objects
│       │   ├── service/             # Testes unitários de serviços
│       │   ├── integration/         # Testes de integração
│       │   └── config/              # Configurações de teste
│       └── resources/
│           └── application-test.yml # Config de testes (H2)
├── infra/
│   └── aws/                         # Terraform para AWS EKS
│       ├── main.tf                  # Configuração principal
│       ├── vpc.tf                   # Rede VPC
│       ├── eks.tf                   # Cluster Kubernetes
│       ├── rds.tf                   # Banco de dados PostgreSQL
│       └── README.md                # Documentação Terraform
├── k8s/                             # Manifests Kubernetes
│   ├── app-deployment.yaml          # Deployment da aplicação
│   ├── app-service.yaml             # Service (LoadBalancer)
│   ├── hpa.yaml                     # Horizontal Pod Autoscaler
│   ├── configmap.yaml               # Configurações
│   ├── secret.yaml                  # Credenciais
│   ├── postgres-*.yaml              # PostgreSQL
│   └── README.md                    # Documentação K8s
├── scripts/                         # Scripts úteis
│   ├── check-deployment.sh          # Verificar deployment
│   ├── setup-cicd.sh               # Setup CI/CD
│   └── payloads/                    # Exemplos de payloads
├── Dockerfile                       # Imagem Docker
├── docker-compose.yml               # Orquestração local
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

### 6. Aprovar/Recusar Orçamento
```bash
POST /api/ordens-servico/1/aprovar-orcamento
Body: {
  "aprovado": true,
  "motivoRecusa": null
}
```

### 7. Gerenciar Status da OS
```bash
PATCH /api/ordens-servico/1/status
Body: {
  "novoStatus": "EM_EXECUCAO",
  "observacao": "Iniciando reparos"
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
| GET | `/api/veiculos/{id}` | Buscar por ID | ✅ |
| PUT | `/api/veiculos/{id}` | Atualizar | ✅ |
| DELETE | `/api/veiculos/{id}` | Deletar | ✅ |
| **Peças** |
| GET | `/api/pecas-insumos` | Listar peças | ✅ |
| POST | `/api/pecas-insumos` | Criar peça | ✅ |
| GET | `/api/pecas-insumos/{id}` | Buscar por ID | ✅ |
| PUT | `/api/pecas-insumos/{id}` | Atualizar | ✅ |
| DELETE | `/api/pecas-insumos/{id}` | Deletar | ✅ |
| PATCH | `/api/pecas-insumos/{id}/estoque` | Ajustar estoque | ✅ |
| **Serviços** |
| GET | `/api/servicos` | Listar serviços | ✅ |
| POST | `/api/servicos` | Criar serviço | ✅ |
| GET | `/api/servicos/{id}` | Buscar por ID | ✅ |
| PUT | `/api/servicos/{id}` | Atualizar | ✅ |
| DELETE | `/api/servicos/{id}` | Deletar | ✅ |
| **Ordens de Serviço** |
| POST | `/api/ordens-servico` | Criar OS | ✅ |
| GET | `/api/ordens-servico` | Listar OSs | ✅ |
| GET | `/api/ordens-servico/{id}` | Buscar por ID | ✅ |
| PATCH | `/api/ordens-servico/{id}/status` | Alterar status | ✅ |
| POST | `/api/ordens-servico/{id}/aprovar-orcamento` | Aprovar/recusar orçamento | ✅ |
| GET | `/api/ordens-servico/em-andamento` | Listar OSs ordenadas | ✅ |
| GET | `/api/ordens-servico/status/{id}` | Consulta pública | ❌ |
| GET | `/api/ordens-servico/monitoramento/tempo-medio` | Estatísticas | ✅ |

**Legenda:** ✅ Requer JWT | ❌ Público

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

### Problema: Build falha no Maven
```bash
# Limpe o cache do Maven
./mvnw clean

# Rebuild completo
./mvnw clean install
```

---

## 📖 Documentação Adicional

### Documentação Principal
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Referência completa da API
- **[ARQUITETURA.md](ARQUITETURA.md)** - Arquitetura detalhada do sistema
- **[ESCALABILIDADE_ANALISE.md](ESCALABILIDADE_ANALISE.md)** - Análise de escalabilidade

### Infraestrutura
- **[infra/aws/README.md](infra/aws/README.md)** - Terraform AWS EKS
- **[k8s/README.md](k8s/README.md)** - Kubernetes manifests

### Exemplos
- **[API_EXAMPLES.http](API_EXAMPLES.http)** - Exemplos práticos de requisições

---

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👥 Autores

**Tech Challenge Team**

- Desenvolvido como projeto acadêmico
- Implementa boas práticas de desenvolvimento
- Arquitetura escalável e moderna

---

## 🔗 Links Úteis

### 🌐 Deploy em Produção (AWS)
- **API Base URL:** http://a8ee8070f02404420b7817f981e33463-d5a383da039125b0.elb.us-east-1.amazonaws.com
- **Swagger UI (Produção):** http://a8ee8070f02404420b7817f981e33463-d5a383da039125b0.elb.us-east-1.amazonaws.com/swagger-ui.html
- **OpenAPI Docs (Produção):** http://a8ee8070f02404420b7817f981e33463-d5a383da039125b0.elb.us-east-1.amazonaws.com/v3/api-docs
- **Health Check:** http://a8ee8070f02404420b7817f981e33463-d5a383da039125b0.elb.us-east-1.amazonaws.com/actuator/health

### 🏗️ Repositórios da Fase 3
- **[tech-challenge-infra-db](https://github.com/VitorVieira12/tech-challenge-infra-db)** — Terraform RDS PostgreSQL
- **[tech-challenge-infra-k8s](https://github.com/VitorVieira12/tech-challenge-infra-k8s)** — Terraform EKS + Kubernetes
- **[tech-challenge-lambda](https://github.com/VitorVieira12/tech-challenge-lambda)** — Lambda Autenticação CPF

### 📊 Monitoramento
- **New Relic Dashboard:** https://one.newrelic.com (App: `Tech Challenge - Oficina`)

### 📚 Documentação
- **Swagger UI Local:** http://localhost:8080/swagger-ui.html
- **OpenAPI Docs Local:** http://localhost:8080/v3/api-docs
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Kubernetes:** https://kubernetes.io/
- **Terraform:** https://www.terraform.io/

---

## 🏗️ Arquitetura Fase 3 — Visão Geral

```
                        ┌─────────────────────────────────┐
                        │           AWS us-east-1          │
                        │                                 │
  Cliente               │  ┌──────────────────────────┐  │
  (CPF/CNPJ) ─────────▶│  │      API Gateway          │  │
                        │  │   (HTTP API v2)           │  │
                        │  └────────────┬─────────────┘  │
                        │               │                 │
                        │    ┌──────────▼──────────┐     │
                        │    │   Lambda (Auth)      │     │
                        │    │   AuthHandler.java   │     │
                        │    │   → Valida CPF       │     │
                        │    │   → Gera JWT Token   │     │
                        │    └──────────┬──────────┘     │
                        │               │                 │
  JWT Token ◀────────── │  ─────────────┘                 │
                        │                                 │
  API Requests          │  ┌──────────────────────────┐  │
  (com JWT) ──────────▶ │  │  EKS Cluster (t3.small)  │  │
                        │  │  ┌────────────────────┐   │  │
                        │  │  │  Spring Boot App    │   │  │
                        │  │  │  + New Relic Agent  │   │  │
                        │  │  │  + HPA (auto-scale) │   │  │
                        │  │  └────────────────────┘   │  │
                        │  └──────────────┬─────────────┘  │
                        │                 │                 │
                        │  ┌──────────────▼─────────────┐  │
                        │  │      RDS PostgreSQL 15      │  │
                        │  │    tech-challenge-db        │  │
                        │  └────────────────────────────┘  │
                        │                                 │
                        │  ┌──────────────────────────┐  │
                        │  │  New Relic               │  │
                        │  │  (Monitoramento)         │  │
                        │  └──────────────────────────┘  │
                        └─────────────────────────────────┘
```

## 🤝 Colaborador avaliador

- **soat-architecture** — adicionado como colaborador para avaliação

---

**Desenvolvido com ❤️ usando Spring Boot 3, Java 21, AWS EKS, Lambda e Terraform**
