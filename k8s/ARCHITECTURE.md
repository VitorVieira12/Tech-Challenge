# 🏗️ Arquitetura Kubernetes - Tech Challenge

## 📐 Diagrama da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                         Internet / Users                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LoadBalancer / Ingress                        │
│                  (tech-challenge-service)                        │
│                    Port 80 → 8080                                │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│              Horizontal Pod Autoscaler (HPA)                     │
│         Min: 1 replica | Max: 5 replicas                         │
│         CPU Target: 70% | Memory Target: 80%                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  App Pod 1   │  │  App Pod 2   │  │  App Pod N   │
│ (Spring Boot)│  │ (Spring Boot)│  │ (Spring Boot)│
│              │  │              │  │              │
│ CPU: 500m-1  │  │ CPU: 500m-1  │  │ CPU: 500m-1  │
│ RAM: 512Mi-1G│  │ RAM: 512Mi-1G│  │ RAM: 512Mi-1G│
│              │  │              │  │              │
│ Probes:      │  │ Probes:      │  │ Probes:      │
│ - Liveness   │  │ - Liveness   │  │ - Liveness   │
│ - Readiness  │  │ - Readiness  │  │ - Readiness  │
│ - Startup    │  │ - Startup    │  │ - Startup    │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       └─────────────────┼─────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   PostgreSQL Service │
              │      (ClusterIP)     │
              │       Port 5432      │
              └──────────┬───────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   PostgreSQL Pod     │
              │   (StatefulSet-like) │
              │                      │
              │ CPU: 250m-500m       │
              │ RAM: 256Mi-512Mi     │
              │                      │
              │ Storage: 5Gi PVC     │
              │ (Persistent Volume)  │
              └──────────────────────┘
```

---

## 🔄 Fluxo de Requisição

1. **Usuário** faz requisição HTTP para o LoadBalancer
2. **LoadBalancer** roteia para um dos pods da aplicação (via Service)
3. **Service** usa **Session Affinity** (ClientIP) para manter o usuário no mesmo pod
4. **HPA** monitora CPU/Memória e escala pods conforme necessário
5. **Aplicação** se conecta ao **PostgreSQL** via Service interno (ClusterIP)
6. **Liveness/Readiness Probes** garantem que apenas pods saudáveis recebem tráfego
7. **RollingUpdate** garante zero downtime durante deploys

---

## 📦 Componentes Kubernetes

### 1. **Namespace** (`namespace.yaml`)

```yaml
tech-challenge
├── Isolation: Separa recursos de outros namespaces
├── Resource Quotas: Pode limitar recursos por namespace
└── RBAC: Controle de acesso por namespace
```

### 2. **ConfigMap** (`configmap.yaml`)

**Armazena configurações não sensíveis:**
- Endereços de serviços (DB_HOST)
- Portas (DB_PORT, SERVER_PORT)
- Profiles do Spring (SPRING_PROFILES_ACTIVE)
- Configurações de logging
- Timezone

**Consumo:** Injetado como variáveis de ambiente (`envFrom`)

### 3. **Secret** (`secret.yaml`)

**Armazena dados sensíveis (base64):**
- Credenciais do banco (DB_USERNAME, DB_PASSWORD)
- JWT Secret
- Credenciais de admin

**Segurança:**
- Valores em base64 (não é criptografia!)
- Para produção, use: Sealed Secrets, HashiCorp Vault ou External Secrets Operator
- Nunca commite secrets reais no Git

### 4. **PersistentVolumeClaim** (`postgres-pvc.yaml`)

**Volume persistente para PostgreSQL:**
- **Access Mode:** ReadWriteOnce
- **Storage:** 5Gi
- **Lifecycle:** Independente do pod (dados sobrevivem a recreações)

**Storage Classes:**
- Minikube: `standard` (hostPath)
- AWS: `gp2`, `gp3` (EBS)
- GCP: `standard`, `ssd` (Persistent Disk)
- Azure: `default`, `managed-premium` (Azure Disk)

### 5. **Deployments**

#### PostgreSQL Deployment (`postgres-deployment.yaml`)

```yaml
Replicas: 1
Strategy: Recreate  # Garante apenas 1 instância ativa
Resources:
  Requests: 250m CPU, 256Mi RAM
  Limits: 500m CPU, 512Mi RAM
Volume: postgres-pvc (5Gi)
Probes:
  - Liveness: pg_isready (30s initial, 10s period)
  - Readiness: pg_isready (10s initial, 5s period)
```

**Nota:** Para alta disponibilidade em produção, use **StatefulSet** com replicação PostgreSQL ou serviços gerenciados (RDS, Cloud SQL, Azure Database).

#### Aplicação Deployment (`app-deployment.yaml`)

```yaml
Replicas: 2 (HPA pode escalar até 5)
Strategy: RollingUpdate
  MaxSurge: 1       # Pode criar 1 pod extra durante update
  MaxUnavailable: 0 # Garante zero downtime
Resources:
  Requests: 500m CPU, 512Mi RAM
  Limits: 1000m CPU (1 core), 1Gi RAM
Init Container:
  - wait-for-postgres (aguarda DB estar pronto)
Probes:
  - Liveness: /actuator/health/liveness (90s initial)
  - Readiness: /actuator/health/readiness (60s initial)
  - Startup: /actuator/health (30s initial, max 120s)
JVM Opts: -Xms512m -Xmx1024m -XX:+UseG1GC
```

**Init Container:** Evita que a aplicação inicie antes do PostgreSQL estar pronto, prevenindo erros de conexão.

### 6. **Services**

#### PostgreSQL Service (`postgres-service.yaml`)

```yaml
Type: ClusterIP  # Apenas acessível dentro do cluster
Port: 5432
Selector: app=postgres
DNS: postgres-service.tech-challenge.svc.cluster.local
```

#### Aplicação Service (`app-service.yaml`)

```yaml
Type: LoadBalancer  # Expõe externamente
Port: 80 → TargetPort: 8080
Selector: app=tech-challenge
Session Affinity: ClientIP (timeout: 10800s = 3h)
```

**Session Affinity:** Garante que requisições do mesmo cliente vão para o mesmo pod (útil para sessões stateful).

### 7. **Horizontal Pod Autoscaler** (`hpa.yaml`)

```yaml
Min Replicas: 1
Max Replicas: 5
Metrics:
  - CPU: 70% utilization
  - Memory: 80% utilization
Behavior:
  Scale Up:
    - Stabilization: 0s (imediato)
    - Max Increase: 2 pods a cada 30s
  Scale Down:
    - Stabilization: 300s (5 min)
    - Max Decrease: 50% dos pods por vez
```

**Como funciona:**
1. Metrics Server coleta métricas de CPU/Memória dos pods
2. HPA calcula se precisa escalar com base nos targets
3. Se CPU > 70% OU Memory > 80%: escala UP
4. Se CPU < 70% E Memory < 80%: aguarda 5 min e escala DOWN

**Fórmula:**
```
Desired Replicas = ceil[current replicas * (current metric / target metric)]
```

### 8. **Ingress** (`ingress.yaml`)

```yaml
IngressClass: nginx
Host: tech-challenge.local
Path: / (Prefix)
Annotations:
  - Rate Limiting: 100 req/s
  - CORS: Enabled
  - Proxy Timeout: 60s
  - Body Size: 10m
TLS: Suportado (descomentar e configurar)
```

**Benefícios do Ingress:**
- Um único LoadBalancer para múltiplos serviços
- Terminação TLS/SSL centralizada
- Roteamento baseado em path/host
- Rate limiting e WAF integrados

---

## 🔍 Health Checks Detalhados

### Liveness Probe
**Propósito:** "O container está vivo?"  
**Ação se falhar:** Kubernetes reinicia o container

```yaml
httpGet:
  path: /actuator/health/liveness
  port: 8080
initialDelaySeconds: 90  # Tempo para aplicação iniciar
periodSeconds: 10        # Verifica a cada 10s
timeoutSeconds: 5        # Timeout da requisição
failureThreshold: 3      # Falha após 3 tentativas consecutivas
```

**Quando usar:** Detectar deadlocks, memory leaks, ou estados irrecuperáveis.

### Readiness Probe
**Propósito:** "O container está pronto para receber tráfego?"  
**Ação se falhar:** Remove do load balancer (não reinicia)

```yaml
httpGet:
  path: /actuator/health/readiness
  port: 8080
initialDelaySeconds: 60
periodSeconds: 5
timeoutSeconds: 3
failureThreshold: 3
```

**Quando usar:** Detectar dependências não prontas (ex: DB connection pool vazio).

### Startup Probe
**Propósito:** "A aplicação já inicializou?"  
**Ação se falhar:** Reinicia o container (desabilita outras probes durante startup)

```yaml
httpGet:
  path: /actuator/health
  port: 8080
initialDelaySeconds: 30
periodSeconds: 10
failureThreshold: 12  # 12 * 10s = 120s máximo
```

**Quando usar:** Aplicações que demoram para iniciar (JVM, Spring Boot).

---

## 📊 Resource Management

### Por que definir Requests e Limits?

#### **Requests** (Garantia Mínima)
- Scheduler garante que o node tem recursos disponíveis
- Base para cálculo do HPA
- Prioridade de QoS

#### **Limits** (Máximo Permitido)
- Previne que um pod consuma todos os recursos do node
- CPU: throttling (desacelera)
- Memory: OOMKilled (reinicia)

### QoS Classes

```
┌─────────────┬──────────────┬──────────────┬───────────────┐
│ QoS Class   │ Requests     │ Limits       │ Eviction      │
├─────────────┼──────────────┼──────────────┼───────────────┤
│ Guaranteed  │ = Limits     │ Defined      │ Last (melhor) │
│ Burstable   │ < Limits     │ Defined      │ Middle        │
│ BestEffort  │ Not Defined  │ Not Defined  │ First (pior)  │
└─────────────┴──────────────┴──────────────┴───────────────┘
```

**Nossa configuração:** **Burstable** (pode usar até o limite se disponível).

---

## 🔐 Segurança

### Implementado

✅ **Namespace Isolation**  
✅ **Secrets para credenciais**  
✅ **Non-root containers** (Spring Boot)  
✅ **Resource Limits** (previne resource exhaustion)  
✅ **Health Checks** (self-healing)  
✅ **Rolling Updates** (zero downtime)  

### Próximos Passos (Produção)

🔲 **Network Policies** - Controlar tráfego entre pods  
🔲 **Pod Security Policies** - Restringir privilégios  
🔲 **RBAC** - Controle de acesso granular  
🔲 **TLS/SSL** - Criptografia em trânsito  
🔲 **Sealed Secrets** - Criptografia de secrets  
🔲 **Image Scanning** - Verificar vulnerabilidades  
🔲 **Admission Controllers** - Validações customizadas  

---

## 🚀 Estratégias de Deploy

### RollingUpdate (Atual)

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1       # Pods extras durante update
    maxUnavailable: 0 # Zero downtime
```

**Fluxo:**
1. Cria 1 pod novo (v2)
2. Aguarda pod ficar Ready
3. Remove 1 pod antigo (v1)
4. Repete até todos serem v2

**Vantagem:** Zero downtime, rollback fácil  
**Desvantagem:** Versões v1 e v2 coexistem temporariamente

### Alternativas

#### Blue/Green Deployment
- Mantém 2 ambientes completos (Blue = atual, Green = novo)
- Troca roteamento instantaneamente
- Rollback imediato (volta para Blue)
- **Custo:** 2x recursos temporariamente

#### Canary Deployment
- Direciona % pequeno do tráfego para nova versão
- Monitora métricas/erros
- Aumenta % gradualmente
- **Requer:** Service Mesh (Istio/Linkerd) ou Ingress avançado

---

## 📈 Observabilidade

### Logs

```bash
# Logs em tempo real
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Últimas 100 linhas
kubectl logs deployment/tech-challenge-app -n tech-challenge --tail=100

# Logs de pods crashados
kubectl logs <pod-name> -n tech-challenge --previous
```

**Recomendação para Produção:** EFK/ELK Stack (Elasticsearch, Fluentd/Logstash, Kibana)

### Métricas

```bash
# Métricas do cluster
kubectl top nodes

# Métricas dos pods
kubectl top pods -n tech-challenge

# Métricas do HPA
kubectl get hpa -n tech-challenge -w
```

**Recomendação para Produção:** Prometheus + Grafana

### Traces (Próximo Passo)

Distribuir rastreamento de requisições:
- **Jaeger** - Distributed tracing
- **Zipkin** - Trace analysis
- **OpenTelemetry** - Instrumentação padronizada

---

## 🎯 Escalabilidade

### Horizontal (HPA) ✅ Implementado

- Adiciona/remove pods automaticamente
- Base: CPU e Memória
- **Limitação:** Não escala recursos por pod

### Vertical (VPA) - Próximo Passo

- Ajusta requests/limits dos pods automaticamente
- Baseado em uso histórico
- **Requer:** Vertical Pod Autoscaler instalado

### Cluster Autoscaler - Infraestrutura

- Adiciona/remove nodes no cluster
- Baseado em pods não agendáveis
- **Cloud-specific:** EKS, GKE, AKS

---

## 🔄 Disaster Recovery

### Backup do PostgreSQL

```bash
# Criar backup manual
kubectl exec -n tech-challenge deployment/postgres -- pg_dump -U tech_user tech_challenge_db > backup.sql

# Restaurar backup
cat backup.sql | kubectl exec -i -n tech-challenge deployment/postgres -- psql -U tech_user -d tech_challenge_db
```

**Recomendação para Produção:**
- **Velero** - Backup do cluster completo
- **CronJobs** - Backups automáticos periódicos
- **RDS/Cloud SQL** - Backups gerenciados automáticos

### Disaster Recovery Plan

1. **PVC Snapshots** - Backup de volumes
2. **GitOps** - Manifestos versionados (ArgoCD)
3. **Multi-Region** - Replicação geográfica
4. **Chaos Engineering** - Testar falhas (Chaos Mesh)

---

## 🌍 Multi-Environment Setup

```
k8s/
├── base/
│   ├── kustomization.yaml
│   ├── app-deployment.yaml
│   ├── configmap.yaml
│   └── secret.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml
    │   ├── replica-patch.yaml (replicas: 1)
    │   └── resource-patch.yaml (menor recursos)
    ├── staging/
    │   ├── kustomization.yaml
    │   ├── replica-patch.yaml (replicas: 2)
    │   └── configmap-patch.yaml
    └── prod/
        ├── kustomization.yaml
        ├── replica-patch.yaml (replicas: 3)
        ├── resource-patch.yaml (mais recursos)
        └── secret-sealed.yaml
```

**Deploy:**
```bash
kubectl apply -k k8s/overlays/dev
kubectl apply -k k8s/overlays/prod
```

---

## 📚 Referências e Leitura Recomendada

- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Spring Boot Kubernetes Guide](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [12-Factor App](https://12factor.net/)
- [CNCF Landscape](https://landscape.cncf.io/)
- [Production Best Practices](https://learnk8s.io/production-best-practices)

---

**Arquitetura criada com foco em:**
- ✅ Alta Disponibilidade
- ✅ Escalabilidade Automática
- ✅ Zero Downtime
- ✅ Observabilidade
- ✅ Segurança
- ✅ Disaster Recovery

