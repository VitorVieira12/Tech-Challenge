# 🔐 Guia de Configuração de Secrets - GitHub Actions

Este guia detalha como configurar todos os secrets necessários para os workflows funcionarem.

## 📍 Onde Configurar

```
GitHub Repository → Settings → Secrets and variables → Actions → New repository secret
```

---

## 1️⃣ Docker Hub (Obrigatório)

### DOCKER_USERNAME

**Descrição:** Seu nome de usuário do Docker Hub

**Como obter:**
1. Acesse [hub.docker.com](https://hub.docker.com)
2. Faça login
3. Seu username aparece no canto superior direito

**Valor de exemplo:** `seu-usuario`

---

### DOCKER_PASSWORD

**Descrição:** Token de acesso do Docker Hub (NÃO use sua senha!)

**Como obter:**
1. Acesse [hub.docker.com](https://hub.docker.com)
2. Vá em **Account Settings** → **Security**
3. Clique em **New Access Token**
4. Nome: `github-actions`
5. Permissions: **Read, Write, Delete**
6. Clique em **Generate**
7. **COPIE O TOKEN IMEDIATAMENTE** (você não poderá vê-lo novamente!)

**Valor de exemplo:** `dckr_pat_xxxxxxxxxxxxx`

---

## 2️⃣ Kubernetes (Obrigatório para Deploy)

### KUBE_CONFIG_STAGING

**Descrição:** Arquivo kubeconfig do cluster staging (codificado em base64)

**Como obter:**

```bash
# Opção 1: Se você já tem um cluster
cat ~/.kube/config | base64 -w 0

# Opção 2: Se provisionou com Terraform AWS
cd infra/aws
terraform output -raw kubeconfig | base64 -w 0

# Opção 3: EKS
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1
cat ~/.kube/config | base64 -w 0

# Opção 4: AKS
az aks get-credentials --name tech-challenge-aks --resource-group tech-challenge-rg
cat ~/.kube/config | base64 -w 0

# Opção 5: GKE
gcloud container clusters get-credentials tech-challenge-gke --zone us-central1-a
cat ~/.kube/config | base64 -w 0
```

**⚠️ IMPORTANTE:** O valor deve ser base64, uma única linha!

**Validar:**
```bash
echo "SEU_SECRET_AQUI" | base64 -d > test-kubeconfig
kubectl --kubeconfig=test-kubeconfig get nodes
rm test-kubeconfig
```

---

### KUBE_CONFIG_PRODUCTION

**Descrição:** Arquivo kubeconfig do cluster production (codificado em base64)

**Como obter:** Mesmo processo do `KUBE_CONFIG_STAGING`, mas usando o cluster de produção.

**⚠️ IMPORTANTE:**
- Use um cluster DIFERENTE para produção
- Configure namespace separado
- Restrinja permissões (use service account limitado)

---

## 3️⃣ AWS (Obrigatório para Terraform)

### AWS_ACCESS_KEY_ID

**Descrição:** ID da Access Key da AWS

**Como obter:**

```bash
# Opção 1: Via AWS Console
# 1. Acesse AWS Console
# 2. IAM → Users → seu-usuario → Security credentials
# 3. Create access key → Command Line Interface (CLI)
# 4. Copie o Access Key ID

# Opção 2: Via AWS CLI (para usuário github-actions)
aws iam create-user --user-name github-actions
aws iam attach-user-policy \
  --user-name github-actions \
  --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
aws iam create-access-key --user-name github-actions
```

**Valor de exemplo:** `AKIAIOSFODNN7EXAMPLE`

---

### AWS_SECRET_ACCESS_KEY

**Descrição:** Secret Access Key da AWS

**Como obter:**
- É gerada junto com a Access Key ID
- **COPIE IMEDIATAMENTE** (você não poderá vê-la novamente!)
- Se perdeu, delete a key e crie uma nova

**Valor de exemplo:** `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`

---

### DB_PASSWORD

**Descrição:** Senha do banco de dados RDS

**Como definir:**
- Use uma senha forte (mínimo 16 caracteres)
- Inclua letras maiúsculas, minúsculas, números e símbolos
- NÃO use caracteres especiais que precisam escape (`, ", ', \)

**Gerador:**
```bash
# Gerar senha segura
openssl rand -base64 24 | tr -d "=+/" | cut -c1-20
```

**Valor de exemplo:** `MySecureP@ssw0rd2024!`

---

## 4️⃣ Notificações (Opcional)

### SLACK_WEBHOOK

**Descrição:** Webhook URL do Slack para notificações

**Como obter:**
1. Acesse [api.slack.com/apps](https://api.slack.com/apps)
2. Clique em **Create New App**
3. Escolha **From scratch**
4. Nome: `Tech Challenge CI/CD`
5. Escolha seu workspace
6. Vá em **Incoming Webhooks**
7. Ative **Activate Incoming Webhooks**
8. Clique em **Add New Webhook to Workspace**
9. Escolha o canal (ex: `#deployments`)
10. Copie a **Webhook URL**

**Valor de exemplo:** `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX`

---

### SONAR_TOKEN

**Descrição:** Token de autenticação do SonarQube/SonarCloud

**Como obter:**

**SonarCloud:**
1. Acesse [sonarcloud.io](https://sonarcloud.io)
2. Faça login (pode usar GitHub)
3. My Account → Security
4. Generate Token
5. Nome: `github-actions`
6. Copie o token

**SonarQube (self-hosted):**
1. Acesse sua instância SonarQube
2. My Account → Security → Generate Tokens
3. Copie o token

**Valor de exemplo:** `sqp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

---

### SONAR_HOST_URL

**Descrição:** URL da instância SonarQube

**Valores:**
- SonarCloud: `https://sonarcloud.io`
- Self-hosted: `https://sonar.sua-empresa.com`

---

## 5️⃣ GitHub Token (Automático)

### GITHUB_TOKEN

**Descrição:** Token automático do GitHub Actions

**Configuração:**
- ✅ Gerado automaticamente
- ❌ NÃO precisa criar secret
- ⚙️ Configurar permissões:

```
Settings → Actions → General → Workflow permissions
→ Read and write permissions ✅
→ Allow GitHub Actions to create and approve pull requests ✅
```

---

## ✅ Checklist de Configuração

### Obrigatório (Para CI/CD básico funcionar)

- [ ] `DOCKER_USERNAME`
- [ ] `DOCKER_PASSWORD`

### Obrigatório (Para Deploy funcionar)

- [ ] `KUBE_CONFIG_STAGING`
- [ ] `KUBE_CONFIG_PRODUCTION`

### Obrigatório (Para Terraform funcionar)

- [ ] `AWS_ACCESS_KEY_ID`
- [ ] `AWS_SECRET_ACCESS_KEY`
- [ ] `DB_PASSWORD`

### Opcional (Melhorias)

- [ ] `SLACK_WEBHOOK`
- [ ] `SONAR_TOKEN`
- [ ] `SONAR_HOST_URL`

---

## 🔒 Segurança

### ✅ Boas Práticas

1. **Rotação de Secrets**
   ```bash
   # Trocar secrets regularmente (a cada 90 dias)
   ```

2. **Princípio do Menor Privilégio**
   ```bash
   # Use IAM roles específicas, não AdministratorAccess em produção
   ```

3. **Auditoria**
   ```bash
   # Monitore uso de secrets
   Settings → Actions → Audit log
   ```

4. **Ambientes Separados**
   ```bash
   # Use secrets diferentes para staging e production
   ```

5. **Backup de Secrets**
   ```bash
   # Documente onde estão armazenados (Password Manager)
   ```

### ❌ Nunca Faça

- ❌ Commitar secrets no código
- ❌ Compartilhar secrets via chat/email
- ❌ Usar mesma senha em múltiplos lugares
- ❌ Logar secrets no console
- ❌ Usar secrets em PRs de forks

---

## 🧪 Testando Secrets

### Testar Docker Hub

```bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
```

### Testar Kubeconfig

```bash
echo "$KUBE_CONFIG_STAGING" | base64 -d > test-kube
kubectl --kubeconfig=test-kube get nodes
rm test-kube
```

### Testar AWS

```bash
export AWS_ACCESS_KEY_ID="seu-key-id"
export AWS_SECRET_ACCESS_KEY="seu-secret"
aws sts get-caller-identity
```

### Testar Slack Webhook

```bash
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"Test from GitHub Actions!"}' \
  "$SLACK_WEBHOOK"
```

---

## 📋 Template de Documentação

```markdown
# Secrets do Projeto Tech Challenge

| Secret | Valor | Atualizado em | Expira em |
|--------|-------|---------------|-----------|
| DOCKER_USERNAME | `seu-usuario` | 2024-01-07 | N/A |
| DOCKER_PASSWORD | `dckr_pat_***` | 2024-01-07 | 2024-04-07 |
| AWS_ACCESS_KEY_ID | `AKIA***` | 2024-01-07 | 2024-04-07 |
| ... | ... | ... | ... |

**Localização:** 1Password / Vault Tech Challenge
**Responsável:** DevOps Team
**Próxima revisão:** 2024-04-07
```

---

## 🆘 Troubleshooting

### Erro: "Invalid credentials"

1. Verificar se secret está configurado
2. Verificar espaços em branco extras
3. Recriar secret

### Erro: "Permission denied"

1. Verificar permissões do IAM user/token
2. Verificar expiração do token
3. Regenerar credenciais

### Erro: "Secret not found"

1. Verificar nome EXATO do secret
2. Verificar se está em Actions (não Dependabot)
3. Salvar novamente

---

## 📚 Referências

- [GitHub Encrypted Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [AWS IAM Best Practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)
- [Docker Hub Access Tokens](https://docs.docker.com/docker-hub/access-tokens/)
- [Kubernetes RBAC](https://kubernetes.io/docs/reference/access-authn-authz/rbac/)


