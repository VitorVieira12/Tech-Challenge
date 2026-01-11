# 🚀 CI/CD Pipeline - Tech Challenge

[![CI/CD Pipeline](https://github.com/seu-usuario/Tech-Challenge/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/seu-usuario/Tech-Challenge/actions/workflows/ci-cd.yml)

**Pipeline de CI/CD completa com GitHub Actions + AWS EKS**

---

## 📚 Documentação

| Documento | Descrição | Tempo de Leitura |
|-----------|-----------|------------------|
| **[CI_CD_QUICKSTART.md](CI_CD_QUICKSTART.md)** | 🚀 Guia Rápido de 10 minutos | ⚡ 10 min |
| **[CI_CD_SETUP_GUIDE.md](CI_CD_SETUP_GUIDE.md)** | 📖 Guia Completo Detalhado | 📚 30 min |
| **[INFRAESTRUTURA_COMPLETA.md](INFRAESTRUTURA_COMPLETA.md)** | 🏗️ Documentação de Infraestrutura | 📚 20 min |

---

## ⚡ Quick Start (10 minutos)

### 1. Configurar AWS
```bash
# Criar usuário IAM com permissões
# Configurar AWS CLI
aws configure

# Rodar script helper
./scripts/setup-cicd.sh
```

### 2. Configurar GitHub Secrets
```
Settings > Secrets and variables > Actions

Adicionar 8 secrets:
✅ AWS_ACCESS_KEY_ID
✅ AWS_SECRET_ACCESS_KEY
✅ AWS_ACCOUNT_ID
✅ DB_USERNAME
✅ DB_PASSWORD
✅ JWT_SECRET
✅ ADMIN_USERNAME
✅ ADMIN_PASSWORD
```

### 3. Provisionar Infraestrutura
```bash
cd infra/aws
terraform init
terraform apply
```

### 4. Deploy
```bash
git push origin main
# Pipeline roda automaticamente!
```

### 5. Verificar
```bash
./scripts/check-deployment.sh
```

---

## 🔄 Pipeline Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     GIT PUSH TO MAIN                        │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  BUILD & TEST                  │ ⏱️ ~5 min
         │  • Maven build                 │
         │  • Unit tests                  │
         │  • Integration tests           │
         │  • Coverage report             │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  CODE ANALYSIS                 │ ⏱️ ~2 min
         │  • SonarCloud (opcional)       │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  DOCKER BUILD & PUSH           │ ⏱️ ~3 min
         │  • Build image                 │
         │  • Security scan               │
         │  • Push to ECR                 │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  TERRAFORM DEPLOY              │ ⏱️ ~15 min*
         │  • Provision VPC               │
         │  • Create EKS cluster          │
         │  • Setup RDS PostgreSQL        │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  KUBERNETES DEPLOY             │ ⏱️ ~3 min
         │  • Deploy database             │
         │  • Deploy application          │
         │  • Configure HPA               │
         │  • Health checks               │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  POST DEPLOYMENT               │ ⏱️ ~1 min
         │  • Smoke tests                 │
         │  • Notifications               │
         └────────────────────────────────┘

*Terraform only runs on first deployment or changes
```

---

## 📁 Arquivos da Pipeline

### Workflows GitHub Actions

```
.github/workflows/
├── ci-cd.yml              # 🚀 Pipeline principal (CI/CD completo)
├── pr-validation.yml      # ✅ Validação automática de PRs
└── rollback.yml           # ⏮️ Rollback manual
```

### Scripts Auxiliares

```
scripts/
├── setup-cicd.sh          # 🔧 Helper para setup AWS
└── check-deployment.sh    # 📊 Verificador de deployment
```

---

## 🎯 Funcionalidades

### ✅ Build e Testes
- ✅ Build Maven com cache
- ✅ Testes unitários
- ✅ Testes de integração
- ✅ Relatórios de cobertura
- ✅ Upload de artifacts

### ✅ Qualidade de Código
- ✅ SonarCloud integration (opcional)
- ✅ Análise estática
- ✅ Code coverage tracking

### ✅ Container e Registry
- ✅ Build multi-stage Docker
- ✅ Security scan (Trivy)
- ✅ Push para Amazon ECR
- ✅ Tags inteligentes (sha, latest, env)

### ✅ Infraestrutura
- ✅ Terraform automation
- ✅ VPC com subnets multi-AZ
- ✅ EKS cluster gerenciado
- ✅ RDS PostgreSQL
- ✅ Auto-scaling

### ✅ Deploy Kubernetes
- ✅ Zero-downtime deployment
- ✅ Rolling updates
- ✅ Health checks
- ✅ HPA (auto-scaling)
- ✅ Secrets management

### ✅ Validação e Rollback
- ✅ Smoke tests
- ✅ Automated rollback on failure
- ✅ Manual rollback workflow
- ✅ Revision history

---

## 🔐 Secrets Requeridos

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `AWS_ACCESS_KEY_ID` | AWS Access Key | `AKIAIOSFODNN7...` |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key | `wJalrXUtnFEMI...` |
| `AWS_ACCOUNT_ID` | AWS Account ID | `123456789012` |
| `DB_USERNAME` | Database username | `tech_admin` |
| `DB_PASSWORD` | Database password | `SecurePass123!` |
| `JWT_SECRET` | JWT secret key | `404E635266556A...` |
| `ADMIN_USERNAME` | Admin username | `admin` |
| `ADMIN_PASSWORD` | Admin password | `AdminPass123!` |

### Secrets Opcionais

| Secret | Descrição |
|--------|-----------|
| `CODECOV_TOKEN` | Token para Codecov.io |
| `SONAR_TOKEN` | Token para SonarCloud |

---

## 🌿 Branch Strategy

### Branch: `main`
- ✅ Executa pipeline completa
- ✅ Deploy para PRODUÇÃO
- ✅ Aplica Terraform
- ✅ Requer aprovação (recomendado)
- 🏷️ Tag Docker: `production`, `latest`, `sha`

### Branch: `develop`
- ✅ Executa build, test, docker
- ✅ Deploy para STAGING
- ❌ NÃO aplica Terraform
- 🏷️ Tag Docker: `staging`, `sha`

### Pull Requests
- ✅ Executa build e testes
- ✅ Gera relatório de cobertura
- ✅ Comenta no PR
- ❌ NÃO faz deploy

---

## 📊 Monitoramento

### GitHub Actions
```
https://github.com/seu-usuario/Tech-Challenge/actions
```

### Kubernetes Dashboard
```bash
# Ver pods
kubectl get pods -n tech-challenge

# Ver logs
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Ver HPA
kubectl get hpa -n tech-challenge -w
```

### AWS Console
```
- ECR: https://console.aws.amazon.com/ecr/
- EKS: https://console.aws.amazon.com/eks/
- RDS: https://console.aws.amazon.com/rds/
- CloudWatch: https://console.aws.amazon.com/cloudwatch/
```

---

## 🔄 Workflows Comuns

### Deploy Normal
```bash
git checkout main
git pull origin main
# fazer mudanças
git add .
git commit -m "feat: Nova funcionalidade"
git push origin main
# Pipeline roda automaticamente
```

### Deploy com Feature Branch
```bash
git checkout -b feature/nova-feature
# fazer mudanças
git add .
git commit -m "feat: Nova feature"
git push origin feature/nova-feature
# Criar PR no GitHub
# Pipeline valida PR
# Após merge, deploy automático
```

### Rollback
```bash
# Opção 1: Via GitHub Actions UI
# Actions > Rollback Deployment > Run workflow

# Opção 2: Via kubectl
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge

# Opção 3: Para revisão específica
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge --to-revision=3
```

### Ver histórico de deploys
```bash
kubectl rollout history deployment/tech-challenge-app -n tech-challenge
```

---

## 🐛 Troubleshooting

### Pipeline falha no build
```bash
# Rodar testes localmente
mvn clean verify

# Ver logs detalhados no GitHub Actions
```

### Pipeline falha no Terraform
```bash
# Verificar credenciais
aws sts get-caller-identity

# Verificar state
cd infra/aws
terraform state list

# Forçar unlock se necessário
terraform force-unlock <LOCK_ID>
```

### Pipeline falha no deploy K8s
```bash
# Verificar cluster
kubectl cluster-info

# Ver eventos
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# Ver logs dos pods
kubectl logs -l app=tech-challenge -n tech-challenge
```

### Pods não ficam prontos
```bash
# Descrever pod
kubectl describe pod <pod-name> -n tech-challenge

# Ver logs
kubectl logs <pod-name> -n tech-challenge

# Verificar secrets
kubectl get secret app-secrets -n tech-challenge -o yaml
```

---

## 📈 Métricas e KPIs

### Tempo de Build
- Target: < 10 minutos
- Atual: ~8 minutos (sem Terraform)

### Frequência de Deploy
- Produção: On-demand (push to main)
- Staging: Automático (push to develop)

### Taxa de Sucesso
- Target: > 95%
- Monitorar via GitHub Actions

### MTTR (Mean Time to Recovery)
- Rollback manual: < 2 minutos
- Rollback automático: < 5 minutos

---

## 🎯 Próximas Melhorias

- [ ] Adicionar testes de performance
- [ ] Implementar Canary deployments
- [ ] Adicionar notificações Slack/Discord
- [ ] Implementar Blue/Green deployment
- [ ] Adicionar Prometheus + Grafana
- [ ] Implementar GitOps com ArgoCD
- [ ] Adicionar testes de segurança SAST/DAST

---

## 📚 Recursos Adicionais

- **[GitHub Actions Docs](https://docs.github.com/en/actions)**
- **[AWS EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)**
- **[Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)**
- **[Kubernetes Documentation](https://kubernetes.io/docs/)**

---

## 💡 Dicas

1. **Sempre teste em staging antes de produção**
2. **Mantenha secrets atualizados**
3. **Monitore custos da AWS**
4. **Faça rollback se algo der errado**
5. **Documente mudanças importantes**
6. **Use conventional commits**
7. **Revise PRs antes de merge**

---

## ✅ Status do Projeto

| Componente | Status | Versão |
|------------|--------|--------|
| Pipeline CI/CD | ✅ Funcionando | v1.0 |
| GitHub Actions | ✅ Configurado | Latest |
| AWS ECR | ✅ Ativo | - |
| AWS EKS | ✅ Running | 1.28 |
| AWS RDS | ✅ Running | PostgreSQL 15 |
| Terraform | ✅ Aplicado | 1.6.0 |
| Kubernetes | ✅ Deployado | 1.28 |

---

## 📞 Suporte

Para problemas ou dúvidas:

1. Consulte [CI_CD_SETUP_GUIDE.md](CI_CD_SETUP_GUIDE.md) - Troubleshooting
2. Verifique GitHub Actions logs
3. Execute `./scripts/check-deployment.sh`
4. Abra uma issue no repositório

---

**Criado para Tech Challenge - 2026**  
**Pipeline by GitHub Actions + AWS**

🚀 **Happy Deploying!**

