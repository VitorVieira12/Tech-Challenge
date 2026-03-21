# RFC-002: Deploy e Escalabilidade no Amazon EKS

**Status:** Implementado  
**Data:** 2026-03-01  
**Autores:** Tech Challenge Team  
**Revisores:** FIAP 13SOAT

---

## Resumo

Este RFC descreve a estratégia de deploy e escalabilidade da aplicação principal (Spring Boot) no **Amazon EKS**, incluindo configuração de HPA, probes de saúde, rolling updates e monitoramento com New Relic.

---

## Motivação

A aplicação precisa:
1. Ser executada em **containers Docker** em um cluster Kubernetes gerenciado
2. **Escalar horizontalmente** com base na carga de CPU
3. Ter **zero-downtime deployments** via Rolling Update
4. Ser **monitorada** por uma ferramenta APM (New Relic)
5. Ter **health checks** configurados para garantir disponibilidade

---

## Design Detalhado

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tech-challenge-app
  namespace: tech-challenge
spec:
  replicas: 1
  progressDeadlineSeconds: 900    # 15 min para o Spring Boot inicializar
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1           # Permite substituição de 1 pod por vez
  template:
    spec:
      containers:
        - name: tech-challenge-app
          image: vitorvieira12/tech-challenge-app:latest
          ports:
            - containerPort: 8080
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
```

### Health Probes

#### Startup Probe
Aguarda até 15 minutos para o Spring Boot inicializar (JVM + Hibernate DDL + New Relic agent):
```yaml
startupProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 90    # 90 × 10s = 900s = 15 minutos
  successThreshold: 1
```

#### Liveness Probe
Detecta se a aplicação travou e precisa ser reiniciada:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 120
  periodSeconds: 30
  failureThreshold: 3
```

#### Readiness Probe
Indica quando o pod está pronto para receber tráfego:
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 15
  failureThreshold: 3
```

### HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: tech-challenge-hpa
  namespace: tech-challenge
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: tech-challenge-app
  minReplicas: 1
  maxReplicas: 3
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

**Comportamento:**
- CPU < 70%: mantém 1 réplica
- CPU ≥ 70% por 3 minutos: escala para 2 réplicas
- CPU ≥ 70% por mais 3 minutos: escala para 3 réplicas
- CPU < 70% por 5 minutos: escala para baixo (cooldown)

---

## Configuração do New Relic

### Dockerfile
```dockerfile
# Copia o agente New Relic
COPY --from=build /newrelic/newrelic.jar /opt/newrelic/newrelic.jar
COPY --from=build /newrelic/newrelic.yml /opt/newrelic/newrelic.yml

# Ativa o agente via JVM arg
ENTRYPOINT ["java",
  "-javaagent:/opt/newrelic/newrelic.jar",
  "-Xms256m", "-Xmx768m",
  "-jar", "app.jar"]
```

### Kubernetes Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: newrelic-secret
  namespace: tech-challenge
type: Opaque
data:
  NEW_RELIC_LICENSE_KEY: <base64 da license key>
  NEW_RELIC_APP_NAME: VGVjaCBDaGFsbGVuZ2UgLSBPZmljaW5h
```

### Deployment env vars
```yaml
env:
  - name: NEW_RELIC_LICENSE_KEY
    valueFrom:
      secretKeyRef:
        name: newrelic-secret
        key: NEW_RELIC_LICENSE_KEY
  - name: NEW_RELIC_APP_NAME
    valueFrom:
      secretKeyRef:
        name: newrelic-secret
        key: NEW_RELIC_APP_NAME
```

---

## Estratégia de Rolling Update

### Cenário de Deploy
1. Novo push para `fase-3` → CI/CD atualiza a imagem Docker
2. `kubectl set image deployment/tech-challenge-app app=vitorvieira12/tech-challenge-app:latest`
3. Kubernetes termina o pod antigo (maxUnavailable=1) e inicia o novo (maxSurge=1)
4. Novo pod passa pela startup probe (até 15 min)
5. Novo pod passa pela readiness probe → recebe tráfego
6. Pod antigo é terminado

### Zero-Downtime com 1 Réplica
Com apenas 1 réplica e `maxUnavailable=1`, há uma breve janela de indisponibilidade durante o rolling update (~60-120s enquanto o novo pod inicializa). Para zero-downtime real em produção, seriam necessárias pelo menos 2 réplicas.

---

## Configurações do Node Group

```hcl
# terraform/variables.tf
variable "node_instance_types" {
  default = ["t3.small"]
}
variable "node_desired_size" { default = 1 }
variable "node_min_size"     { default = 1 }
variable "node_max_size"     { default = 3 }
```

**Limitações do t3.small (2 vCPU, 2GB RAM):**
- 1 pod Spring Boot + New Relic consome ~800MB RAM
- Deixa ~1.2GB para o sistema operacional e Kubernetes components
- HPA pode escalar até 3 réplicas se a carga exigir (o cluster EC2 Auto Scaling também escala os nodes)

---

## Alternativas Rejeitadas

### AWS Fargate (EKS sem nodes)
Serverless para containers, elimina gerenciamento de nodes. Porém, não elegível para Free Tier e não cumpre requisito de "cluster Kubernetes gerenciado com nodes" da fase.

### 2 Réplicas Iniciais
Aumentaria disponibilidade mas excederia os recursos do t3.small (apenas 2GB RAM total, 2 pods Spring Boot = ~1.6GB, sem margem para o OS).

### t3.medium (4GB RAM)
Resolveria o problema de memória, mas não é Free Tier eligible, gerando custo adicional significativo.

---

## Monitoramento e Observabilidade

### Dashboards New Relic
- **APM**: throughput, response time, error rate
- **Infrastructure**: CPU, memória, disco do node
- **JVM**: heap, GC, threads
- **Kubernetes**: pod status, restarts

### Alertas
- Pod restart > 3 em 10 min → Warning
- CPU node > 85% por 5 min → Warning
- Response time P95 > 2s → Critical
- Error rate > 5% → Critical

---

## Referências

- [Amazon EKS User Guide](https://docs.aws.amazon.com/eks/latest/userguide/)
- [Kubernetes HPA](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [New Relic Java Agent](https://docs.newrelic.com/docs/apm/agents/java-agent/)
