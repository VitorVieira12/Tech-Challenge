# Architecture Decision Records (ADRs)

> Tech Challenge Fase 3 — FIAP 13SOAT
> Sistema de Gestão de Oficina Mecânica

---

## ADR-001: Autenticação via AWS Lambda + CPF/CNPJ

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
O sistema precisa autenticar clientes usando CPF ou CNPJ, sem necessidade de senha, conforme requisito da Fase 3. A autenticação deve ser independente da aplicação principal e escalável.

### Decisão
Implementar a autenticação como uma **AWS Lambda Function** separada, exposta via **AWS API Gateway (HTTP API v2)**, que valida o CPF/CNPJ consultando o RDS e retorna um JWT.

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **Lambda (escolhida)** | Escalável, pay-per-use, isolada | Cold start, mais complexo |
| Endpoint no Spring Boot | Simples, código centralizado | Não cumpre requisito de Lambda |
| API Gateway + Cognito | Gerenciado, seguro | Custo adicional, over-engineering |

### Consequências
- ✅ Cumpre requisito de Lambda Serverless da Fase 3
- ✅ Autenticação escalável e sem servidor a gerenciar
- ⚠️ Cold start de ~2-5s na primeira requisição
- ⚠️ Conexão direta ao RDS (sem pool de conexões HikariCP)

---

## ADR-002: Container Orchestration com Amazon EKS

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
A aplicação Spring Boot precisa ser implantada em um ambiente de containers gerenciado, com suporte a auto-scaling, rolling updates e health checks.

### Decisão
Usar **Amazon EKS (Elastic Kubernetes Service)** com:
- Cluster Kubernetes 1.31
- Node Group com instâncias **t3.small** (Free Tier eligible)
- 1 réplica inicial com HPA para escalar até 3

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **EKS (escolhida)** | Kubernetes gerenciado, padrão de mercado | Custo do control plane ($0.10/h) |
| ECS (Fargate) | Serverless containers, sem gerenciar nodes | Não cumpre requisito de Kubernetes |
| EC2 puro | Mais barato | Sem orquestração, manual |

### Consequências
- ✅ Kubernetes gerenciado com auto-healing e rolling updates
- ✅ HPA escala automaticamente com base em CPU
- ⚠️ Custo do EKS control plane (~$72/mês) — maior custo da solução
- ✅ Startup probe configurado com 15 minutos para acomodar inicialização do Spring Boot + New Relic

---

## ADR-003: Banco de Dados Amazon RDS PostgreSQL

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
A aplicação precisa de um banco relacional para armazenar clientes, veículos e ordens de serviço.

### Decisão
Usar **Amazon RDS PostgreSQL 15** com:
- Instância `db.t3.micro` (Free Tier eligible)
- `publicly_accessible = true` (necessário para acesso do Lambda e EKS em subnets diferentes)
- `backup_retention_period = 1` (Free Tier)
- `multi_az = false` (redução de custo)

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **RDS PostgreSQL (escolhida)** | Gerenciado, backups automáticos | Custo maior que EC2+PostgreSQL |
| Aurora Serverless | Auto-pause, pay-per-use | Não é Free Tier, custo variável |
| DynamoDB | Escala infinita, serverless | Não relacional, requer refactoring total |
| PostgreSQL em EC2 | Mais barato | Sem gerenciamento automático |

### Consequências
- ✅ Banco gerenciado com patches e backups automáticos
- ✅ Compatível com Hibernate/JPA sem mudanças
- ⚠️ `publicly_accessible = true` requer Security Group bem configurado
- ✅ `spring.jpa.hibernate.ddl-auto=update` para criação automática de tabelas no primeiro deploy

---

## ADR-004: Monitoramento com New Relic

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
A Fase 3 exige monitoramento de: latência, CPU, memória, health checks, uptime e alertas. A solução deve integrar-se facilmente ao ambiente Kubernetes/JVM.

### Decisão
Usar **New Relic** com:
- **Java Agent** (`newrelic.jar`) no container via `-javaagent`
- **License Key** armazenada como Kubernetes Secret
- **App Name**: `Tech Challenge - Oficina`

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **New Relic (escolhida)** | Requisito da fase, agente JVM, APM completo | Licença paga (trial disponível) |
| AWS CloudWatch | Nativo AWS, gratuito | Interface menos amigável, sem APM |
| Prometheus + Grafana | Open source, flexível | Requer setup de infraestrutura adicional |
| Datadog | APM completo | Mais caro que New Relic |

### Consequências
- ✅ Cumpre requisito de New Relic da Fase 3
- ✅ APM completo: JVM metrics, HTTP traces, DB queries
- ✅ Alertas e dashboards configuráveis
- ⚠️ Aumenta startup time do Spring Boot (~30-60s para inicializar o agente)
- ⚠️ Consumo adicional de memória (~64MB heap)

---

## ADR-005: Infraestrutura como Código com Terraform

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
Toda a infraestrutura AWS precisa ser provisionável de forma reproduzível e versionada.

### Decisão
Usar **Terraform 1.6.0** com backend de estado no **S3** (`tech-challenge-tfstate-vitorvieira12`), organizado em dois repositórios:
- `tech-challenge-infra-db` — provisionamento do RDS
- `tech-challenge-infra-k8s` — provisionamento do EKS + API Gateway

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **Terraform (escolhida)** | Multi-cloud, padrão de mercado, HCL simples | State management, curva de aprendizado |
| AWS CDK | TypeScript/Python, testes unitários | Apenas AWS, verbose |
| AWS CloudFormation | Nativo AWS, sem custo | YAML verbose, menos features |
| Pulumi | Linguagens convencionais | Menos adoção, curva de aprendizado |

### Consequências
- ✅ Estado versionado e compartilhado via S3
- ✅ `terraform destroy` para limpeza completa dos recursos
- ✅ Integrado ao GitHub Actions com `workflow_dispatch` para destruição manual
- ⚠️ Necessidade de gerenciar `terraform.tfstate` cuidadosamente

---

## ADR-006: CI/CD com GitHub Actions

**Data:** 2026-03-01  
**Status:** ✅ Aceito

### Contexto
Cada repositório precisa de um pipeline de CI/CD que execute testes, build e deploy automaticamente.

### Decisão
Usar **GitHub Actions** com:
- Triggers em push para branch principal
- Secrets armazenados no GitHub Secrets
- Jobs separados: Test → Build → Deploy

### Alternativas Consideradas

| Alternativa | Prós | Contras |
|---|---|---|
| **GitHub Actions (escolhida)** | Integrado ao GitHub, gratuito para public, amplo marketplace | Limitado para repos privados free |
| Jenkins | Open source, altamente configurável | Requer servidor dedicado |
| CircleCI | Rápido, YAML simples | Custo para paralelismo |
| GitLab CI | CI/CD nativo GitLab | Requer migração do GitHub |

### Consequências
- ✅ Integração nativa com GitHub: PR checks, deployment status
- ✅ Marketplace de actions reutilizáveis (aws-actions, docker, etc.)
- ✅ Secrets integrados ao repositório
- ⚠️ Minutos gratuitos limitados para repos privados (resolvido tornando os repos públicos)

---

## ADR-007: Branch Protection com Pull Requests Obrigatórios

**Data:** 2026-03-21  
**Status:** ✅ Aceito

### Contexto
A Fase 3 exige que a branch principal seja protegida e que todos os merges sejam feitos via Pull Request com pelo menos 1 aprovação.

### Decisão
Configurar **Branch Protection Rules** nas branches principais (`main`/`fase-3`) de todos os 4 repositórios via GitHub API, exigindo:
- 1 aprovação de revisão antes do merge
- Dismiss de reviews obsoletas em novos pushes
- Proibição de force push e deleção

> **Nota:** Os repositórios foram tornados públicos para habilitar branch protection no GitHub Free tier.

### Consequências
- ✅ Garante revisão de código antes de merge em produção
- ✅ Histórico auditável de todas as mudanças
- ✅ Proteção contra commits acidentais na branch principal
- ℹ️ Admins podem fazer bypass da regra quando necessário (configurado)
