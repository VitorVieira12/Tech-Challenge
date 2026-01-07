# 🚀 Quick Start - Kubernetes Deployment

## Deploy Rápido (1 Comando)

### Opção 1: Script Automatizado (Linux/Mac)

```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```

### Opção 2: Kubectl Apply (Qualquer SO)

```bash
cd k8s
kubectl apply -f .
```

### Opção 3: Kustomize

```bash
kubectl apply -k k8s/
```

---

## ⚙️ Configuração Rápida para Minikube

```bash
# 1. Iniciar Minikube
minikube start --cpus=4 --memory=4096

# 2. Habilitar addons necessários
minikube addons enable metrics-server
minikube addons enable ingress

# 3. Usar o Docker do Minikube
eval $(minikube docker-env)

# 4. Build da imagem
docker build -t tech-challenge:latest .

# 5. Deploy
cd k8s
kubectl apply -f .

# 6. Obter URL
minikube service tech-challenge-service -n tech-challenge --url
```

---

## 📋 Checklist Pré-Deploy

- [ ] Kubernetes cluster configurado (`kubectl cluster-info`)
- [ ] Metrics Server instalado (necessário para HPA)
- [ ] Imagem Docker buildada e disponível
- [ ] Secrets configurados corretamente
- [ ] Recursos suficientes no cluster (4GB RAM, 2 CPUs mínimo)

---

## 🔍 Comandos Úteis

### Ver Status

```bash
# Pods
kubectl get pods -n tech-challenge -w

# Services
kubectl get svc -n tech-challenge

# HPA
kubectl get hpa -n tech-challenge

# Logs
kubectl logs -f deployment/tech-challenge-app -n tech-challenge
```

### Teste de Carga (HPA)

```bash
# Gerar carga
kubectl run -i --tty load-generator --rm --image=busybox:1.36 --restart=Never -n tech-challenge -- /bin/sh -c "while sleep 0.01; do wget -q -O- http://tech-challenge-service/api/clientes; done"

# Em outro terminal, observe o HPA
kubectl get hpa -n tech-challenge -w
```

### Debug

```bash
# Acessar shell do pod
kubectl exec -it <pod-name> -n tech-challenge -- /bin/sh

# Ver eventos
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# Descrever pod
kubectl describe pod <pod-name> -n tech-challenge
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
kubectl delete namespace tech-challenge
```

---

## 🌐 Acessar a Aplicação

### LoadBalancer (Cloud)

```bash
kubectl get svc tech-challenge-service -n tech-challenge
# Acesse: http://<EXTERNAL-IP>
```

### Minikube

```bash
minikube service tech-challenge-service -n tech-challenge
```

### Port Forward (Local)

```bash
kubectl port-forward svc/tech-challenge-service 8080:80 -n tech-challenge
# Acesse: http://localhost:8080
```

### Ingress

```bash
# Adicione ao /etc/hosts:
echo "$(kubectl get ingress tech-challenge-ingress -n tech-challenge -o jsonpath='{.status.loadBalancer.ingress[0].ip}') tech-challenge.local" | sudo tee -a /etc/hosts

# Acesse: http://tech-challenge.local
```

---

## 📊 Endpoints da API

Após o deploy, acesse:

- **Swagger UI:** `http://<URL>/swagger-ui.html`
- **Health Check:** `http://<URL>/actuator/health`
- **Metrics:** `http://<URL>/actuator/metrics`
- **API Base:** `http://<URL>/api`

### Exemplo de Requisição

```bash
# Obter token (primeiro faça login)
TOKEN=$(curl -X POST http://<URL>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  | jq -r '.token')

# Listar clientes
curl http://<URL>/api/clientes \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🎯 Especificações do Deployment

### Aplicação (app-deployment.yaml)

- **Réplicas Iniciais:** 2
- **CPU Request:** 500m (0.5 core)
- **CPU Limit:** 1000m (1 core)
- **Memory Request:** 512Mi
- **Memory Limit:** 1Gi
- **Strategy:** RollingUpdate (Zero Downtime)

### PostgreSQL (postgres-deployment.yaml)

- **Réplicas:** 1
- **CPU Request:** 250m
- **CPU Limit:** 500m
- **Memory Request:** 256Mi
- **Memory Limit:** 512Mi
- **Storage:** 5Gi (PVC)

### HPA (hpa.yaml)

- **Min Replicas:** 1
- **Max Replicas:** 5
- **CPU Target:** 70%
- **Memory Target:** 80%
- **Scale Up:** Imediato (máx 2 pods a cada 30s)
- **Scale Down:** Após 5 minutos (máx 50% dos pods por vez)

---

## 🔐 Secrets (Base64 Encoded)

**Para visualizar:**

```bash
kubectl get secret app-secrets -n tech-challenge -o jsonpath='{.data.DB_PASSWORD}' | base64 -d
```

**Para atualizar:**

```bash
# Gerar novo base64
echo -n "nova-senha" | base64

# Editar secret
kubectl edit secret app-secrets -n tech-challenge
```

**Valores padrão:**
- DB_USERNAME: `tech_user`
- DB_PASSWORD: `tech_password_2024`
- JWT_SECRET: `my-secret-key-for-jwt-token-generation-2024`
- ADMIN_USERNAME: `admin`
- ADMIN_PASSWORD: `admin@2024`

---

## 💡 Dicas

1. **Metrics Server:** Essencial para HPA funcionar
   ```bash
   kubectl top nodes
   kubectl top pods -n tech-challenge
   ```

2. **Logs Centralizados:** Use `-f` para follow
   ```bash
   kubectl logs -f deployment/tech-challenge-app -n tech-challenge --tail=100
   ```

3. **Rollback:** Se algo der errado
   ```bash
   kubectl rollout undo deployment/tech-challenge-app -n tech-challenge
   ```

4. **Escalar Manualmente:**
   ```bash
   kubectl scale deployment tech-challenge-app --replicas=3 -n tech-challenge
   ```

5. **Atualizar Imagem:**
   ```bash
   kubectl set image deployment/tech-challenge-app app=tech-challenge:v2 -n tech-challenge
   ```

---

## 🐛 Troubleshooting Rápido

| Problema | Solução |
|----------|---------|
| Pods com status `ImagePullBackOff` | Verifique se a imagem existe: `docker images` |
| HPA não escala | Verifique o Metrics Server: `kubectl top nodes` |
| Aplicação não conecta ao DB | Verifique se o PostgreSQL está ready: `kubectl get pods -l app=postgres -n tech-challenge` |
| Service sem EXTERNAL-IP | Use `minikube service` ou `kubectl port-forward` |
| Pod crashando | Veja os logs: `kubectl logs <pod> -n tech-challenge --previous` |

---

## 📞 Suporte

Para mais detalhes, consulte o **[README.md](README.md)** completo.

