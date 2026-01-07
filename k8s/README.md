# Kubernetes Deployment - Tech Challenge

Este diretório contém todos os manifestos Kubernetes necessários para fazer o deploy da aplicação Tech Challenge.

## 📋 Estrutura de Arquivos

```
k8s/
├── namespace.yaml              # Namespace isolado para a aplicação
├── configmap.yaml             # Variáveis de ambiente não sensíveis
├── secret.yaml                # Credenciais e dados sensíveis (base64)
├── postgres-pvc.yaml          # Persistent Volume Claim para PostgreSQL
├── postgres-deployment.yaml   # Deployment do PostgreSQL
├── postgres-service.yaml      # Service do PostgreSQL (ClusterIP)
├── app-deployment.yaml        # Deployment da aplicação Spring Boot
├── app-service.yaml           # Service da aplicação (LoadBalancer)
├── hpa.yaml                   # Horizontal Pod Autoscaler
├── ingress.yaml               # Ingress para roteamento HTTP (opcional)
└── README.md                  # Este arquivo
```

## 🚀 Deploy Completo

### 1. Pré-requisitos

- Kubernetes cluster configurado (Minikube, Kind, EKS, GKE, AKS, etc.)
- `kubectl` instalado e configurado
- Metrics Server instalado (para HPA funcionar)
- (Opcional) NGINX Ingress Controller (para usar Ingress)

#### Instalar Metrics Server (necessário para HPA)

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Para Minikube, habilite o addon:
minikube addons enable metrics-server
```

#### Instalar NGINX Ingress Controller (opcional)

```bash
# Para Minikube:
minikube addons enable ingress

# Para outros clusters:
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

### 2. Build da Imagem Docker

Antes de fazer o deploy, você precisa buildar e disponibilizar a imagem Docker:

```bash
# Build da imagem
docker build -t tech-challenge:latest .

# Para Minikube, use o Docker daemon do Minikube:
eval $(minikube docker-env)
docker build -t tech-challenge:latest .

# Para outros clusters, faça push para um registry:
docker tag tech-challenge:latest seu-usuario/tech-challenge:latest
docker push seu-usuario/tech-challenge:latest
```

### 3. Aplicar os Manifestos

Execute os comandos na ordem abaixo:

```bash
# 1. Criar namespace
kubectl apply -f namespace.yaml

# 2. Criar ConfigMap e Secrets
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 3. Deploy do PostgreSQL
kubectl apply -f postgres-pvc.yaml
kubectl apply -f postgres-deployment.yaml
kubectl apply -f postgres-service.yaml

# Aguarde o PostgreSQL estar pronto
kubectl wait --for=condition=ready pod -l app=postgres -n tech-challenge --timeout=120s

# 4. Deploy da Aplicação
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml

# 5. HPA (Horizontal Pod Autoscaler)
kubectl apply -f hpa.yaml

# 6. (Opcional) Ingress
kubectl apply -f ingress.yaml
```

### 4. Aplicar Tudo de Uma Vez (alternativa)

```bash
kubectl apply -f k8s/
```

## 🔍 Verificação e Monitoramento

### Verificar Status dos Pods

```bash
# Ver todos os pods
kubectl get pods -n tech-challenge

# Ver pods com mais detalhes
kubectl get pods -n tech-challenge -o wide

# Logs da aplicação
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Logs do PostgreSQL
kubectl logs -f deployment/postgres -n tech-challenge
```

### Verificar Services

```bash
# Listar services
kubectl get svc -n tech-challenge

# Para LoadBalancer, obter IP externo
kubectl get svc tech-challenge-service -n tech-challenge

# Para Minikube, obter URL do service
minikube service tech-challenge-service -n tech-challenge --url
```

### Verificar HPA

```bash
# Status do HPA
kubectl get hpa -n tech-challenge

# Detalhes do HPA
kubectl describe hpa tech-challenge-hpa -n tech-challenge

# Monitorar em tempo real
kubectl get hpa -n tech-challenge -w
```

### Verificar Ingress

```bash
# Status do Ingress
kubectl get ingress -n tech-challenge

# Detalhes do Ingress
kubectl describe ingress tech-challenge-ingress -n tech-challenge

# Para Minikube, adicione ao /etc/hosts:
echo "$(minikube ip) tech-challenge.local" | sudo tee -a /etc/hosts
```

## 📊 Teste de Escalonamento Automático

Para testar o HPA, você pode gerar carga na aplicação:

```bash
# Gerar carga (usando kubectl run)
kubectl run -i --tty load-generator --rm --image=busybox:1.36 --restart=Never -n tech-challenge -- /bin/sh -c "while sleep 0.01; do wget -q -O- http://tech-challenge-service/api/clientes; done"

# Em outro terminal, observe o HPA escalando
kubectl get hpa -n tech-challenge -w
```

## 🔧 Configurações Importantes

### Resources (CPU e Memória)

**PostgreSQL:**
- Requests: 250m CPU, 256Mi RAM
- Limits: 500m CPU, 512Mi RAM

**Aplicação:**
- Requests: 500m CPU, 512Mi RAM
- Limits: 1000m CPU (1 core), 1Gi RAM

### HPA (Horizontal Pod Autoscaler)

- **Min Replicas:** 1
- **Max Replicas:** 5
- **CPU Target:** 70% de utilização
- **Memory Target:** 80% de utilização
- **Scale Down:** Aguarda 5 minutos, remove no máximo 50% dos pods por vez
- **Scale Up:** Imediato, pode dobrar o número de pods

### Health Checks

**Liveness Probe:** Verifica se o container está vivo (reinicia se falhar)
**Readiness Probe:** Verifica se o container está pronto para receber tráfego
**Startup Probe:** Dá tempo para a aplicação iniciar (120 segundos máximo)

## 🔐 Gerenciamento de Secrets

### Visualizar Secrets (decodificados)

```bash
# Ver secret decodificado
kubectl get secret app-secrets -n tech-challenge -o jsonpath='{.data.DB_PASSWORD}' | base64 -d

# Ver todos os secrets
kubectl get secret app-secrets -n tech-challenge -o yaml
```

### Criar Novos Secrets

```bash
# Criar secret a partir de valores literais
kubectl create secret generic app-secrets \
  --from-literal=DB_USERNAME=tech_user \
  --from-literal=DB_PASSWORD=tech_password_2024 \
  --from-literal=JWT_SECRET=my-secret-key \
  -n tech-challenge

# Criar secret a partir de arquivo
kubectl create secret generic app-secrets \
  --from-file=./secrets.env \
  -n tech-challenge
```

### Atualizar Secrets

```bash
# Editar secret diretamente
kubectl edit secret app-secrets -n tech-challenge

# Ou deletar e recriar
kubectl delete secret app-secrets -n tech-challenge
kubectl apply -f secret.yaml
```

## 🗑️ Limpeza (Deletar tudo)

```bash
# Deletar namespace (remove tudo dentro dele)
kubectl delete namespace tech-challenge

# Ou deletar recursos individualmente
kubectl delete -f k8s/
```

## 🐛 Troubleshooting

### Pod não inicia

```bash
# Ver eventos do pod
kubectl describe pod <pod-name> -n tech-challenge

# Ver logs do container
kubectl logs <pod-name> -n tech-challenge

# Ver logs anteriores (se o container crashou)
kubectl logs <pod-name> -n tech-challenge --previous
```

### Aplicação não conecta ao banco

```bash
# Verificar se o PostgreSQL está rodando
kubectl get pods -l app=postgres -n tech-challenge

# Testar conectividade do pod da aplicação ao PostgreSQL
kubectl exec -it <app-pod-name> -n tech-challenge -- nc -zv postgres-service 5432

# Ver variáveis de ambiente do pod
kubectl exec <app-pod-name> -n tech-challenge -- env | grep DB
```

### HPA não escala

```bash
# Verificar se o Metrics Server está rodando
kubectl get deployment metrics-server -n kube-system

# Verificar se as métricas estão disponíveis
kubectl top nodes
kubectl top pods -n tech-challenge

# Ver eventos do HPA
kubectl describe hpa tech-challenge-hpa -n tech-challenge
```

### Service não responde

```bash
# Verificar endpoints do service
kubectl get endpoints tech-challenge-service -n tech-challenge

# Port-forward para teste local
kubectl port-forward svc/tech-challenge-service 8080:80 -n tech-challenge

# Testar
curl http://localhost:8080/api/clientes
```

## 📚 Referências

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [Resource Management](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/)
- [Health Checks](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Secrets Management](https://kubernetes.io/docs/concepts/configuration/secret/)

## 🎯 Próximos Passos

1. **Monitoramento:** Configurar Prometheus + Grafana
2. **Logging:** Implementar EFK/ELK Stack (Elasticsearch, Fluentd, Kibana)
3. **CI/CD:** Integrar com GitHub Actions, Jenkins, ou ArgoCD
4. **Service Mesh:** Considerar Istio ou Linkerd para observabilidade avançada
5. **Backup:** Configurar backup automático do PostgreSQL
6. **SSL/TLS:** Configurar certificados Let's Encrypt com cert-manager

