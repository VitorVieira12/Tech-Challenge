# 🚀 CI/CD Quick Start - Tech Challenge

**Guia Rápido de 10 Minutos**

Se você já tem AWS e GitHub configurados e quer começar rápido, siga este guia.

---

## ⚡ Pré-requisitos Rápidos

```bash
# Instale se ainda não tiver:
- AWS CLI
- kubectl
- terraform
```

---

## 🔧 Passo 1: Configurar AWS (5 minutos)

### 1.1 Criar usuário IAM

```bash
# No console AWS (https://console.aws.amazon.com/iam/):
1. IAM > Users > Create user
2. Nome: github-actions-user
3. Permissions: Attach policies directly
   - AmazonEC2ContainerRegistryFullAccess
   - AmazonEKSClusterPolicy
   - AmazonEKSWorkerNodePolicy
   - AmazonRDSFullAccess
   - AmazonVPCFullAccess
   - IAMFullAccess
4. Create access key > CLI
5. COPIE E GUARDE as credenciais!
```

### 1.2 Configurar AWS CLI

```bash
aws configure
# Informe as credenciais copiadas acima
# Region: us-east-1
# Output: json
```

### 1.3 Criar ECR Repository

```bash
# Rode o script helper:
chmod +x scripts/setup-cicd.sh
./scripts/setup-cicd.sh

# Ou manual:
aws ecr create-repository --repository-name tech-challenge --region us-east-1
```

---

## 🔐 Passo 2: Configurar GitHub Secrets (3 minutos)

Vá para: `Settings > Secrets and variables > Actions > New repository secret`

Adicione (copie e cole):

```yaml
AWS_ACCESS_KEY_ID: <sua-access-key>
AWS_SECRET_ACCESS_KEY: <sua-secret-key>
AWS_ACCOUNT_ID: <seu-account-id>  # Ex: 123456789012
DB_USERNAME: tech_admin
DB_PASSWORD: TechChallenge2024!Secure
JWT_SECRET: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ADMIN_USERNAME: admin
ADMIN_PASSWORD: Admin2024!Secure
```

---

## 🏗️ Passo 3: Provisionar Infraestrutura (15 minutos)

```bash
# 1. Navegar para terraform
cd infra/aws

# 2. Copiar variáveis
cp terraform.tfvars.example terraform.tfvars

# 3. Inicializar
terraform init

# 4. Aplicar (digite 'yes' quando solicitado)
export TF_VAR_db_password="TechChallenge2024!Secure"
terraform apply

# 5. Configurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 6. Verificar
kubectl get nodes  # Deve mostrar 2 nodes
```

---

## 🚀 Passo 4: Testar Pipeline (2 minutos)

```bash
# 1. Fazer uma mudança
echo "# CI/CD test" >> README.md

# 2. Commit e push
git add .
git commit -m "test: Testing CI/CD pipeline"
git push origin main

# 3. Acompanhar no GitHub
# https://github.com/seu-usuario/Tech-Challenge/actions
```

---

## ✅ Passo 5: Verificar Deployment

```bash
# Usar script helper
chmod +x scripts/check-deployment.sh
./scripts/check-deployment.sh

# Ou manual:
kubectl get all -n tech-challenge

# Obter URL
kubectl get svc tech-challenge-service -n tech-challenge

# Testar
LOADBALANCER_URL=$(kubectl get svc tech-challenge-service -n tech-challenge -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
curl http://$LOADBALANCER_URL/actuator/health
```

---

## 📊 O que a Pipeline Faz

```
1. Build Maven         (~3 min)
2. Testes             (~2 min)
3. Docker Build       (~2 min)
4. Push para ECR      (~1 min)
5. Terraform Apply    (~15 min - só na primeira vez)
6. Deploy K8s         (~3 min)
7. Verificações       (~1 min)
-----------------------------------
Total: ~27 min (primeira vez)
       ~12 min (deploys seguintes)
```

---

## 🎯 Arquivos Criados

```
.github/workflows/
├── ci-cd.yml            # Pipeline principal
├── pr-validation.yml    # Validação de PRs
└── rollback.yml         # Rollback manual

scripts/
├── setup-cicd.sh        # Helper de setup
└── check-deployment.sh  # Verificador de deployment

CI_CD_SETUP_GUIDE.md     # Guia completo detalhado
CI_CD_QUICKSTART.md      # Este arquivo (guia rápido)
```

---

## 🔄 Workflow Normal

### Desenvolvimento Diário:

```bash
# 1. Criar branch
git checkout -b feature/nova-funcionalidade

# 2. Desenvolver e commitar
git add .
git commit -m "feat: Nova funcionalidade"

# 3. Push e criar PR
git push origin feature/nova-funcionalidade
# Criar PR no GitHub

# 4. Pipeline valida automaticamente o PR
# (build + testes)

# 5. Após aprovação, merge para main
# Pipeline completa roda automaticamente:
# - Build
# - Testes
# - Docker
# - Deploy
```

### Rollback se necessário:

```bash
# Via GitHub Actions UI:
1. Actions > Rollback Deployment > Run workflow
2. Escolha environment: production
3. Run workflow

# Ou via kubectl:
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge
```

---

## 📚 Links Úteis

- **Guia Completo:** [CI_CD_SETUP_GUIDE.md](CI_CD_SETUP_GUIDE.md)
- **Infraestrutura:** [INFRAESTRUTURA_COMPLETA.md](INFRAESTRUTURA_COMPLETA.md)
- **Kubernetes:** [k8s/README.md](k8s/README.md)
- **Terraform AWS:** [infra/aws/README.md](infra/aws/README.md)

---

## 🐛 Problemas Comuns

### "kubectl: connection refused"
```bash
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1
```

### "ImagePullBackOff"
```bash
# Verificar imagem no ECR
aws ecr describe-images --repository-name tech-challenge
```

### "Terraform state locked"
```bash
terraform force-unlock <LOCK_ID>
```

### "Pipeline fails on terraform"
```bash
# Verificar secrets do GitHub
# Principalmente: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
```

---

## ✅ Checklist

- [ ] AWS CLI instalado e configurado
- [ ] kubectl instalado
- [ ] Terraform instalado
- [ ] Usuário IAM criado com permissões
- [ ] ECR repository criado
- [ ] Secrets configurados no GitHub (8 secrets)
- [ ] Infraestrutura provisionada (terraform apply)
- [ ] Cluster acessível (kubectl get nodes)
- [ ] Pipeline executada (push para main)
- [ ] Aplicação rodando (kubectl get pods)
- [ ] LoadBalancer acessível
- [ ] Health check OK

---

## 🎉 Pronto!

Se você completou todos os passos, sua pipeline CI/CD está funcionando!

**Próximo deploy:** Apenas faça `git push origin main` 🚀

Para mais detalhes, veja: [CI_CD_SETUP_GUIDE.md](CI_CD_SETUP_GUIDE.md)

