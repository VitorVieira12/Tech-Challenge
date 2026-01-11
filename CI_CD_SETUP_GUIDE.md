# 🚀 GUIA COMPLETO: CI/CD com GitHub Actions e AWS

**Guia Passo a Passo Detalhado - Atualizado 2026**

Este guia vai te levar do zero até ter um pipeline CI/CD completo funcionando com GitHub Actions, fazendo deploy automático na AWS (EKS + RDS).

---

## 📋 Índice

1. [Pré-requisitos](#1-pré-requisitos)
2. [Configuração da AWS](#2-configuração-da-aws)
3. [Configuração do GitHub](#3-configuração-do-github)
4. [Estrutura de Arquivos Criada](#4-estrutura-de-arquivos-criada)
5. [Como Funciona a Pipeline](#5-como-funciona-a-pipeline)
6. [Primeiro Deploy](#6-primeiro-deploy)
7. [Monitoramento e Logs](#7-monitoramento-e-logs)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Pré-requisitos

### ✅ O que você precisa ter:

- ✅ Conta no GitHub (você já tem)
- ✅ Conta na AWS (você já tem)
- ✅ AWS CLI instalado no seu computador
- ✅ Git instalado
- ✅ Projeto no GitHub

### 📥 Instalar AWS CLI (se ainda não tiver)

**Windows:**
```powershell
# Usando MSI Installer
# Baixe de: https://awscli.amazonaws.com/AWSCLIV2.msi
# Execute o instalador

# Ou usando Chocolatey:
choco install awscli

# Verificar instalação:
aws --version
```

**Linux:**
```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Verificar:
aws --version
```

**macOS:**
```bash
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /

# Verificar:
aws --version
```

---

## 2. Configuração da AWS

### Passo 2.1: Criar Usuário IAM para CI/CD

1. **Acesse o Console AWS:**
   - Vá para: https://console.aws.amazon.com/
   - Faça login com sua conta

2. **Navegue até IAM:**
   - No menu de serviços, procure por "IAM"
   - Ou acesse direto: https://console.aws.amazon.com/iam/

3. **Criar novo usuário:**
   ```
   - Clique em "Users" (Usuários) no menu lateral
   - Clique em "Create user" (Criar usuário)
   - Nome do usuário: github-actions-user
   - ✅ Marque: "Provide user access to AWS Management Console" (NÃO)
   - Clique em "Next"
   ```

4. **Adicionar permissões:**
   ```
   - Selecione: "Attach policies directly"
   - Procure e adicione as seguintes policies:
     ✅ AmazonEC2ContainerRegistryFullAccess
     ✅ AmazonEKSClusterPolicy
     ✅ AmazonEKSWorkerNodePolicy
     ✅ AmazonEKS_CNI_Policy
     ✅ AmazonRDSFullAccess
     ✅ AmazonVPCFullAccess
     ✅ IAMFullAccess
   
   - Clique em "Next"
   - Clique em "Create user"
   ```

5. **Criar Access Keys:**
   ```
   - Clique no usuário recém-criado
   - Vá na aba "Security credentials"
   - Role até "Access keys"
   - Clique em "Create access key"
   - Selecione: "Command Line Interface (CLI)"
   - Marque a checkbox de confirmação
   - Clique em "Next"
   - (Opcional) Adicione uma descrição: "GitHub Actions CI/CD"
   - Clique em "Create access key"
   ```

6. **🔴 IMPORTANTE - Salve as credenciais:**
   ```
   ⚠️ COPIE E GUARDE AGORA (não aparecerão novamente):
   
   Access key ID: AKIAIOSFODNN7EXAMPLE
   Secret access key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
   
   Guarde em local seguro! Vamos usar no próximo passo.
   ```

### Passo 2.2: Criar ECR Repository

1. **Acesse o ECR:**
   - Procure por "ECR" nos serviços
   - Ou acesse: https://console.aws.amazon.com/ecr/

2. **Criar Repository:**
   ```
   - Clique em "Create repository"
   - Visibilidade: Private
   - Nome do repositório: tech-challenge
   - Deixe as outras configurações padrão
   - Clique em "Create repository"
   ```

3. **Anote o URI do repositório:**
   ```
   Exemplo: 123456789012.dkr.ecr.us-east-1.amazonaws.com/tech-challenge
   
   Copie o número da conta (123456789012) - vamos precisar!
   ```

### Passo 2.3: Configurar AWS CLI localmente

```bash
# Configure suas credenciais AWS
aws configure

# Quando solicitado, informe:
AWS Access Key ID: [Cole a Access Key ID do passo 2.1]
AWS Secret Access Key: [Cole a Secret Access Key do passo 2.1]
Default region name: us-east-1
Default output format: json

# Testar configuração:
aws sts get-caller-identity

# Deve retornar algo como:
# {
#     "UserId": "AIDAXXXXXXXXXXXXXXXXX",
#     "Account": "123456789012",
#     "Arn": "arn:aws:iam::123456789012:user/github-actions-user"
# }
```

---

## 3. Configuração do GitHub

### Passo 3.1: Adicionar Secrets no GitHub

1. **Acesse seu repositório no GitHub:**
   ```
   https://github.com/seu-usuario/Tech-Challenge
   ```

2. **Vá para Settings > Secrets and variables > Actions:**
   ```
   - Clique na aba "Settings"
   - No menu lateral, clique em "Secrets and variables"
   - Clique em "Actions"
   - Clique no botão "New repository secret"
   ```

3. **Adicione os seguintes secrets (um por vez):**

   **Secret 1: AWS_ACCESS_KEY_ID**
   ```
   Name: AWS_ACCESS_KEY_ID
   Secret: [Cole a Access Key ID da AWS]
   ```

   **Secret 2: AWS_SECRET_ACCESS_KEY**
   ```
   Name: AWS_SECRET_ACCESS_KEY
   Secret: [Cole a Secret Access Key da AWS]
   ```

   **Secret 3: AWS_ACCOUNT_ID**
   ```
   Name: AWS_ACCOUNT_ID
   Secret: [Seu número de conta AWS, ex: 123456789012]
   ```

   **Secret 4: DB_USERNAME**
   ```
   Name: DB_USERNAME
   Secret: tech_admin
   ```

   **Secret 5: DB_PASSWORD**
   ```
   Name: DB_PASSWORD
   Secret: [Crie uma senha forte, ex: TechChallenge2024!Secure]
   ```

   **Secret 6: JWT_SECRET**
   ```
   Name: JWT_SECRET
   Secret: [Gere uma chave aleatória, ex: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970]
   ```

   **Secret 7: ADMIN_USERNAME**
   ```
   Name: ADMIN_USERNAME
   Secret: admin
   ```

   **Secret 8: ADMIN_PASSWORD**
   ```
   Name: ADMIN_PASSWORD
   Secret: [Senha forte para admin, ex: Admin2024!Secure]
   ```

   **Secret 9: CODECOV_TOKEN (Opcional)**
   ```
   Name: CODECOV_TOKEN
   Secret: [Token do codecov.io se quiser cobertura de código]
   ```

   **Secret 10: SONAR_TOKEN (Opcional)**
   ```
   Name: SONAR_TOKEN
   Secret: [Token do SonarCloud se quiser análise de código]
   ```

### Passo 3.2: Verificar Secrets

```
Após adicionar todos, você deve ver na lista:
✅ AWS_ACCESS_KEY_ID
✅ AWS_SECRET_ACCESS_KEY  
✅ AWS_ACCOUNT_ID
✅ DB_USERNAME
✅ DB_PASSWORD
✅ JWT_SECRET
✅ ADMIN_USERNAME
✅ ADMIN_PASSWORD
```

---

## 4. Estrutura de Arquivos Criada

Os seguintes arquivos foram criados no seu projeto:

```
.github/
└── workflows/
    ├── ci-cd.yml           # Pipeline principal de CI/CD
    ├── pr-validation.yml   # Validação de Pull Requests
    └── rollback.yml        # Pipeline de rollback
```

### 📄 Descrição dos arquivos:

**`ci-cd.yml`** - Pipeline principal com 6 jobs:
1. **build-and-test**: Compila e testa a aplicação
2. **code-analysis**: Análise de qualidade de código (opcional)
3. **docker-build-push**: Cria imagem Docker e envia para ECR
4. **terraform-deploy**: Provisiona infraestrutura com Terraform
5. **kubernetes-deploy**: Faz deploy no cluster EKS
6. **post-deployment**: Notificações e validações finais

**`pr-validation.yml`** - Valida Pull Requests automaticamente:
- Executa build
- Roda todos os testes
- Gera relatório de cobertura
- Comenta no PR com resultados

**`rollback.yml`** - Permite fazer rollback manual:
- Acesso via GitHub Actions interface
- Volta para versão anterior
- Pode especificar revisão específica

---

## 5. Como Funciona a Pipeline

### 🔄 Fluxo Completo

```
┌─────────────────────────────────────────────────────────────────┐
│  1. PUSH para main/develop  ou  PULL REQUEST                    │
└────────────────────────┬────────────────────────────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 1: Build & Test           │
         │  - Compila com Maven           │
         │  - Executa testes unitários    │
         │  - Executa testes integração   │
         │  - Gera relatório coverage     │
         │  - Salva JAR como artifact     │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 2: Code Analysis          │
         │  - SonarCloud scan (opcional)  │
         │  - Análise de qualidade        │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 3: Docker Build & Push    │
         │  - Faz download do JAR         │
         │  - Constrói imagem Docker      │
         │  - Faz scan de segurança       │
         │  - Push para Amazon ECR        │
         │  - Tags: sha, latest, env      │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 4: Terraform Deploy       │
         │  - Terraform init              │
         │  - Terraform plan              │
         │  - Terraform apply             │
         │  - Cria: VPC, EKS, RDS         │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 5: Kubernetes Deploy      │
         │  - Configura kubectl           │
         │  - Cria namespace              │
         │  - Aplica secrets              │
         │  - Deploy PostgreSQL           │
         │  - Deploy aplicação            │
         │  - Aplica HPA                  │
         │  - Aguarda pods prontos        │
         │  - Obtém LoadBalancer URL      │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  JOB 6: Post Deployment        │
         │  - Verifica status             │
         │  - Envia notificações          │
         │  - Smoke tests                 │
         └────────────────────────────────┘
```

### ⚙️ Triggers da Pipeline

**Push para main:**
- ✅ Executa TODOS os jobs
- ✅ Faz deploy em PRODUÇÃO
- ✅ Aplica Terraform
- ✅ Tag da imagem: `production`

**Push para develop:**
- ✅ Executa jobs de build, test e docker
- ✅ Faz deploy em STAGING
- ✅ NÃO aplica Terraform
- ✅ Tag da imagem: `staging`

**Pull Request:**
- ✅ Executa build e testes
- ❌ NÃO faz deploy
- ❌ NÃO cria imagem Docker

---

## 6. Primeiro Deploy

### Passo 6.1: Provisionar Infraestrutura Manualmente (Primeira Vez)

Antes do primeiro deploy via CI/CD, você precisa criar a infraestrutura:

```bash
# 1. Clone o repositório (se ainda não tiver)
git clone https://github.com/seu-usuario/Tech-Challenge.git
cd Tech-Challenge

# 2. Configure variáveis do Terraform
cd infra/aws
cp terraform.tfvars.example terraform.tfvars

# 3. Edite terraform.tfvars com suas configurações
# (use seu editor preferido)
nano terraform.tfvars

# 4. Defina a senha do banco
export TF_VAR_db_password="TechChallenge2024!Secure"

# 5. Inicialize o Terraform
terraform init

# 6. Planeje as mudanças
terraform plan

# 7. Aplique (vai levar 15-20 minutos)
terraform apply

# 8. Quando perguntar, digite: yes

# 9. Configure kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 10. Verifique conexão
kubectl get nodes

# Deve mostrar 2 nodes em estado Ready
```

### Passo 6.2: Testar a Pipeline

```bash
# 1. Volte para a raiz do projeto
cd ../..

# 2. Crie uma branch para teste
git checkout -b test-pipeline

# 3. Faça uma pequena mudança (ex: adicione um comentário)
echo "# Pipeline test" >> README.md

# 4. Commit e push
git add .
git commit -m "test: Testando pipeline CI/CD"
git push origin test-pipeline

# 5. Crie um Pull Request no GitHub
# Acesse: https://github.com/seu-usuario/Tech-Challenge/pulls
# Clique em "New pull request"
# Base: main <- Compare: test-pipeline
# Clique em "Create pull request"
```

### Passo 6.3: Acompanhar a Execução

1. **No GitHub:**
   ```
   - Vá para a aba "Actions"
   - Você verá a pipeline "PR Validation" rodando
   - Clique nela para ver os logs em tempo real
   ```

2. **Aguarde completar:**
   ```
   ✅ Build & Test - ~3-5 minutos
   ✅ Code Analysis - ~2 minutos
   ```

3. **Após aprovação do PR, faça merge:**
   ```
   - Volte para o Pull Request
   - Clique em "Merge pull request"
   - Clique em "Confirm merge"
   ```

4. **Pipeline completa será executada:**
   ```
   - Vá novamente para "Actions"
   - Veja a pipeline "CI/CD Pipeline" rodando
   - Tempo total: ~20-30 minutos
   ```

### Passo 6.4: Verificar Deploy

```bash
# 1. Listar pods
kubectl get pods -n tech-challenge

# Deve mostrar:
# NAME                                    READY   STATUS    RESTARTS   AGE
# postgres-xxxxxxxxx-xxxxx                1/1     Running   0          5m
# tech-challenge-app-xxxxxxxxx-xxxxx      1/1     Running   0          3m
# tech-challenge-app-xxxxxxxxx-xxxxx      1/1     Running   0          3m

# 2. Verificar services
kubectl get svc -n tech-challenge

# Anote o EXTERNAL-IP do tech-challenge-service

# 3. Obter URL do LoadBalancer
LOADBALANCER_URL=$(kubectl get svc tech-challenge-service -n tech-challenge -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "URL da Aplicação: http://$LOADBALANCER_URL"

# 4. Testar a aplicação
curl http://$LOADBALANCER_URL/actuator/health

# Deve retornar:
# {"status":"UP"}

# 5. Acessar Swagger UI
echo "Swagger UI: http://$LOADBALANCER_URL/swagger-ui.html"
```

---

## 7. Monitoramento e Logs

### 📊 No GitHub Actions

**Ver execuções:**
```
1. Vá para: https://github.com/seu-usuario/Tech-Challenge/actions
2. Clique em uma execução
3. Clique em um job específico
4. Veja os logs detalhados de cada step
```

**Download de artifacts:**
```
1. Na página da execução
2. Role até "Artifacts"
3. Baixe: application-jar, terraform-outputs, etc.
```

### 📈 No Kubernetes

```bash
# Logs da aplicação
kubectl logs -f deployment/tech-challenge-app -n tech-challenge

# Logs do PostgreSQL
kubectl logs -f deployment/postgres -n tech-challenge

# Eventos do namespace
kubectl get events -n tech-challenge --sort-by='.lastTimestamp'

# Status detalhado dos pods
kubectl describe pod <pod-name> -n tech-challenge

# Métricas do HPA
kubectl get hpa -n tech-challenge -w
```

### 🔍 No AWS

**CloudWatch Logs:**
```
1. Console AWS > CloudWatch > Log groups
2. Procure por: /aws/eks/tech-challenge-eks/cluster
3. Veja logs do cluster
```

**ECR Images:**
```
1. Console AWS > ECR
2. Repository: tech-challenge
3. Veja todas as imagens buildadas
```

**EKS Cluster:**
```
1. Console AWS > EKS
2. Cluster: tech-challenge-eks
3. Veja nodes, workloads, etc.
```

---

## 8. Troubleshooting

### ❌ Erro: "Unable to connect to the server"

**Problema:** kubectl não consegue conectar ao cluster

**Solução:**
```bash
# Reconfigurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# Verificar credenciais AWS
aws sts get-caller-identity
```

### ❌ Erro: "ImagePullBackOff"

**Problema:** Kubernetes não consegue baixar a imagem do ECR

**Solução:**
```bash
# 1. Verificar se a imagem existe no ECR
aws ecr describe-images --repository-name tech-challenge --region us-east-1

# 2. Verificar IAM role do node
kubectl describe node | grep iam

# 3. Atualizar deployment com imagem correta
kubectl set image deployment/tech-challenge-app \
  app=<account-id>.dkr.ecr.us-east-1.amazonaws.com/tech-challenge:latest \
  -n tech-challenge
```

### ❌ Erro: "Terraform apply failed"

**Problema:** Terraform não consegue criar recursos

**Solução:**
```bash
# 1. Verificar logs detalhados
terraform plan -detailed-exitcode

# 2. Verificar permissões IAM
aws iam get-user

# 3. Verificar state lock
terraform force-unlock <lock-id>

# 4. Limpar e reiniciar
rm -rf .terraform
terraform init
terraform plan
```

### ❌ Erro: "Pods não ficam Ready"

**Problema:** Pods ficam em CrashLoopBackOff

**Solução:**
```bash
# 1. Ver logs do pod
kubectl logs <pod-name> -n tech-challenge

# 2. Descrever pod
kubectl describe pod <pod-name> -n tech-challenge

# 3. Verificar secrets
kubectl get secret app-secrets -n tech-challenge -o yaml

# 4. Verificar se PostgreSQL está rodando
kubectl get pod -l app=postgres -n tech-challenge

# 5. Testar conectividade
kubectl exec -it <app-pod-name> -n tech-challenge -- nc -zv postgres-service 5432
```

### ❌ Erro: "Pipeline fails on tests"

**Problema:** Testes falham na pipeline

**Solução:**
```bash
# 1. Rodar testes localmente
mvn clean test

# 2. Ver relatório de testes
mvn surefire-report:report
open target/site/surefire-report.html

# 3. Verificar dependências
mvn dependency:tree

# 4. Limpar cache Maven
mvn clean install -U
```

### ❌ Erro: "Access Denied" na AWS

**Problema:** GitHub Actions não tem permissão

**Solução:**
```
1. Verificar secrets do GitHub:
   - AWS_ACCESS_KEY_ID
   - AWS_SECRET_ACCESS_KEY

2. Verificar políticas IAM do usuário github-actions-user

3. Testar credenciais localmente:
   aws sts get-caller-identity
```

### ❌ Erro: "LoadBalancer não provisiona"

**Problema:** Service fica sem EXTERNAL-IP

**Solução:**
```bash
# 1. Verificar service
kubectl describe svc tech-challenge-service -n tech-challenge

# 2. Verificar security groups
aws ec2 describe-security-groups --filters "Name=tag:Name,Values=*eks*"

# 3. Verificar AWS Load Balancer Controller
kubectl get deployment -n kube-system aws-load-balancer-controller

# 4. Mudar para NodePort temporariamente
kubectl patch svc tech-challenge-service -n tech-challenge -p '{"spec":{"type":"NodePort"}}'
```

---

## 9. Comandos Úteis

### 🔄 Fazer novo deploy manual

```bash
# Triggerar pipeline manualmente
git commit --allow-empty -m "trigger: Manual deploy"
git push origin main
```

### 📦 Ver histórico de deploys

```bash
# Ver revisões do deployment
kubectl rollout history deployment/tech-challenge-app -n tech-challenge

# Ver detalhes de uma revisão específica
kubectl rollout history deployment/tech-challenge-app -n tech-challenge --revision=3
```

### ⏮️ Rollback manual

```bash
# Voltar para versão anterior
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge

# Voltar para revisão específica
kubectl rollout undo deployment/tech-challenge-app -n tech-challenge --to-revision=2

# Ver status do rollout
kubectl rollout status deployment/tech-challenge-app -n tech-challenge
```

### 🧹 Limpar recursos

```bash
# Deletar aplicação
kubectl delete namespace tech-challenge

# Destruir infraestrutura
cd infra/aws
terraform destroy
```

### 📊 Monitorar em tempo real

```bash
# Watch pods
kubectl get pods -n tech-challenge -w

# Watch HPA
kubectl get hpa -n tech-challenge -w

# Stream logs
kubectl logs -f -l app=tech-challenge -n tech-challenge --all-containers=true
```

---

## 10. Próximos Passos

### 🎯 Melhorias Recomendadas

1. **Adicionar Slack/Discord notifications:**
   ```yaml
   # Adicione no final do job post-deployment
   - name: Notify Slack
     uses: 8398a7/action-slack@v3
     with:
       status: ${{ job.status }}
       webhook_url: ${{ secrets.SLACK_WEBHOOK }}
   ```

2. **Configurar ambientes múltiplos:**
   ```yaml
   # Adicione environments no GitHub
   Settings > Environments
   - production (requires approval)
   - staging (auto-deploy)
   - development (auto-deploy)
   ```

3. **Adicionar cache para Docker layers:**
   ```yaml
   - name: Cache Docker layers
     uses: actions/cache@v4
     with:
       path: /tmp/.buildx-cache
       key: ${{ runner.os }}-buildx-${{ github.sha }}
   ```

4. **Implementar Blue/Green deployment:**
   ```bash
   # Criar deployment alternativo
   kubectl apply -f k8s/app-deployment-blue.yaml
   kubectl apply -f k8s/app-deployment-green.yaml
   ```

5. **Adicionar testes de carga:**
   ```yaml
   - name: Load Testing
     run: |
       kubectl run -it --rm load-test --image=williamyeh/hey:latest \
         --restart=Never -- -z 30s -c 10 http://tech-challenge-service/api/clientes
   ```

---

## 11. Referências

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [AWS EKS User Guide](https://docs.aws.amazon.com/eks/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

## ✅ Checklist Final

Antes de considerar o setup completo, verifique:

- [ ] ✅ Usuário IAM criado com permissões corretas
- [ ] ✅ ECR repository criado
- [ ] ✅ Todos os secrets configurados no GitHub
- [ ] ✅ AWS CLI configurado localmente
- [ ] ✅ Infraestrutura provisionada com Terraform
- [ ] ✅ Cluster EKS acessível via kubectl
- [ ] ✅ Pipeline executada com sucesso
- [ ] ✅ Aplicação deployada e rodando
- [ ] ✅ LoadBalancer acessível
- [ ] ✅ Health check retornando OK
- [ ] ✅ HPA funcionando
- [ ] ✅ Logs sendo gerados corretamente

---

## 🎉 Parabéns!

Se você chegou até aqui e completou todos os passos, você tem agora:

✅ Pipeline CI/CD totalmente automatizada  
✅ Deploy automático no EKS da AWS  
✅ Infraestrutura como código com Terraform  
✅ Containerização com Docker  
✅ Orquestração com Kubernetes  
✅ Auto-scaling configurado  
✅ Banco de dados gerenciado (RDS)  
✅ Rollback automatizado  
✅ Validação automática de PRs  

**Seu projeto está pronto para produção! 🚀**

---

**📧 Suporte:**

Se encontrar problemas, consulte a seção de Troubleshooting ou abra uma issue no repositório do projeto.

**Criado para Tech Challenge - 2026**

