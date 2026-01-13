# 🔄 CI/CD Pipeline - GitHub Actions

Este diretório contém todos os workflows automatizados do Tech Challenge.

## 📁 Workflows Disponíveis

### 1. **ci-cd-pipeline.yml** 🚀 (Principal)

Pipeline completo de integração e deploy contínuo.

**Triggers:**
- Push em `main` ou `develop`
- Workflow manual (com escolha de ambiente)

**Jobs:**
1. ✅ **Build & Test** - Compila e testa o código
2. 🔐 **Security Scan** - Analisa vulnerabilidades
3. 🐳 **Docker Build & Push** - Cria e publica imagem
4. 🚀 **Deploy Staging** - Deploy em staging (branch develop)
5. 🚀 **Deploy Production** - Deploy em produção (branch main)
6. 📢 **Notify** - Notificações Slack

**Tempo estimado:** ~15-20 minutos

---

### 2. **pull-request.yml** 📋

Validação automática de Pull Requests.

**Triggers:**
- Abertura de PR
- Commits em PR

**Jobs:**
1. ✅ **Validate Code** - Estilo e compilação
2. 🔐 **Security Check** - Análise de dependências
3. 🐳 **Docker Check** - Lint Dockerfile e build test

**Tempo estimado:** ~5-8 minutos

---

### 3. **terraform.yml** 🏗️

Gerenciamento de infraestrutura.

**Triggers:**
- Push em `main` (com mudanças em `infra/`)
- Pull Request com mudanças em `infra/`
- Workflow manual

**Jobs:**
1. 🔍 **Terraform Validate** - Valida syntax
2. 📋 **Terraform Plan** - Mostra mudanças
3. 🚀 **Terraform Apply** - Aplica infraestrutura (main)
4. 🗑️ **Terraform Destroy** - Destroi infraestrutura (manual)

**Tempo estimado:** Plan: ~2min, Apply: ~20min

---

### 4. **release.yml** 📦

Criação de releases.

**Triggers:**
- Push de tag `v*.*.*` (ex: v1.0.0)
- Workflow manual

**Jobs:**
1. 📦 **Create Release** - Cria release no GitHub
2. 🏗️ **Build Artifacts** - Gera JAR
3. 🐳 **Release Docker** - Publica imagem versionada

**Tempo estimado:** ~10 minutos

---

## 🔐 Secrets Necessários

Configure em **Settings** → **Secrets and variables** → **Actions**

### Docker Hub

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `DOCKER_USERNAME` | Usuário Docker Hub | `seu-usuario` |
| `DOCKER_PASSWORD` | Token Docker Hub | `dckr_pat_...` |

**Como obter Docker Hub Token:**
1. Acesse [hub.docker.com](https://hub.docker.com)
2. Settings → Security → New Access Token
3. Copie o token gerado

### Kubernetes

| Secret | Descrição | Como obter |
|--------|-----------|-----------|
| `KUBE_CONFIG_STAGING` | Kubeconfig staging (base64) | `cat ~/.kube/config \| base64 -w 0` |
| `KUBE_CONFIG_PRODUCTION` | Kubeconfig produção (base64) | `cat ~/.kube/config-prod \| base64 -w 0` |

### AWS (para Terraform)

| Secret | Descrição |
|--------|-----------|
| `AWS_ACCESS_KEY_ID` | AWS Access Key |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key |
| `DB_PASSWORD` | Senha do banco RDS |

**Como criar IAM user:**
```bash
aws iam create-user --user-name github-actions
aws iam attach-user-policy --user-name github-actions --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
aws iam create-access-key --user-name github-actions
```

### Notificações (Opcional)

| Secret | Descrição | Como obter |
|--------|-----------|-----------|
| `SLACK_WEBHOOK` | Webhook Slack | [Create Webhook](https://api.slack.com/messaging/webhooks) |
| `SONAR_TOKEN` | Token SonarQube | Dashboard → My Account → Security |
| `SONAR_HOST_URL` | URL SonarQube | `https://sonarcloud.io` |

---

## 🚀 Primeiro Uso

### 1. Configurar Secrets

```bash
# No repositório GitHub, vá em:
Settings → Secrets and variables → Actions → New repository secret

# Adicione todos os secrets listados acima
```

### 2. Criar Ambientes

```bash
# No repositório GitHub, vá em:
Settings → Environments

# Crie:
- staging (sem proteção)
- production (com required reviewers)
- infrastructure-aws (com required reviewers)
- infrastructure-aws-destroy (com required reviewers)
```

### 3. Habilitar Workflows

```bash
# No repositório GitHub, vá em:
Actions → Enable workflows

# Todos os workflows devem aparecer na lista
```

### 4. Primeiro Deploy

```bash
# Fazer push no main
git add .
git commit -m "feat: initial setup"
git push origin main

# Acompanhar em:
Actions → CI/CD Pipeline
```

---

## 📊 Fluxo de Trabalho

### Feature Development

```bash
# 1. Criar branch
git checkout -b feature/nova-funcionalidade

# 2. Desenvolver e commitar
git add .
git commit -m "feat: adiciona nova funcionalidade"
git push origin feature/nova-funcionalidade

# 3. Abrir Pull Request
# → Workflow "Pull Request" é executado automaticamente
# → Valida código, testes e segurança

# 4. Após aprovação, merge para develop
# → Deploy automático em Staging

# 5. Após testes em staging, merge develop → main
# → Deploy automático em Production
```

### Hotfix

```bash
# 1. Criar branch de hotfix
git checkout -b hotfix/correcao-critica main

# 2. Corrigir e commitar
git add .
git commit -m "fix: corrige bug crítico"

# 3. Merge direto para main (após PR approval)
git checkout main
git merge hotfix/correcao-critica
git push origin main
# → Deploy automático em Production

# 4. Backport para develop
git checkout develop
git merge hotfix/correcao-critica
git push origin develop
```

### Release

```bash
# 1. Atualizar versão no pom.xml
mvn versions:set -DnewVersion=1.2.0

# 2. Commitar
git add pom.xml
git commit -m "chore: bump version to 1.2.0"
git push origin main

# 3. Criar tag
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0

# → Workflow "Release" é executado automaticamente
# → Cria release no GitHub
# → Publica imagem Docker com versão
```

---

## 🐛 Troubleshooting

### Erro: "Image not found"

```yaml
# Verifique se os secrets estão configurados:
DOCKER_USERNAME
DOCKER_PASSWORD
```

### Erro: "kubectl: connection refused"

```yaml
# Verifique se o KUBE_CONFIG está correto:
echo $KUBE_CONFIG_PRODUCTION | base64 -d > kubeconfig
kubectl --kubeconfig=kubeconfig get nodes
```

### Erro: "Terraform state locked"

```bash
# Forçar unlock (cuidado!)
terraform force-unlock <LOCK_ID>
```

### Erro: "Permission denied"

```yaml
# Verifique as permissões do workflow:
Settings → Actions → General → Workflow permissions
→ Read and write permissions ✅
```

---

## 📈 Monitoramento

### GitHub Actions Dashboard

```
Repositório → Actions
```

- Ver todos os workflows
- Histórico de execuções
- Logs detalhados
- Artifacts gerados

### Métricas Úteis

- **Success Rate:** % de builds bem sucedidos
- **Build Time:** Tempo médio de execução
- **Deploy Frequency:** Frequência de deploys
- **MTTR:** Tempo médio de recuperação

---

## 🎯 Boas Práticas

### ✅ DO

- ✅ Usar secrets para credenciais
- ✅ Testar workflows em branch separada
- ✅ Adicionar caching para deps
- ✅ Usar ambientes para proteção
- ✅ Documentar mudanças em PR
- ✅ Versionar imagens Docker

### ❌ DON'T

- ❌ Commitar secrets no código
- ❌ Pular testes em PRs
- ❌ Deploy direto sem review
- ❌ Usar `latest` em produção
- ❌ Ignorar falhas de segurança

---

## 🔄 Atualizações

### Atualizar Java Version

```yaml
# Em todos os workflows:
env:
  JAVA_VERSION: '21'  # Alterar para versão desejada
```

### Atualizar Terraform Version

```yaml
# Em terraform.yml:
env:
  TF_VERSION: '1.6.0'  # Alterar para versão desejada
```

### Adicionar Novo Ambiente

1. Criar ambiente no GitHub
2. Adicionar job no workflow
3. Configurar secrets específicos

---

## 📚 Referências

- [GitHub Actions Docs](https://docs.github.com/actions)
- [Docker Build Push Action](https://github.com/marketplace/actions/build-and-push-docker-images)
- [Kubernetes Actions](https://github.com/marketplace?type=actions&query=kubernetes)
- [Terraform Actions](https://github.com/marketplace/actions/hashicorp-setup-terraform)

---

## 🆘 Suporte

**Problemas com workflows?**

1. Verificar logs em Actions
2. Revisar secrets configurados
3. Testar comandos localmente
4. Consultar documentação oficial

**Canal Slack:** #tech-challenge-cicd  
**Email:** devops@tech-challenge.com


