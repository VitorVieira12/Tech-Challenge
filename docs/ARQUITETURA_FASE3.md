# Arquitetura Técnica — Tech Challenge Fase 3

> FIAP — Pós Tech | Software Architecture | Turma 13SOAT

## 1. Visão Geral do Sistema

O sistema gerencia uma **Oficina Mecânica**, permitindo o cadastro de clientes e veículos, agendamento de serviços e controle de ordens de serviço.

A Fase 3 introduz uma **arquitetura cloud-native** completa na AWS, com:
- **Autenticação serverless** via AWS Lambda + API Gateway
- **Aplicação principal em containers** no Amazon EKS (Kubernetes)
- **Banco de dados gerenciado** no Amazon RDS PostgreSQL
- **Monitoramento** via New Relic
- **CI/CD automatizado** via GitHub Actions + Terraform

---

## 2. Diagrama de Componentes

```mermaid
graph TB
    subgraph Client["Cliente (Browser/App Mobile)"]
        C[HTTP Client]
    end

    subgraph AWS["AWS us-east-1"]
        subgraph Auth["Autenticação (Serverless)"]
            APIGW[API Gateway\nHTTP API v2]
            LAMBDA[Lambda Function\nAuthHandler.java\nJava 21]
        end

        subgraph K8s["EKS Cluster v1.31"]
            LB[Load Balancer\nAWS NLB]
            subgraph NS["Namespace: tech-challenge"]
                DEP[Deployment\ntech-challenge-app\n1-3 réplicas]
                HPA[HPA\nCPU 70%\nmin=1, max=3]
                CM[ConfigMap\napp-config]
                SEC[Secrets\napp-secrets\nnewrelic-secret]
            end
        end

        RDS[(RDS PostgreSQL 15\ntech-challenge-db\ndb.t3.micro)]
        NR[New Relic\nMonitoramento]
    end

    C -->|"POST /auth\n{cpf}"| APIGW
    APIGW --> LAMBDA
    LAMBDA -->|"SELECT cliente\nWHERE cpf=?"| RDS
    LAMBDA -->|"JWT Token"| C

    C -->|"GET /api/*\nAuthorization: Bearer JWT"| LB
    LB --> DEP
    DEP -->|"JDBC"| RDS
    DEP -->|"-javaagent:\nnewrelic.jar"| NR
    HPA -.->|"scale"| DEP
    CM -.->|"env vars"| DEP
    SEC -.->|"credentials"| DEP
```

---

## 3. Diagrama de Sequência — Autenticação

```mermaid
sequenceDiagram
    actor Cliente
    participant APIGW as API Gateway
    participant Lambda as Lambda (Auth)
    participant RDS as RDS PostgreSQL
    participant App as Spring Boot App (EKS)

    Cliente->>APIGW: POST /auth { cpf: "123.456.789-00" }
    APIGW->>Lambda: Invoke (evento HTTP)
    Lambda->>Lambda: Valida formato CPF
    Lambda->>RDS: SELECT id, ativo FROM clientes WHERE cpf = ?
    RDS-->>Lambda: { id: 1, ativo: true }
    Lambda->>Lambda: Gera JWT (HMAC-SHA256, 24h)
    Lambda-->>APIGW: 200 OK { token: "eyJ..." }
    APIGW-->>Cliente: JWT Token

    Cliente->>App: GET /api/clientes\nAuthorization: Bearer eyJ...
    App->>App: Valida JWT (JwtAuthFilter)
    App->>RDS: SELECT * FROM clientes
    RDS-->>App: [lista de clientes]
    App-->>Cliente: 200 OK [{ id, nome, cpf, ... }]
```

---

## 4. Diagrama de Sequência — CI/CD Deploy

```mermaid
sequenceDiagram
    participant Dev as Desenvolvedor
    participant GH as GitHub
    participant CI as GitHub Actions
    participant DH as Docker Hub
    participant AWS as AWS (EKS)

    Dev->>GH: git push fase-3
    GH->>CI: Trigger CI/CD workflow
    CI->>CI: mvn test (JUnit)
    CI->>CI: mvn package (JAR)
    CI->>CI: docker build
    CI->>DH: docker push vitorvieira12/tech-challenge-app:latest
    CI->>AWS: aws eks update-kubeconfig
    CI->>AWS: kubectl set image deployment/tech-challenge-app
    AWS->>AWS: RollingUpdate (terminates old pod, starts new)
    AWS->>AWS: startupProbe (15min timeout)
    AWS->>AWS: readinessProbe (ok → receive traffic)
    CI->>CI: kubectl rollout status (aguarda 16min)
    CI-->>Dev: Deploy concluído ✓
```

---

## 5. Diagrama ER — Banco de Dados

```mermaid
erDiagram
    CLIENTES {
        bigint id PK
        varchar nome
        varchar cpf UK
        varchar cnpj
        varchar email
        varchar telefone
        varchar endereco
        boolean ativo
        timestamp created_at
        timestamp updated_at
    }

    VEICULOS {
        bigint id PK
        varchar placa UK
        varchar marca
        varchar modelo
        int ano
        varchar cor
        bigint cliente_id FK
        timestamp created_at
        timestamp updated_at
    }

    ORDENS_SERVICO {
        bigint id PK
        varchar numero UK
        varchar status
        varchar descricao
        decimal valor_total
        bigint cliente_id FK
        bigint veiculo_id FK
        timestamp data_abertura
        timestamp data_conclusao
    }

    SERVICOS {
        bigint id PK
        varchar nome
        varchar descricao
        decimal valor
        boolean ativo
    }

    ORDEM_SERVICO_ITENS {
        bigint id PK
        bigint ordem_id FK
        bigint servico_id FK
        int quantidade
        decimal valor_unitario
    }

    CLIENTES ||--o{ VEICULOS : "possui"
    CLIENTES ||--o{ ORDENS_SERVICO : "solicita"
    VEICULOS ||--o{ ORDENS_SERVICO : "atendido em"
    ORDENS_SERVICO ||--o{ ORDEM_SERVICO_ITENS : "contém"
    SERVICOS ||--o{ ORDEM_SERVICO_ITENS : "incluído em"
```

---

## 6. Diagrama de Infraestrutura AWS

```mermaid
graph TB
    subgraph Internet["Internet"]
        USER[Usuário]
    end

    subgraph AWS["AWS us-east-1"]
        subgraph VPC_DB["VPC (infra-db)"]
            SG_RDS[Security Group\nPort 5432]
            RDS[(RDS PostgreSQL 15\ndb.t3.micro\nMulti-AZ: false)]
        end

        subgraph VPC_EKS["VPC (infra-k8s)"]
            IGW[Internet Gateway]

            subgraph PUBLIC["Subnets Públicas"]
                ALB[NLB\nLoad Balancer]
            end

            subgraph PRIVATE["Subnets Privadas"]
                subgraph EKS["EKS Cluster v1.31"]
                    NODE[Node Group\nt3.small\nmin=1 max=3]
                    POD[Pod\nSpring Boot\n+ New Relic]
                end
            end

            APIGW[API Gateway\nHTTP API]
            LAMBDA[Lambda\nAuthHandler]
        end

        subgraph S3["S3"]
            STATE[Terraform State\ntech-challenge-tfstate-vitorvieira12]
        end

        NR[New Relic\nSaaS]
    end

    USER -->|HTTPS| APIGW
    USER -->|HTTP| ALB
    APIGW --> LAMBDA
    LAMBDA --> RDS
    ALB --> POD
    POD --> RDS
    POD --> NR
    IGW --> ALB
```

---

## 7. Configuração de Monitoramento (New Relic)

### Métricas Monitoradas
- **JVM**: heap memory, GC, thread count
- **HTTP**: request rate, response time, error rate
- **Database**: query time, connection pool
- **Pod Health**: liveness, readiness, startup probes

### Alertas Configurados
| Condição | Threshold | Severidade |
|---|---|---|
| CPU > 80% | 5 minutos | Warning |
| Response Time > 2s | 3 minutos | Critical |
| Error Rate > 5% | 2 minutos | Critical |
| Pod Restarts > 3 | 10 minutos | Warning |

### Configuração do Agente
```dockerfile
# Dockerfile
ENTRYPOINT ["java",
  "-javaagent:/opt/newrelic/newrelic.jar",
  "-Xms256m", "-Xmx768m",
  "-jar", "app.jar"]
```

```yaml
# Kubernetes Secret (injetado pelo CI/CD)
NEW_RELIC_LICENSE_KEY: <valor do GitHub Secret>
NEW_RELIC_APP_NAME: "Tech Challenge - Oficina"
```

---

## 8. Repositórios e Branches

| Repositório | Branch Principal | Protegida | CI/CD |
|---|---|---|---|
| [Tech-Challenge](https://github.com/VitorVieira12/Tech-Challenge) | `fase-3` | ✅ PR obrigatório | Build → Push Docker → Deploy EKS |
| [tech-challenge-infra-db](https://github.com/VitorVieira12/tech-challenge-infra-db) | `main` | ✅ PR obrigatório | Terraform RDS |
| [tech-challenge-infra-k8s](https://github.com/VitorVieira12/tech-challenge-infra-k8s) | `main` | ✅ PR obrigatório | Terraform EKS + K8s Manifests |
| [tech-challenge-lambda](https://github.com/VitorVieira12/tech-challenge-lambda) | `main` | ✅ PR obrigatório | SAM Build + Deploy |
