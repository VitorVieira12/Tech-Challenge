# 🚗 Tech Challenge - Sistema de Gestão de Oficina Mecânica

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)
[![Terraform](https://img.shields.io/badge/Terraform-1.6-7B42BC.svg)](https://www.terraform.io/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF.svg)](https://github.com/features/actions)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

API RESTful para gerenciamento completo de oficina mecânica, desenvolvida com Spring Boot 3, JWT Authentication, documentação Swagger/OpenAPI e containerização Docker.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [🚀 CI/CD Pipeline](#-cicd-pipeline-new)
- [Arquitetura de Infraestrutura](#-arquitetura-de-infraestrutura)
  - [Visão Geral](#visão-geral)
  - [Kubernetes](#kubernetes-orquestração)
  - [Terraform](#terraform-infraestrutura-como-código)
- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Execução](#instalação-e-execução)
  - [Docker Compose](#docker-compose)
  - [Kubernetes](#kubernetes)
  - [CI/CD Automático](#cicd-automático)
- [Documentação da API](#documentação-da-api)
- [Autenticação](#autenticação)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)

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

## 🚀 CI/CD Pipeline **NEW!**

Pipeline completa de CI/CD com GitHub Actions + AWS EKS implementada e funcional!

### ⚡ Quick Start (10 minutos)

```bash
# 1. Configurar AWS e GitHub
./scripts/setup-cicd.sh

# 2. Adicionar secrets no GitHub
# (veja guia completo)

# 3. Deploy automático
git push origin main
# Pipeline roda automaticamente!

# 4. Verificar deployment
./scripts/check-deployment.sh
```

### 📚 Documentação Completa

| Documento | Descrição | Tempo |
|-----------|-----------|-------|
| **[CI_CD_QUICKSTART.md](CI_CD_QUICKSTART.md)** | 🚀 Guia Rápido | 10 min |
| **[CI_CD_SETUP_GUIDE.md](CI_CD_SETUP_GUIDE.md)** | 📖 Guia Completo | 30 min |
| **[CI_CD_RESUMO_EXECUTIVO.md](CI_CD_RESUMO_EXECUTIVO.md)** | 📊 Resumo para Avaliação | 5 min |
| **[CI_CD_INDEX.md](CI_CD_INDEX.md)** | 📚 Índice Navegável | - |

### ✅ Pipeline Implementada

```
Push → Build → Tests → Docker → Terraform → K8s Deploy → ✅
       (5min)  (2min)  (3min)  (15min*)    (3min)

*Terraform só roda na primeira vez ou em mudanças
Deploys normais: ~12-14 minutos
```

### 🔄 Workflows Disponíveis

- ✅ **ci-cd.yml**: Pipeline principal completa
- ✅ **pr-validation.yml**: Validação automática de PRs
- ✅ **rollback.yml**: Rollback manual/automático

### 🎯 Funcionalidades

- ✅ Build e testes automatizados
- ✅ Deploy automático no AWS EKS
- ✅ Provisionamento de infra (Terraform)
- ✅ Zero-downtime deployments
- ✅ Rollback automatizado
- ✅ Security scanning
- ✅ Secrets management

**Para mais detalhes:** [CI_CD_README.md](CI_CD_README.md)

---

## 🏗️ Arquitetura de Infraestrutura

### Visão Geral

O Tech Challenge implementa uma arquitetura moderna e completa de **Cloud-Native** com automação end-to-end:

```
┌─────────────────────────────────────────────────────────────────┐
│                         DEVELOPER                                │
└──────────────────────────┬──────────────────────────────────────┘
                           │ git push
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   GITHUB ACTIONS (CI/CD)                         │
├─────────────────────────────────────────────────────────────────┤
│  1. Build & Test    │  Maven, JUnit, JaCoCo                     │
│  2. Security Scan   │  OWASP, Trivy, SonarQube                  │
│  3. Docker Build    │  Multi-platform, Tags, Push               │
│  4. Deploy          │  Kubectl, Helm, Rolling Update            │
└──────────────────────────┬──────────────────────────────────────┘
                           │ deploy
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     KUBERNETES CLUSTER                           │
│                    (AWS EKS / Azure AKS / GCP GKE)               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   Pod 1     │  │   Pod 2     │  │   Pod N     │             │
│  │ Spring Boot │  │ Spring Boot │  │ Spring Boot │             │
│  │  (App)      │  │  (App)      │  │  (App)      │             │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘             │
│         │                 │                 │                     │
│         └─────────────────┼─────────────────┘                     │
│                           │                                       │
│         ┌─────────────────▼─────────────────┐                    │
│         │   PostgreSQL Service (ClusterIP)  │                    │
│         └─────────────────┬─────────────────┘                    │
│                           │                                       │
│  ┌───────────────────────────────────────────────────┐          │
│  │  HPA (Horizontal Pod Autoscaler)                  │          │
│  │  • Min: 1 replica                                 │          │
│  │  • Max: 5 replicas                                │          │
│  │  • CPU Target: 70%                                │          │
│  └───────────────────────────────────────────────────┘          │
│                                                                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ connection
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MANAGED DATABASE                              │
│              (RDS PostgreSQL / Azure DB / Cloud SQL)             │
│                                                                   │
│  • PostgreSQL 15.4                                               │
│  • Automated Backups (7 days)                                    │
│  • Encrypted at rest                                             │
│  • Multi-AZ (Production)                                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      TERRAFORM (IaC)                             │
│  Provisiona toda a infraestrutura acima automaticamente          │
└─────────────────────────────────────────────────────────────────┘
```

---

### Kubernetes (Orquestração)

**Localização:** [`/k8s`](k8s/) | **Documentação:** [KUBERNETES.md](KUBERNETES.md)

#### 🎯 O que faz:
Orquestra os containers da aplicação com **alta disponibilidade**, **auto-scaling** e **zero downtime**.

#### 📦 Recursos Implementados:

| Recurso | Arquivo | Descrição |
|---------|---------|-----------|
| **Deployment** | `app-deployment.yaml` | 2 réplicas iniciais, Rolling Update |
| **Service** | `app-service.yaml` | LoadBalancer, Session Affinity |
| **HPA** | `hpa.yaml` | Auto-scaling 1-5 pods (CPU 70%) |
| **ConfigMap** | `configmap.yaml` | Variáveis de ambiente |
| **Secret** | `secret.yaml` | Credenciais (base64) |
| **PostgreSQL** | `postgres-*.yaml` | Database com PVC (5Gi) |
| **Ingress** | `ingress.yaml` | Roteamento HTTP (opcional) |

#### ⚙️ Características:

- ✅ **Alta Disponibilidade:** Múltiplas réplicas em diferentes nodes
- ✅ **Auto-Scaling:** HPA escala de 1 a 5 pods baseado em CPU/Memória
- ✅ **Zero Downtime:** RollingUpdate com `maxUnavailable: 0`
- ✅ **Health Checks:** Liveness, Readiness e Startup probes
- ✅ **Persistent Storage:** PVC para dados do PostgreSQL
- ✅ **Resource Limits:** CPU e Memória configurados

#### 🚀 Deploy Rápido:

```bash
# Aplicar todos os manifests
kubectl apply -f k8s/

# Verificar status
kubectl get pods -n tech-challenge
kubectl get hpa -n tech-challenge

# Acessar aplicação
kubectl port-forward svc/tech-challenge-service 8080:80 -n tech-challenge
```

📖 **Documentação Completa:** [k8s/README.md](k8s/README.md)

---

### Terraform (Infraestrutura como Código)

**Localização:** [`/infra`](infra/) | **Documentação:** [TERRAFORM.md](infra/TERRAFORM.md)

#### 🎯 O que faz:
Provisiona automaticamente **toda a infraestrutura cloud** necessária (VPC, Kubernetes, Database, etc).

#### ☁️ Provedores Suportados:

| Provider | Kubernetes | Database | Status | Custo/mês |
|----------|------------|----------|--------|-----------|
| **AWS** | EKS | RDS PostgreSQL | ✅ Implementado | ~$203 |
| **Azure** | AKS | Azure PostgreSQL | 📋 Planejado | ~$115 |
| **GCP** | GKE | Cloud SQL | 📋 Planejado | ~$168 |

#### 📦 Recursos AWS Provisionados:

```hcl
# VPC e Networking
- VPC (10.0.0.0/16)
- 6 Subnets (2 AZs)
  ├─ 2 Public (NAT, LoadBalancer)
  ├─ 2 Private (EKS Nodes)
  └─ 2 Database (RDS)
- 2 NAT Gateways (HA)
- Internet Gateway
- Security Groups

# Kubernetes (EKS)
- Cluster v1.28
- Node Group (2-4 nodes)
- Nodes: t3.medium (2 vCPU, 4GB RAM)
- Auto-scaling habilitado

# Database (RDS)
- PostgreSQL 15.4
- Instance: db.t3.micro
- Storage: 20GB (auto-scale até 100GB)
- Backups automáticos (7 dias)
- Encrypted at rest
```

#### 🚀 Uso Rápido:

```bash
# 1. Configurar credenciais
cd infra/aws
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars

# 2. Provisionar infraestrutura (15-20 min)
export TF_VAR_db_password="SuaSenhaSegura123!"
terraform init
terraform plan
terraform apply

# 3. Configurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 4. Deploy da aplicação
kubectl apply -f ../../k8s/
```

📖 **Documentação Completa:** [infra/aws/README.md](infra/aws/README.md)

---

### CI/CD Pipeline

**Localização:** [`/.github/workflows`](.github/workflows/) | **Documentação:** [workflows/README.md](.github/workflows/README.md)

#### 🎯 O que faz:
Automatiza **todo o ciclo** de desenvolvimento: build, test, security, docker, deploy.

#### 🔄 Fluxo Automático:

```
Developer Push → GitHub Actions → Production
     (1s)            (15 min)         (5 min)

┌──────────────────────────────────────────────┐
│  1. BUILD & TEST (5 min)                     │
│  ✓ Compile Java 21                           │
│  ✓ Run unit tests                            │
│  ✓ Generate coverage report (JaCoCo 80%+)    │
│  ✓ Upload artifacts                          │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  2. SECURITY SCAN (2 min)                    │
│  ✓ OWASP Dependency Check                    │
│  ✓ SonarQube analysis                        │
│  ✓ Code quality gates                        │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  3. DOCKER BUILD (3 min)                     │
│  ✓ Build multi-platform (amd64, arm64)       │
│  ✓ Scan vulnerabilities (Trivy)              │
│  ✓ Push to Docker Hub                        │
│  ✓ Tag: latest, sha, branch, semver          │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  4. DEPLOY STAGING (5 min)                   │
│  ✓ Update K8s manifests                      │
│  ✓ Deploy to staging cluster                 │
│  ✓ Wait for rollout                          │
│  ✓ Run smoke tests                           │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  5. DEPLOY PRODUCTION (5 min)                │
│  ✓ Deploy to production cluster              │
│  ✓ Zero downtime (RollingUpdate)             │
│  ✓ Health checks                             │
│  ✓ Slack notification                        │
└──────────────────────────────────────────────┘
```

#### 📋 Workflows Implementados:

| Workflow | Trigger | Descrição | Tempo |
|----------|---------|-----------|-------|
| **ci-cd-pipeline.yml** | Push main/develop | Pipeline completo | ~15-20 min |
| **pull-request.yml** | PR opened | Validação de código | ~5-8 min |
| **terraform.yml** | Push infra/* | Infraestrutura | ~2-20 min |
| **release.yml** | Tag v*.*.* | Release automático | ~10 min |

#### 🔐 Secrets Necessários:

```bash
# Docker Hub
DOCKER_USERNAME=seu-usuario
DOCKER_PASSWORD=dckr_pat_xxxxx

# Kubernetes
KUBE_CONFIG_STAGING=<base64>
KUBE_CONFIG_PRODUCTION=<base64>

# AWS (Terraform)
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=xxx...
DB_PASSWORD=xxx...

# Notificações (Opcional)
SLACK_WEBHOOK=https://hooks.slack.com/...
SONAR_TOKEN=sqp_...
```

#### 🚀 Primeiro Deploy:

```bash
# 1. Configurar secrets no GitHub
# Settings → Secrets and variables → Actions

# 2. Criar ambientes
# Settings → Environments
# Crie: staging, production

# 3. Push para main
git add .
git commit -m "feat: initial setup"
git push origin main

# 4. Acompanhar em Actions
# GitHub → Actions → CI/CD Pipeline
```

📖 **Documentação Completa:** [.github/SETUP_SECRETS.md](.github/SETUP_SECRETS.md)

---

### 🔗 Integração Completa

**Como tudo se conecta:**

1. **Developer** faz push no repositório
2. **GitHub Actions** inicia automaticamente:
   - Compila e testa o código
   - Cria imagem Docker
   - Faz deploy no Kubernetes
3. **Kubernetes** recebe a nova imagem:
   - Atualiza pods gradualmente (RollingUpdate)
   - HPA monitora e escala se necessário
   - Health checks garantem disponibilidade
4. **Aplicação** conecta ao banco de dados:
   - RDS PostgreSQL (provisionado pelo Terraform)
   - Credenciais via Kubernetes Secrets
   - Connection pooling otimizado

**Resultado:** Deploy automatizado em **~15-20 minutos** com **zero downtime**!

---

### 📊 Ambientes

| Ambiente | Branch | Cluster | Deploy | Aprovação |
|----------|--------|---------|--------|-----------|
| **Development** | feature/* | Local (Docker Compose) | Manual | - |
| **Staging** | develop | EKS/AKS/GKE Staging | Automático | - |
| **Production** | main | EKS/AKS/GKE Production | Automático | 2 reviewers |

---

### 💡 Benefícios da Arquitetura

✅ **Automação:** 95%+ do processo automatizado  
✅ **Escalabilidade:** Auto-scaling de 1 a 5 pods  
✅ **Alta Disponibilidade:** Multi-AZ, múltiplas réplicas  
✅ **Zero Downtime:** Rolling updates  
✅ **Segurança:** Scans automáticos, secrets management  
✅ **Observabilidade:** Logs, métricas, health checks  
✅ **Reprodutibilidade:** Infraestrutura como código  
✅ **Velocidade:** Deploy em 15-20 minutos  

---

### 📚 Documentação Detalhada

- **Kubernetes:** [KUBERNETES.md](KUBERNETES.md) | [k8s/README.md](k8s/README.md)
- **Terraform:** [infra/TERRAFORM.md](infra/TERRAFORM.md) | [infra/aws/README.md](infra/aws/README.md)
- **CI/CD:** [.github/workflows/README.md](.github/workflows/README.md) | [.github/SETUP_SECRETS.md](.github/SETUP_SECRETS.md)

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
- **Kubernetes** - Orquestração em produção (EKS/AKS/GKE)
- **Terraform** - Infraestrutura como código (IaC)
- **GitHub Actions** - CI/CD Pipeline
- **Maven** - Gerenciamento de dependências e build

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

- **Notificações Automáticas** (**FASE 2** ✨):
  - 📧 **Email automático** ao cliente quando status muda
  - Verifica se contato é email (não envia para telefones)
  - Template HTML profissional e responsivo
  - Processamento assíncrono (não bloqueia aplicação)
  - Modo dev/prod configurável

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
| POST | `/api/ordens-servico/{id}/aprovar-orcamento` | Aprovar orçamento 🆕 | ✅ |
| GET | `/api/ordens-servico/em-andamento` | Listar ordenado 🆕 | ✅ |
| GET | `/api/ordens-servico/monitoramento/tempo-medio` | Estatísticas | ✅ |

**Legenda:** ✅ Requer JWT | ❌ Público | 🆕 Fase 2

---

## 🔧 Variáveis de Ambiente

### Docker Compose

Já configurado no `docker-compose.yml`. Para personalizar, edite o arquivo:

```yaml
environment:
  # Database
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/tech_challenge
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: 123456
  
  # JWT
  JWT_SECRET: sua_chave_secreta_aqui
  JWT_EXPIRATION: 86400000
  
  # Email (Fase 2)
  MAIL_HOST: smtp.gmail.com
  MAIL_PORT: 587
  MAIL_USERNAME: seu-email@gmail.com
  MAIL_PASSWORD: sua-senha-app
  MAIL_FROM: noreply@techchallenge.com
  EMAIL_ENABLED: false  # true para produção
```

### Execução Local

Configure no `application.yml` ou via variáveis de ambiente:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tech_challenge
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=123456

# JWT
export JWT_SECRET=sua_chave_secreta
export JWT_EXPIRATION=86400000

# Email (Fase 2)
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=seu-email@gmail.com
export MAIL_PASSWORD=sua-senha-app
export EMAIL_ENABLED=false  # true para produção
```

**📧 Configuração de Email:**
- Em **desenvolvimento**: `EMAIL_ENABLED=false` (apenas loga, não envia)
- Em **produção**: `EMAIL_ENABLED=true` + configure credenciais SMTP
- Veja [EMAIL_NOTIFICATION.md](EMAIL_NOTIFICATION.md) para detalhes

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

### Fase 2 - Novidades
- **[EMAIL_NOTIFICATION.md](EMAIL_NOTIFICATION.md)** - 📧 Sistema de notificações por email
- **[FASE2_CHECKLIST_FINAL.md](FASE2_CHECKLIST_FINAL.md)** - ✅ Checklist completo da Fase 2
- **[FASE2_IMPLEMENTACAO_COMPLETA.md](FASE2_IMPLEMENTACAO_COMPLETA.md)** - Resumo da implementação

### Gestão de OS
- **[GESTAO_OS_GUIDE.md](GESTAO_OS_GUIDE.md)** - Guia completo de Gestão de OS
- **[TESTE_RAPIDO_GESTAO_OS.md](TESTE_RAPIDO_GESTAO_OS.md)** - Testes práticos
- **[CHANGELOG_GESTAO_OS.md](CHANGELOG_GESTAO_OS.md)** - Histórico de mudanças

### APIs
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Referência da API
- **[API_EXAMPLES.http](API_EXAMPLES.http)** - Exemplos HTTP

### Arquitetura
- **[ARQUITETURA.md](ARQUITETURA.md)** - Arquitetura completa
- **[ARQUITETURA_VALUE_OBJECTS.md](ARQUITETURA_VALUE_OBJECTS.md)** - Value Objects
- **[RESUMO_IMPLEMENTACAO.md](RESUMO_IMPLEMENTACAO.md)** - Resumo técnico