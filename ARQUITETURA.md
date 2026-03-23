# 🏗️ Arquitetura Completa - Tech Challenge

## 📋 Visão Geral

Este documento apresenta a arquitetura completa do Tech Challenge, incluindo aplicação, infraestrutura e pipeline de deployment.

---

## 🎯 Arquitetura em Camadas

### 1. Camada de Aplicação (Spring Boot)

```
┌─────────────────────────────────────────────────────────┐
│                    CONTROLLERS                          │
│  REST APIs (ClienteController, OrdemDeServicoController)│
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                    USE CASES                            │
│  (AprovarOrcamentoUseCase, ListarOrdensServicoUseCase)  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                    SERVICES                             │
│  (ClienteService, VeiculoService, OrdemDeServicoService)│
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                  DOMAIN ENTITIES                        │
│  (Cliente, Veiculo, OrdemDeServico) + VALUE OBJECTS     │
│  (CpfCnpj, Placa, ValorMonetario, Contato, AnoVeiculo)  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                  REPOSITORIES                           │
│  Spring Data JPA (ClienteRepository, VeiculoRepository) │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                    DATABASE                             │
│              PostgreSQL 15.4                            │
└─────────────────────────────────────────────────────────┘
```

**Princípios Aplicados:**
- ✅ Clean Architecture
- ✅ Domain-Driven Design (DDD)
- ✅ SOLID Principles
- ✅ Value Objects para evitar Primitive Obsession
- ✅ Separation of Concerns

---

## ☁️ Arquitetura de Infraestrutura (Cloud)

### Provisionamento (Terraform)

```
┌─────────────────────────────────────────────────────────┐
│                   TERRAFORM (IaC)                        │
│                                                          │
│  Provider: AWS / Azure / GCP                             │
│                                                          │
│  Provisiona:                                             │
│  • VPC / Virtual Network                                 │
│  • Subnets (Public, Private, Database)                   │
│  • NAT Gateway / Internet Gateway                        │
│  • Security Groups / NSGs                                │
│  • Kubernetes Cluster (EKS/AKS/GKE)                      │
│  • Managed Database (RDS/Azure DB/Cloud SQL)             │
│  • Load Balancers                                        │
│  • IAM Roles / Service Accounts                          │
└─────────────────────────────────────────────────────────┘
                         │
                         │ terraform apply
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  CLOUD PROVIDER                          │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │              KUBERNETES CLUSTER                 │    │
│  │                                                 │    │
│  │  ┌──────────────────────────────────────────┐  │    │
│  │  │          Namespace: tech-challenge       │  │    │
│  │  │                                          │  │    │
│  │  │  ┌────────┐ ┌────────┐ ┌────────┐      │  │    │
│  │  │  │ Pod 1  │ │ Pod 2  │ │ Pod N  │      │  │    │
│  │  │  │ App    │ │ App    │ │ App    │      │  │    │
│  │  │  └───┬────┘ └───┬────┘ └───┬────┘      │  │    │
│  │  │      └──────────┼──────────┘            │  │    │
│  │  │                 │                        │  │    │
│  │  │         ┌───────▼────────┐              │  │    │
│  │  │         │    Service     │              │  │    │
│  │  │         │  (LoadBalancer)│              │  │    │
│  │  │         └───────┬────────┘              │  │    │
│  │  │                 │                        │  │    │
│  │  │         ┌───────▼────────┐              │  │    │
│  │  │         │      HPA       │              │  │    │
│  │  │         │   (1-5 pods)   │              │  │    │
│  │  │         └────────────────┘              │  │    │
│  │  └──────────────────────────────────────────┘  │    │
│  │                                                 │    │
│  │  ┌──────────────────────────────────────────┐  │    │
│  │  │        PostgreSQL Pod (Stateful)         │  │    │
│  │  │        with PersistentVolume             │  │    │
│  │  └──────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────┘    │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │           MANAGED DATABASE (RDS)                │   │
│  │                                                  │   │
│  │  • PostgreSQL 15.4                              │   │
│  │  • Multi-AZ (Production)                        │   │
│  │  • Automated Backups                            │   │
│  │  • Encrypted                                    │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Pipeline CI/CD (GitHub Actions)

### Fluxo Completo de Deploy

```
┌─────────────────────────────────────────────────────────┐
│  DEVELOPER                                               │
│  • Desenvolve feature                                    │
│  • git commit && git push                                │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  GITHUB REPOSITORY                                       │
│  • Code Review (Pull Request)                            │
│  • Branch Protection Rules                               │
│  • Triggers GitHub Actions                               │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  CI/CD PIPELINE (GitHub Actions)                         │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │ JOB 1: BUILD & TEST (5 min)                    │    │
│  │ • Checkout code                                 │    │
│  │ • Setup Java 21                                 │    │
│  │ • Maven compile                                 │    │
│  │ • Run tests (JUnit)                             │    │
│  │ • Generate coverage (JaCoCo 80%+)               │    │
│  │ • Upload artifacts                              │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │ JOB 2: SECURITY SCAN (2 min)                   │    │
│  │ • OWASP Dependency Check                        │    │
│  │ • SonarQube analysis                            │    │
│  │ • Vulnerability report                          │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │ JOB 3: DOCKER BUILD & PUSH (3 min)             │    │
│  │ • Build multi-platform image                    │    │
│  │ • Scan with Trivy                               │    │
│  │ • Tag (latest, sha, branch, semver)             │    │
│  │ • Push to Docker Hub                            │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │ JOB 4: DEPLOY TO STAGING (5 min)               │    │
│  │ • Update K8s manifests                          │    │
│  │ • kubectl apply                                 │    │
│  │ • Wait for rollout                              │    │
│  │ • Run smoke tests                               │    │
│  └────────────────────────────────────────────────┘    │
│                         ↓                                │
│  ┌────────────────────────────────────────────────┐    │
│  │ JOB 5: DEPLOY TO PRODUCTION (5 min)            │    │
│  │ • Requires approval (2 reviewers)               │    │
│  │ • Zero downtime (RollingUpdate)                 │    │
│  │ • Health checks validation                      │    │
│  │ • Slack notification                            │    │
│  └────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│  PRODUCTION ENVIRONMENT                                  │
│  • Aplicação atualizada                                  │
│  • Usuários não percebem downtime                        │
│  • Monitoramento ativo                                   │
└─────────────────────────────────────────────────────────┘
```

**Tempo Total:** ~15-20 minutos do push ao deploy

---

## 📊 Fluxo de Dados

### Request-Response Flow

```
┌─────────┐
│ USUÁRIO │
└────┬────┘
     │ HTTP Request
     ▼
┌────────────────┐
│  INTERNET      │
└────┬───────────┘
     │
     ▼
┌────────────────────────────────────┐
│  LOAD BALANCER                     │
│  (AWS ALB / Azure LB / GCP LB)     │
└────┬───────────────────────────────┘
     │
     ▼
┌────────────────────────────────────┐
│  KUBERNETES SERVICE                │
│  • Session Affinity                │
│  • Health Check                    │
└────┬───────────────────────────────┘
     │ Round-robin
     ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│  POD 1   │  │  POD 2   │  │  POD N   │
│          │  │          │  │          │
│ Spring   │  │ Spring   │  │ Spring   │
│ Boot App │  │ Boot App │  │ Boot App │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     └─────────────┼─────────────┘
                   │ JDBC Connection
                   ▼
         ┌─────────────────────┐
         │  PostgreSQL Service │
         │    (ClusterIP)      │
         └─────────┬───────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │  RDS PostgreSQL     │
         │  • Connection Pool  │
         │  • Read Replicas    │
         └─────────────────────┘
```

---

## 🔐 Segurança

### Camadas de Segurança Implementadas

```
┌─────────────────────────────────────────────────────────┐
│  NETWORK SECURITY                                        │
│  • VPC Isolation                                         │
│  • Private Subnets                                       │
│  • Security Groups / NSGs                                │
│  • Network Policies                                      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  APPLICATION SECURITY                                    │
│  • JWT Authentication                                    │
│  • Spring Security                                       │
│  • Input Validation                                      │
│  • SQL Injection Prevention (JPA)                        │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  CONTAINER SECURITY                                      │
│  • Non-root user                                         │
│  • Image scanning (Trivy)                                │
│  • No secrets in images                                  │
│  • Minimal base image (Alpine)                           │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  DATA SECURITY                                           │
│  • Encryption at rest (RDS)                              │
│  • Encryption in transit (TLS)                           │
│  • Kubernetes Secrets (base64)                           │
│  • Database credentials rotation                         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│  CI/CD SECURITY                                          │
│  • OWASP Dependency Check                                │
│  • SonarQube Code Analysis                               │
│  • Secrets in GitHub Secrets                             │
│  • Environment Protection Rules                          │
└─────────────────────────────────────────────────────────┘
```

---

## 📈 Observabilidade

### Monitoramento e Logs

```
┌─────────────────────────────────────────────────────────┐
│  METRICS (Prometheus + Grafana)                          │
│  • Pod CPU/Memory usage                                  │
│  • Request rate & latency                                │
│  • Error rate                                            │
│  • HPA scaling events                                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  LOGS (EFK/ELK Stack)                                    │
│  • Application logs (Spring Boot)                        │
│  • Access logs (Nginx Ingress)                           │
│  • Database logs (RDS)                                   │
│  • Audit logs (Kubernetes)                               │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  HEALTH CHECKS                                           │
│  • Liveness Probe: /actuator/health/liveness            │
│  • Readiness Probe: /actuator/health/readiness          │
│  • Startup Probe: /actuator/health                      │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  ALERTING (Slack/Email)                                  │
│  • Deployment failures                                   │
│  • High error rate                                       │
│  • Resource exhaustion                                   │
│  • Database connection issues                            │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Cenários de Uso

### 1. Deploy de Nova Feature

```bash
# Developer
git checkout -b feature/nova-funcionalidade
# ... desenvolve ...
git commit -m "feat: adiciona nova funcionalidade"
git push origin feature/nova-funcionalidade

# Pull Request
# → CI valida código, testes, segurança
# → Code review
# → Merge para develop

# Deploy Automático Staging
# → CI/CD pipeline executa
# → Deploy em staging (~5 min)
# → QA testa em staging

# Merge para Main
# → CI/CD pipeline executa
# → Deploy em production (~5 min)
# → Zero downtime!
```

### 2. Escalabilidade Automática

```bash
# Tráfego aumenta
# → CPU dos pods > 70%
# → HPA detecta
# → Cria novos pods (até 5)
# → Load Balancer distribui tráfego
# → Tráfego diminui
# → HPA aguarda 5 min
# → Remove pods gradualmente
```

### 3. Recuperação de Falhas

```bash
# Pod falha (crash)
# → Liveness Probe falha 3 vezes
# → Kubernetes reinicia pod
# → Readiness Probe valida
# → Pod volta a receber tráfego

# Node falha
# → Pods são movidos para outros nodes
# → Service continua funcionando
# → Zero downtime!
```

---

## 💡 Decisões Arquiteturais

### Por que Kubernetes?
- ✅ Orquestração automatizada
- ✅ Auto-scaling horizontal
- ✅ Self-healing
- ✅ Zero downtime deployments
- ✅ Padrão da indústria

### Por que Terraform?
- ✅ Infraestrutura como código
- ✅ Reprodutível e versionável
- ✅ Multi-cloud (AWS, Azure, GCP)
- ✅ State management
- ✅ Comunidade ativa

### Por que GitHub Actions?
- ✅ Integração nativa com GitHub
- ✅ Workflows como código (YAML)
- ✅ Secrets management integrado
- ✅ Marketplace de actions
- ✅ Gratuito para repositórios públicos

### Por que Value Objects (DDD)?
- ✅ Evita Primitive Obsession
- ✅ Validação centralizada
- ✅ Type safety
- ✅ Expressividade do domínio
- ✅ Imutabilidade

---

## 📚 Referências

- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [The Twelve-Factor App](https://12factor.net/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)

---

**Documentação criada com foco em:**
- ✅ Clareza e compreensão
- ✅ Diagramas visuais
- ✅ Exemplos práticos
- ✅ Justificativas técnicas
- ✅ Boas práticas da indústria


