# ☸️ Kubernetes - Tech Challenge

## 📖 Visão Geral

O Tech Challenge agora possui suporte completo para deploy em Kubernetes, oferecendo:

- ✅ **Alta Disponibilidade** - Múltiplas réplicas da aplicação
- ✅ **Escalabilidade Automática** - HPA (Horizontal Pod Autoscaler) baseado em CPU/Memória
- ✅ **Zero Downtime Deployment** - RollingUpdate strategy
- ✅ **Health Checks** - Liveness, Readiness e Startup probes
- ✅ **Resource Management** - Requests e Limits de CPU/Memória
- ✅ **Persistência** - PostgreSQL com PersistentVolumeClaim
- ✅ **Service Mesh Ready** - Labels e annotations prontos para Istio/Linkerd

---

## 📁 Estrutura do Diretório `/k8s`

```
k8s/
├── namespace.yaml              # Namespace isolado
├── configmap.yaml             # Configurações não sensíveis
├── secret.yaml                # Credenciais (base64)
├── postgres-pvc.yaml          # Volume persistente para PostgreSQL
├── postgres-deployment.yaml   # Deploy do banco de dados
├── postgres-service.yaml      # Service interno do PostgreSQL
├── app-deployment.yaml        # Deploy da aplicação Spring Boot
├── app-service.yaml           # Service da aplicação (LoadBalancer)
├── hpa.yaml                   # Horizontal Pod Autoscaler
├── ingress.yaml               # Roteamento HTTP (opcional)
├── kustomization.yaml         # Configuração Kustomize
├── deploy.sh                  # Script de deploy automatizado
├── cleanup.sh                 # Script de limpeza
├── README.md                  # Documentação completa
└── QUICKSTART.md              # Guia rápido
```

---

## 🚀 Deploy Rápido

### Pré-requisitos

1. Kubernetes cluster configurado (Minikube, Kind, EKS, GKE, AKS)
2. `kubectl` instalado e configurado
3. Metrics Server instalado (para HPA)

### 3 Formas de Deploy

#### 1️⃣ Script Automatizado (Recomendado)

```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```

#### 2️⃣ Kubectl Apply Direto

```bash
kubectl apply -f k8s/
```

#### 3️⃣ Kustomize

```bash
kubectl apply -k k8s/
```

---

## 🎯 Recursos Configurados

### 1. **Namespace**

Isola todos os recursos em um namespace dedicado `tech-challenge`.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: tech-challenge
```

### 2. **ConfigMap** - Variáveis de Ambiente

Armazena configurações não sensíveis:

- `DB_HOST`: postgres-service
- `DB_PORT`: 5432
- `DB_NAME`: tech_challenge_db
- `SPRING_PROFILES_ACTIVE`: prod
- `JWT_EXPIRATION`: 86400000 (24h)
- `TZ`: America/Sao_Paulo

### 3. **Secret** - Dados Sensíveis (Base64)

Armazena credenciais de forma segura:

- `DB_USERNAME`: tech_user
- `DB_PASSWORD`: tech_password_2024
- `JWT_SECRET`: my-secret-key-for-jwt-token-generation-2024
- `ADMIN_USERNAME`: admin
- `ADMIN_PASSWORD`: admin@2024

### 4. **PostgreSQL Deployment**

- **Réplicas:** 1 (StatefulSet para produção)
- **Storage:** 5Gi PersistentVolumeClaim
- **Resources:**
  - Request: 250m CPU, 256Mi RAM
  - Limit: 500m CPU, 512Mi RAM
- **Health Checks:** Liveness e Readiness probes

### 5. **Aplicação Deployment**

- **Réplicas Iniciais:** 2
- **Strategy:** RollingUpdate (maxSurge: 1, maxUnavailable: 0)
- **Resources:**
  - Request: 500m CPU, 512Mi RAM
  - Limit: 1000m CPU, 1Gi RAM
- **Init Container:** Aguarda PostgreSQL ficar pronto
- **Health Checks:** Liveness, Readiness e Startup probes
- **JVM Opts:** `-Xms512m -Xmx1024m -XX:+UseG1GC`

### 6. **Services**

#### PostgreSQL Service (ClusterIP)
- Acessível apenas dentro do cluster
- Porta: 5432

#### Aplicação Service (LoadBalancer)
- Expõe a aplicação externamente
- Porta externa: 80 → Porta interna: 8080
- Session Affinity: ClientIP (3 horas)

### 7. **Horizontal Pod Autoscaler (HPA)**

Escala automaticamente de **1 a 5 réplicas** baseado em:

- **CPU Target:** 70% de utilização
- **Memory Target:** 80% de utilização

**Comportamento:**
- **Scale Up:** Imediato (até 2 pods a cada 30s)
- **Scale Down:** Aguarda 5 min (remove máx 50% dos pods por vez)

### 8. **Ingress** (Opcional)

Roteamento HTTP com NGINX Ingress Controller:

- Host: `tech-challenge.local`
- TLS/SSL suportado
- Rate Limiting: 100 req/s
- CORS habilitado

---

## 📊 Monitoramento e Health Checks

### Health Endpoints (Spring Actuator)

A aplicação expõe os seguintes endpoints:

- `/actuator/health` - Health geral
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/metrics` - Métricas da aplicação

### Kubernetes Probes

#### Liveness Probe
- **Propósito:** Reinicia o container se falhar
- **Endpoint:** `/actuator/health/liveness`
- **Initial Delay:** 90s
- **Period:** 10s
- **Timeout:** 5s
- **Failure Threshold:** 3

#### Readiness Probe
- **Propósito:** Remove do load balancer se não estiver pronto
- **Endpoint:** `/actuator/health/readiness`
- **Initial Delay:** 60s
- **Period:** 5s
- **Timeout:** 3s
- **Failure Threshold:** 3

#### Startup Probe
- **Propósito:** Dá tempo para a aplicação iniciar
- **Endpoint:** `/actuator/health`
- **Initial Delay:** 30s
- **Period:** 10s
- **Timeout:** 3s
- **Failure Threshold:** 12 (120s total)

---

## 🔐 Gerenciamento de Secrets

### Visualizar Secrets

```bash
# Ver secret decodificado
kubectl get secret app-secrets -n tech-challenge -o jsonpath='{.data.DB_PASSWORD}' | base64 -d

# Ver todos os dados
kubectl get secret app-secrets -n tech-challenge -o yaml
```

### Atualizar Secrets

```bash
# Método 1: Editar diretamente
kubectl edit secret app-secrets -n tech-challenge

# Método 2: Recriar o secret
kubectl delete secret app-secrets -n tech-challenge
kubectl apply -f k8s/secret.yaml
```

### Gerar Novos Valores Base64

```bash
echo -n "sua-nova-senha" | base64
```

---

## 🧪 Testando o HPA

Para verificar o escalonamento automático:

```bash
# Terminal 1: Gerar carga
kubectl run -i --tty load-generator --rm --image=busybox:1.36 --restart=Never -n tech-challenge -- /bin/sh -c "while sleep 0.01; do wget -q -O- http://tech-challenge-service/api/clientes; done"

# Terminal 2: Observar o HPA
kubectl get hpa -n tech-challenge -w

# Terminal 3: Observar os pods
kubectl get pods -n tech-challenge -w
```

**Esperado:**
1. CPU utilization aumenta acima de 70%
2. HPA cria novos pods (até 5 máximo)
3. Após parar a carga, aguarda 5 min
4. HPA remove pods gradualmente (voltando ao mínimo de 1)

---

## 🌐 Acessando a Aplicação

### Minikube

```bash
# Obter URL do service
minikube service tech-challenge-service -n tech-challenge --url

# Ou abrir no navegador
minikube service tech-challenge-service -n tech-challenge
```

### LoadBalancer (Cloud)

```bash
# Obter IP externo
kubectl get svc tech-challenge-service -n tech-challenge

# Acesse: http://<EXTERNAL-IP>
```

### Port Forward (Local/Desenvolvimento)

```bash
kubectl port-forward svc/tech-challenge-service 8080:80 -n tech-challenge

# Acesse: http://localhost:8080
```

### Ingress

```bash
# Adicione ao /etc/hosts (Linux/Mac):
echo "$(kubectl get ingress tech-challenge-ingress -n tech-challenge -o jsonpath='{.status.loadBalancer.ingress[0].ip}') tech-challenge.local" | sudo tee -a /etc/hosts

# Windows: Adicione manualmente em C:\Windows\System32\drivers\etc\hosts

# Acesse: http://tech-challenge.local
```

---

## 📝 Comandos Úteis

### Status e Monitoramento

```bash
# Ver todos os recursos
kubectl get all -n tech-challenge

# Ver pods com detalhes
kubectl get pods -n tech-challenge -o wide

# Logs da aplicação
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Logs do PostgreSQL
kubectl logs -f deployment/postgres -n tech-challenge

# Métricas (requer Metrics Server)
kubectl top nodes
kubectl top pods -n tech-challenge
```

### Debug

```bash
# Descrever pod
kubectl describe pod <pod-name> -n tech-challenge

# Eventos recentes
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# Shell interativo no pod
kubectl exec -it <pod-name> -n tech-challenge -- /bin/sh

# Testar conectividade ao PostgreSQL
kubectl exec -it <app-pod-name> -n tech-challenge -- nc -zv postgres-service 5432
```

### Gerenciamento

```bash
# Escalar manualmente
kubectl scale deployment tech-challenge-app --replicas=3 -n tech-challenge

# Rollback para versão anterior
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge

# Histórico de rollouts
kubectl rollout history deployment/tech-challenge-app -n tech-challenge

# Atualizar imagem
kubectl set image deployment/tech-challenge-app app=tech-challenge:v2 -n tech-challenge

# Reiniciar pods (rollout restart)
kubectl rollout restart deployment/tech-challenge-app -n tech-challenge
```

---

## 🗑️ Limpeza

### Script Automatizado

```bash
cd k8s
chmod +x cleanup.sh
./cleanup.sh
```

### Manual

```bash
# Deletar namespace (remove tudo)
kubectl delete namespace tech-challenge

# Ou deletar recursos individualmente
kubectl delete -f k8s/
```

---

## 🏢 Ambientes (Dev, Staging, Prod)

Para gerenciar múltiplos ambientes, use **Kustomize Overlays**:

```
k8s/
├── base/
│   ├── kustomization.yaml
│   ├── app-deployment.yaml
│   └── ...
├── overlays/
│   ├── dev/
│   │   ├── kustomization.yaml
│   │   └── replica-patch.yaml
│   ├── staging/
│   │   └── kustomization.yaml
│   └── prod/
│       └── kustomization.yaml
```

Deploy por ambiente:

```bash
# Dev
kubectl apply -k k8s/overlays/dev

# Staging
kubectl apply -k k8s/overlays/staging

# Prod
kubectl apply -k k8s/overlays/prod
```

---

## 🔒 Segurança - Boas Práticas Implementadas

✅ **Namespace Isolation** - Recursos isolados  
✅ **Secrets Management** - Credenciais em base64  
✅ **Non-root User** - Containers não rodam como root  
✅ **Resource Limits** - Previne resource starvation  
✅ **Readiness Probes** - Remove pods não saudáveis do load balancer  
✅ **Liveness Probes** - Reinicia containers com problemas  
✅ **Rolling Updates** - Zero downtime deployments  
✅ **PVC** - Dados persistentes para o PostgreSQL  

### Recomendações Adicionais para Produção

1. **Network Policies** - Controlar tráfego entre pods
2. **Pod Security Policies** - Restringir privilégios dos containers
3. **RBAC** - Controle de acesso baseado em roles
4. **TLS/SSL** - Certificados para comunicação segura
5. **Secret Management Avançado** - Usar Sealed Secrets, Vault ou External Secrets Operator

---

## 📚 Próximos Passos

1. **Monitoramento:** Prometheus + Grafana
2. **Logging:** EFK/ELK Stack
3. **CI/CD:** GitHub Actions, Jenkins, ArgoCD
4. **Service Mesh:** Istio ou Linkerd
5. **Backup:** Velero para backup do cluster

---

## 🆘 Troubleshooting

Consulte o [README.md](k8s/README.md) completo na pasta `/k8s` para soluções detalhadas de problemas comuns.

---

## 📞 Referências

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [HPA Walkthrough](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/)
- [Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)


