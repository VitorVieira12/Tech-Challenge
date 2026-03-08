# 🚀 Tech Challenge - Fase 3 - Setup Completo

## ✅ Status Atual

### Repositórios Criados e Configurados:
1. ✅ [tech-challenge-lambda](https://github.com/VitorVieira12/tech-challenge-lambda) - Autenticação CPF/CNPJ
2. ✅ [tech-challenge-infra-db](https://github.com/VitorVieira12/tech-challenge-infra-db) - Terraform RDS PostgreSQL
3. ✅ [tech-challenge-infra-k8s](https://github.com/VitorVieira12/tech-challenge-infra-k8s) - Terraform EKS + K8s manifests
4. ✅ [Tech-Challenge](https://github.com/VitorVieira12/Tech-Challenge) - Aplicação Spring Boot

### Colaborador:
✅ `soat-architecture` adicionado nos 4 repositórios

---

## 🔐 Configuração de Secrets (PRÓXIMO PASSO)

### Valores já gerados para você:
```
JWT_SECRET = oD67jIFliVX3NvRRHl7/jiQ+XALjyGCTmPt54JX4yz0=
DB_PASSWORD = 6LRxz1SxaCUBubufIzFre1kOS464enP8
DB_USERNAME = postgres
AWS_REGION = us-east-1
DOCKER_USERNAME = VitorVieira12
```

### Valores que você precisa obter:

1. **AWS Credentials**
   - Acesse: https://console.aws.amazon.com/iam/
   - Users → Security credentials → Create access key
   - Copie: `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY`

2. **Docker Hub Token**
   - Acesse: https://hub.docker.com/settings/security
   - New Access Token → Copie o token
   - Será o `DOCKER_PASSWORD`

3. **New Relic License Key**
   - Acesse: https://one.newrelic.com (criar conta grátis)
   - API Keys → Copie License key
   - Será o `NEW_RELIC_LICENSE_KEY`

### Configurar nos repositórios:

**Lambda** (7 secrets): https://github.com/VitorVieira12/tech-challenge-lambda/settings/secrets/actions
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- AWS_REGION = us-east-1
- DB_HOST = pending-rds-deploy (atualizar depois)
- DB_USERNAME = postgres
- DB_PASSWORD = 6LRxz1SxaCUBubufIzFre1kOS464enP8
- JWT_SECRET = oD67jIFliVX3NvRRHl7/jiQ+XALjyGCTmPt54JX4yz0=

**Infra-DB** (5 secrets): https://github.com/VitorVieira12/tech-challenge-infra-db/settings/secrets/actions
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- AWS_REGION = us-east-1
- DB_USERNAME = postgres
- DB_PASSWORD = 6LRxz1SxaCUBubufIzFre1kOS464enP8

**Infra-K8s** (3 secrets agora): https://github.com/VitorVieira12/tech-challenge-infra-k8s/settings/secrets/actions
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- AWS_REGION = us-east-1
- (LAMBDA_FUNCTION_ARN - adicionar após deploy do Lambda)
- (RDS_ENDPOINT - adicionar após deploy do RDS)

**Tech-Challenge** (9 secrets): https://github.com/VitorVieira12/Tech-Challenge/settings/secrets/actions
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- AWS_REGION = us-east-1
- DOCKER_USERNAME = VitorVieira12
- DOCKER_PASSWORD = (token do Docker Hub)
- DB_USERNAME = postgres
- DB_PASSWORD = 6LRxz1SxaCUBubufIzFre1kOS464enP8
- JWT_SECRET = oD67jIFliVX3NvRRHl7/jiQ+XALjyGCTmPt54JX4yz0=
- NEW_RELIC_LICENSE_KEY = (da New Relic)

---

## 🚀 Deploy (DEPOIS DE CONFIGURAR SECRETS)

### 1. Deploy RDS
```bash
cd repos/tech-challenge-infra-db/terraform
terraform init
terraform apply
# Copiar o output: rds_endpoint
```

### 2. Deploy Lambda
```bash
cd repos/tech-challenge-lambda
sam build
sam deploy --guided
# Copiar o output: lambda_function_arn
```

### 3. Atualizar Secrets Dinâmicos
- Atualizar `DB_HOST` no Lambda com o RDS endpoint
- Atualizar `DB_HOST` no Tech-Challenge com o RDS endpoint
- Adicionar `LAMBDA_FUNCTION_ARN` no Infra-K8s
- Adicionar `RDS_ENDPOINT` no Infra-K8s

### 4. Deploy EKS
```bash
cd repos/tech-challenge-infra-k8s/terraform
terraform init
terraform apply

# Configurar kubectl
aws eks update-kubeconfig --region us-east-1 --name tech-challenge-cluster

# Deploy manifests
kubectl apply -f k8s-manifests/
```

### 5. Deploy App (Automático via CI/CD)
Ao fazer push para `main` ou `fase-3`, o GitHub Actions faz deploy automaticamente.

---

## 📊 Estrutura Final

```
Tech-Challenge/
├── .github/workflows/
│   └── deploy-app.yml         # Pipeline CI/CD
├── repos/                      # Repositórios separados
│   ├── tech-challenge-lambda/
│   ├── tech-challenge-infra-db/
│   └── tech-challenge-infra-k8s/
├── src/                        # Código da aplicação
├── Dockerfile                  # Multi-stage + New Relic
├── .dockerignore              # Otimização build
├── pom.xml                    # Dependências
└── README.md                  # Documentação principal
```

---

## ⏱️ Tempo Estimado

- Configurar secrets: 15-20 min
- Deploy RDS: 10-15 min
- Deploy Lambda: 5-10 min
- Deploy EKS: 20-30 min
- Testes: 15-20 min
- Gravar vídeo: 30-40 min

**Total: ~2-3 horas**

---

## 📝 Checklist Final

- [ ] Obter AWS credentials
- [ ] Obter Docker Hub token
- [ ] Obter New Relic license key
- [ ] Configurar secrets nos 4 repos
- [ ] Deploy RDS (terraform apply)
- [ ] Deploy Lambda (sam deploy)
- [ ] Atualizar secrets dinâmicos
- [ ] Deploy EKS (terraform apply)
- [ ] Verificar aplicação funcionando
- [ ] Testar endpoints
- [ ] Verificar New Relic dashboard
- [ ] Testar auto-scaling (HPA)
- [ ] Gravar vídeo demonstrativo
- [ ] Gerar PDF final
- [ ] Entregar no portal

---

**Última atualização**: Repositórios limpos e otimizados
**Status**: Pronto para configurar secrets e fazer deploy 🚀
