# 📚 Índice Completo - Kubernetes

## 🚀 Começar Aqui

1. **[QUICKSTART.md](QUICKSTART.md)** - Deploy rápido em 3 comandos
2. **[README.md](README.md)** - Documentação completa e detalhada
3. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Arquitetura e design decisions

---

## 📄 Manifestos Kubernetes

### Core Resources

| Arquivo | Descrição | Recurso |
|---------|-----------|---------|
| **[namespace.yaml](namespace.yaml)** | Namespace isolado | `Namespace` |
| **[configmap.yaml](configmap.yaml)** | Variáveis de ambiente não sensíveis | `ConfigMap` |
| **[secret.yaml](secret.yaml)** | Credenciais e dados sensíveis (base64) | `Secret` |

### PostgreSQL

| Arquivo | Descrição | Recurso |
|---------|-----------|---------|
| **[postgres-pvc.yaml](postgres-pvc.yaml)** | Volume persistente (5Gi) | `PersistentVolumeClaim` |
| **[postgres-deployment.yaml](postgres-deployment.yaml)** | Deployment do banco de dados | `Deployment` |
| **[postgres-service.yaml](postgres-service.yaml)** | Service interno (ClusterIP) | `Service` |

### Aplicação

| Arquivo | Descrição | Recurso |
|---------|-----------|---------|
| **[app-deployment.yaml](app-deployment.yaml)** | Deployment da aplicação Spring Boot | `Deployment` |
| **[app-service.yaml](app-service.yaml)** | Service externo (LoadBalancer) | `Service` |
| **[hpa.yaml](hpa.yaml)** | Escalonamento automático (1-5 réplicas) | `HorizontalPodAutoscaler` |
| **[ingress.yaml](ingress.yaml)** | Roteamento HTTP (opcional) | `Ingress` |

### Utilitários

| Arquivo | Descrição | Tipo |
|---------|-----------|------|
| **[kustomization.yaml](kustomization.yaml)** | Configuração Kustomize | `Kustomization` |
| **[deploy.sh](deploy.sh)** | Script de deploy automatizado | `Bash Script` |
| **[cleanup.sh](cleanup.sh)** | Script de limpeza | `Bash Script` |

---

## 📖 Documentação

### Guias

| Documento | Conteúdo | Audiência |
|-----------|----------|-----------|
| **[QUICKSTART.md](QUICKSTART.md)** | Deploy em 5 minutos | DevOps, Desenvolvedores |
| **[README.md](README.md)** | Documentação completa | Todos |
| **[ARCHITECTURE.md](ARCHITECTURE.md)** | Arquitetura detalhada | Arquitetos, DevOps |
| **[INDEX.md](INDEX.md)** | Este arquivo - Índice navegável | Todos |

---

## 🎯 Casos de Uso

### Deploy Inicial

```bash
# Escolha uma opção:

# Opção 1: Script automatizado (Linux/Mac)
cd k8s && chmod +x deploy.sh && ./deploy.sh

# Opção 2: Kubectl direto
kubectl apply -f k8s/

# Opção 3: Kustomize
kubectl apply -k k8s/
```

📖 **Ver:** [QUICKSTART.md](QUICKSTART.md)

### Desenvolvimento Local (Minikube)

```bash
# 1. Iniciar Minikube
minikube start --cpus=4 --memory=4096

# 2. Habilitar addons
minikube addons enable metrics-server ingress

# 3. Build imagem no Docker do Minikube
eval $(minikube docker-env)
docker build -t tech-challenge:latest .

# 4. Deploy
cd k8s && kubectl apply -f .

# 5. Acessar
minikube service tech-challenge-service -n tech-challenge
```

📖 **Ver:** [QUICKSTART.md - Minikube](QUICKSTART.md#⚙️-configuração-rápida-para-minikube)

### Teste de Carga (HPA)

```bash
# Terminal 1: Gerar carga
kubectl run -i --tty load-generator --rm --image=busybox:1.36 \
  --restart=Never -n tech-challenge -- /bin/sh -c \
  "while sleep 0.01; do wget -q -O- http://tech-challenge-service/api/clientes; done"

# Terminal 2: Observar escalonamento
kubectl get hpa -n tech-challenge -w
```

📖 **Ver:** [README.md - Teste de Escalonamento](README.md#📊-teste-de-escalonamento-automático)

### Monitoramento e Debug

```bash
# Logs em tempo real
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Status dos recursos
kubectl get all -n tech-challenge

# Métricas
kubectl top pods -n tech-challenge

# Eventos
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'
```

📖 **Ver:** [README.md - Monitoramento](README.md#🔍-verificação-e-monitoramento)

### Limpeza

```bash
# Script automatizado
cd k8s && chmod +x cleanup.sh && ./cleanup.sh

# Ou deletar namespace (remove tudo)
kubectl delete namespace tech-challenge
```

📖 **Ver:** [QUICKSTART.md - Limpeza](QUICKSTART.md#🗑️-limpeza)

---

## 🔧 Configurações Principais

### Resources (CPU e Memória)

| Componente | CPU Request | CPU Limit | Memory Request | Memory Limit |
|------------|-------------|-----------|----------------|--------------|
| **Aplicação** | 500m (0.5 core) | 1000m (1 core) | 512Mi | 1Gi |
| **PostgreSQL** | 250m | 500m | 256Mi | 512Mi |

📖 **Ver:** [ARCHITECTURE.md - Resource Management](ARCHITECTURE.md#📊-resource-management)

### HPA (Horizontal Pod Autoscaler)

- **Min Replicas:** 1
- **Max Replicas:** 5
- **CPU Target:** 70% utilization
- **Memory Target:** 80% utilization
- **Scale Up:** Imediato (máx 2 pods a cada 30s)
- **Scale Down:** Após 5 min (máx 50% por vez)

📖 **Ver:** [hpa.yaml](hpa.yaml) | [ARCHITECTURE.md - HPA](ARCHITECTURE.md#7-horizontal-pod-autoscaler-hpayaml)

### Health Checks

| Probe | Endpoint | Initial Delay | Period | Timeout | Failures |
|-------|----------|---------------|--------|---------|----------|
| **Liveness** | `/actuator/health/liveness` | 90s | 10s | 5s | 3 |
| **Readiness** | `/actuator/health/readiness` | 60s | 5s | 3s | 3 |
| **Startup** | `/actuator/health` | 30s | 10s | 3s | 12 |

📖 **Ver:** [app-deployment.yaml](app-deployment.yaml) | [ARCHITECTURE.md - Health Checks](ARCHITECTURE.md#🔍-health-checks-detalhados)

### Secrets (Base64 Encoded)

| Key | Valor Padrão | Uso |
|-----|--------------|-----|
| `DB_USERNAME` | tech_user | PostgreSQL user |
| `DB_PASSWORD` | tech_password_2024 | PostgreSQL password |
| `JWT_SECRET` | my-secret-key-for-jwt... | JWT token signing |
| `ADMIN_USERNAME` | admin | Admin user |
| `ADMIN_PASSWORD` | admin@2024 | Admin password |

⚠️ **IMPORTANTE:** Altere os valores padrão em produção!

📖 **Ver:** [secret.yaml](secret.yaml) | [README.md - Secrets](README.md#🔐-gerenciamento-de-secrets)

---

## 🛠️ Troubleshooting Rápido

| Problema | Comando de Debug | Solução |
|----------|------------------|---------|
| **Pods não iniciam** | `kubectl describe pod <pod> -n tech-challenge` | Verificar eventos e logs |
| **ImagePullBackOff** | `docker images \| grep tech-challenge` | Verificar se imagem existe |
| **HPA não escala** | `kubectl top nodes` | Instalar Metrics Server |
| **App não conecta ao DB** | `kubectl logs -f deployment/postgres -n tech-challenge` | Verificar se PostgreSQL está ready |
| **Service sem IP externo** | `minikube service tech-challenge-service -n tech-challenge --url` | Usar Minikube service ou port-forward |
| **Pod crashando** | `kubectl logs <pod> -n tech-challenge --previous` | Ver logs do container anterior |

📖 **Ver:** [README.md - Troubleshooting](README.md#🐛-troubleshooting)

---

## 📊 Diagramas e Visualizações

### Arquitetura Geral

```
Internet → LoadBalancer → HPA → App Pods (1-5) → PostgreSQL Service → PostgreSQL Pod
                                      ↓
                                 ConfigMap + Secret
                                      ↓
                                 PersistentVolume (5Gi)
```

📖 **Ver:** [ARCHITECTURE.md - Diagrama](ARCHITECTURE.md#📐-diagrama-da-arquitetura)

### Fluxo de Requisição

```
1. User Request → LoadBalancer
2. LoadBalancer → App Service (Session Affinity)
3. App Service → Pod (Readiness Probe verificado)
4. Pod → PostgreSQL Service
5. PostgreSQL Service → PostgreSQL Pod
6. Response → User
```

📖 **Ver:** [ARCHITECTURE.md - Fluxo](ARCHITECTURE.md#🔄-fluxo-de-requisição)

---

## 🎓 Conceitos Kubernetes Abordados

- ✅ **Namespaces** - Isolamento de recursos
- ✅ **Deployments** - Gerenciamento de réplicas
- ✅ **Services** - Load balancing interno/externo
- ✅ **ConfigMaps** - Configurações desacopladas
- ✅ **Secrets** - Gerenciamento de credenciais
- ✅ **PersistentVolumeClaims** - Armazenamento persistente
- ✅ **HPA** - Escalonamento automático horizontal
- ✅ **Probes** - Health checks (Liveness, Readiness, Startup)
- ✅ **Init Containers** - Inicialização ordenada
- ✅ **Resource Requests/Limits** - QoS e resource management
- ✅ **Rolling Updates** - Zero downtime deployments
- ✅ **Ingress** - Roteamento HTTP avançado
- ✅ **Kustomize** - Gerenciamento de manifestos

📖 **Ver:** [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 🌟 Próximos Passos (Roadmap)

### Curto Prazo
- [ ] Implementar Network Policies
- [ ] Configurar RBAC granular
- [ ] Adicionar PodDisruptionBudget
- [ ] Configurar Resource Quotas

### Médio Prazo
- [ ] Prometheus + Grafana (monitoring)
- [ ] EFK Stack (logging centralizado)
- [ ] Sealed Secrets (secrets criptografados)
- [ ] CI/CD com GitHub Actions + ArgoCD

### Longo Prazo
- [ ] Service Mesh (Istio/Linkerd)
- [ ] Multi-region deployment
- [ ] Disaster Recovery automático
- [ ] Chaos Engineering (Chaos Mesh)

📖 **Ver:** [ARCHITECTURE.md - Próximos Passos](ARCHITECTURE.md#🎯-escalabilidade)

---

## 📞 Suporte e Referências

### Documentação Oficial

- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Spring Boot on K8s](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [HPA Walkthrough](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/)

### Ferramentas Úteis

- **kubectl** - CLI oficial do Kubernetes
- **k9s** - Terminal UI para Kubernetes
- **Lens** - IDE para Kubernetes (GUI)
- **Helm** - Package manager para Kubernetes
- **Kustomize** - Template-free customization

### Comunidade

- [Kubernetes Slack](https://kubernetes.slack.com/)
- [CNCF](https://www.cncf.io/)
- [LearnK8s](https://learnk8s.io/)

---

## 📝 Contribuindo

Melhorias e sugestões são bem-vindas! Áreas de interesse:

- Performance tuning
- Security hardening
- Monitoring/Observability
- Automation
- Documentation

---

**Última atualização:** Janeiro 2026  
**Versão Kubernetes:** 1.28+  
**Spring Boot:** 3.3.5  
**PostgreSQL:** 16


