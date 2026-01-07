# 🏗️ Infraestrutura como Código - Terraform

## ✅ Implementação Completa

A infraestrutura do Tech Challenge foi implementada usando **Terraform** com suporte para 3 provedores cloud:

### 📁 Estrutura Criada

```
infra/
├── README.md                    # Documentação geral
├── TERRAFORM.md                 # Este arquivo
│
├── aws/                         # ✅ AWS (EKS + RDS)
│   ├── provider.tf              # Configuração AWS provider
│   ├── variables.tf             # Variáveis de entrada
│   ├── main.tf                  # Recursos principais
│   ├── vpc.tf                   # VPC e networking
│   ├── eks.tf                   # Cluster EKS
│   ├── rds.tf                   # PostgreSQL RDS
│   ├── outputs.tf               # Outputs úteis
│   ├── kubeconfig.tpl           # Template kubeconfig
│   ├── terraform.tfvars.example # Exemplo de configuração
│   ├── .gitignore               # Git ignore
│   └── README.md                # Documentação AWS
│
├── azure/                       # 🔵 Azure (AKS + PostgreSQL)
│   └── (arquivos similares)
│
└── gcp/                         # 🟢 GCP (GKE + Cloud SQL)
    └── (arquivos similares)
```

---

## 🚀 AWS - Amazon Web Services

### ✨ Recursos Provisionados

| Recurso | Descrição | Configuração |
|---------|-----------|--------------|
| **VPC** | Rede privada | CIDR 10.0.0.0/16, 2 AZs |
| **Subnets** | Públicas, Privadas, DB | 6 subnets (2 de cada) |
| **NAT Gateway** | Conectividade nodes | 2 (alta disponibilidade) |
| **EKS** | Kubernetes Cluster | v1.28, 2-4 nodes |
| **Nodes** | EC2 instances | t3.medium (2 vCPU, 4GB RAM) |
| **RDS** | PostgreSQL 15.4 | t3.micro (1GB RAM, 20GB SSD) |
| **Security Groups** | Firewall rules | Configurados |
| **IAM Roles** | Permissões | EKS + RDS Enhanced Monitoring |

### 💰 Custos Estimados (us-east-1)

- **EKS Control Plane:** $73/mês
- **EC2 Nodes (2x t3.medium):** $60/mês
- **RDS PostgreSQL:** $15/mês
- **NAT Gateway (2x):** $45/mês
- **Storage, Transfer:** $10/mês
- **Total:** ~**$203/mês**

### 📋 Quick Start AWS

```bash
cd infra/aws

# 1. Configurar credenciais
aws configure

# 2. Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite com seus valores

# 3. Definir senha do banco (NUNCA no código!)
export TF_VAR_db_password="SuaSenhaSuperSegura123!"

# 4. Inicializar e aplicar
terraform init
terraform plan
terraform apply  # Leva ~15-20 minutos

# 5. Configurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 6. Verificar cluster
kubectl get nodes

# 7. Deploy da aplicação
kubectl apply -f ../../k8s/
```

---

## 🔵 Azure - Microsoft Azure

### ✨ Recursos Provisionados

| Recurso | Descrição | Configuração |
|---------|-----------|--------------|
| **Resource Group** | Grupo de recursos | tech-challenge-rg |
| **Virtual Network** | Rede virtual | CIDR 10.0.0.0/16 |
| **Subnets** | AKS, Database | 2 subnets |
| **AKS** | Kubernetes Cluster | v1.28, 2-4 nodes |
| **Nodes** | VM instances | Standard_D2s_v3 (2 vCPU, 8GB RAM) |
| **PostgreSQL** | Azure Database | B_Gen5_1 (1 vCore, 2GB RAM, 20GB) |
| **NSG** | Network Security Groups | Configurados |
| **Managed Identity** | Identidade gerenciada | AKS |

### 💰 Custos Estimados (East US)

- **AKS Control Plane:** Grátis
- **VMs (2x D2s_v3):** $70/mês
- **PostgreSQL:** $25/mês
- **Load Balancer:** $20/mês
- **Total:** ~**$115/mês** ✨ Mais econômico!

### 📋 Quick Start Azure

```bash
cd infra/azure

# 1. Login no Azure
az login

# 2. Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars

# 3. Definir senha do banco
export TF_VAR_db_password="SuaSenhaSuperSegura123!"

# 4. Inicializar e aplicar
terraform init
terraform plan
terraform apply  # Leva ~10-15 minutos

# 5. Configurar kubectl
az aks get-credentials --name tech-challenge-aks --resource-group tech-challenge-rg

# 6. Verificar cluster
kubectl get nodes

# 7. Deploy da aplicação
kubectl apply -f ../../k8s/
```

---

## 🟢 GCP - Google Cloud Platform

### ✨ Recursos Provisionados

| Recurso | Descrição | Configuração |
|---------|-----------|--------------|
| **VPC** | Virtual Private Cloud | Auto mode |
| **Subnets** | GKE, Database | Auto-criadas |
| **GKE** | Kubernetes Cluster | v1.28, 2-4 nodes |
| **Nodes** | Compute Engine | e2-medium (2 vCPU, 4GB RAM) |
| **Cloud SQL** | PostgreSQL 15 | db-f1-micro (0.6GB RAM, 20GB SSD) |
| **Firewall Rules** | Regras de firewall | Configuradas |
| **Service Account** | Conta de serviço | GKE |

### 💰 Custos Estimados (us-central1)

- **GKE Control Plane:** $73/mês
- **Nodes (2x e2-medium):** $50/mês
- **Cloud SQL:** $15/mês
- **Load Balancer:** $30/mês
- **Total:** ~**$168/mês**

### 📋 Quick Start GCP

```bash
cd infra/gcp

# 1. Login no GCP
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# 2. Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars

# 3. Definir senha do banco
export TF_VAR_db_password="SuaSenhaSuperSegura123!"

# 4. Inicializar e aplicar
terraform init
terraform plan
terraform apply  # Leva ~12-18 minutos

# 5. Configurar kubectl
gcloud container clusters get-credentials tech-challenge-gke --zone us-central1-a

# 6. Verificar cluster
kubectl get nodes

# 7. Deploy da aplicação
kubectl apply -f ../../k8s/
```

---

## 📊 Comparação de Provedores

| Característica | AWS | Azure | GCP |
|----------------|-----|-------|-----|
| **Custo/mês** | ~$203 | ~$115 ⭐ | ~$168 |
| **Setup** | 15-20 min | 10-15 min ⭐ | 12-18 min |
| **K8s Control Plane** | $73 | Grátis ⭐ | $73 |
| **Node RAM** | 4GB | 8GB ⭐ | 4GB |
| **Popularidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Free Tier** | 12 meses | 12 meses | 90 dias + $300 |

**Recomendação:**
- **Produção (Budget):** Azure 💙
- **Produção (Enterprise):** AWS 🟠
- **Learning/Demo:** GCP (Free trial) 🟢

---

## 🔐 Segurança - Boas Práticas

### ⚠️ NUNCA faça isso:

```hcl
# ❌ ERRADO - NUNCA coloque senhas no código!
variable "db_password" {
  default = "minha-senha-123"  # ❌ NUNCA!
}
```

### ✅ Faça assim:

```bash
# ✅ Opção 1: Variável de ambiente
export TF_VAR_db_password="senha-super-segura-123!"

# ✅ Opção 2: Terraform Cloud (encriptado)
terraform login
# Configure variável no UI

# ✅ Opção 3: Secrets Manager
# AWS: Secrets Manager
# Azure: Key Vault
# GCP: Secret Manager
```

### 🔒 .gitignore

**SEMPRE** adicione no `.gitignore`:

```
*.tfvars
*.tfstate
*.tfstate.backup
.terraform/
.terraform.lock.hcl
```

---

## 🔄 Workflow Recomendado

### 1. Desenvolvimento (Dev)

```bash
# Ambiente barato para testes
node_desired_size = 1
db_instance_class = "db.t3.micro"  # ou menor
db_multi_az       = false
```

### 2. Staging

```bash
# Ambiente similar à produção
node_desired_size = 2
db_instance_class = "db.t3.small"
db_multi_az       = false
```

### 3. Produção

```bash
# Ambiente robusto
node_desired_size = 3
db_instance_class = "db.t3.medium"
db_multi_az       = true
enable_deletion_protection = true
```

### Usar Workspaces

```bash
# Criar workspaces
terraform workspace new dev
terraform workspace new staging
terraform workspace new prod

# Alternar
terraform workspace select prod

# Aplicar com variáveis específicas
terraform apply -var-file="prod.tfvars"
```

---

## 🧪 Ambientes Isolados

### Estrutura Recomendada

```
infra/
└── aws/
    ├── dev.tfvars       # Configuração dev
    ├── staging.tfvars   # Configuração staging
    └── prod.tfvars      # Configuração produção
```

### Deploy por Ambiente

```bash
# Dev
terraform apply -var-file="dev.tfvars"

# Staging
terraform apply -var-file="staging.tfvars"

# Prod
terraform apply -var-file="prod.tfvars"
```

---

## 📈 Monitoramento e Observabilidade

### Após provisionar, configure:

**AWS:**
```bash
# CloudWatch Logs
# Container Insights
# X-Ray
```

**Azure:**
```bash
# Azure Monitor
# Application Insights
# Log Analytics
```

**GCP:**
```bash
# Cloud Monitoring (Stackdriver)
# Cloud Logging
# Cloud Trace
```

### Prometheus + Grafana (todos)

```bash
# Adicionar Prometheus ao cluster
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

---

## 🗑️ Destruir Infraestrutura

### ⚠️ ATENÇÃO: Ordem Importa!

```bash
# 1. Deletar aplicação Kubernetes PRIMEIRO
kubectl delete namespace tech-challenge

# 2. Aguardar LoadBalancers serem removidos (2-3 min)
# Se não aguardar, o Terraform não consegue deletar a VPC

# 3. Destruir infraestrutura Terraform
terraform destroy

# 4. Confirmar com "yes"

# 5. Verificar se tudo foi deletado
# AWS:
aws ec2 describe-vpcs --filters "Name=tag:Project,Values=tech-challenge"
# Azure:
az group show --name tech-challenge-rg
# GCP:
gcloud compute instances list --filter="labels.project=tech-challenge"
```

---

## 💡 Dicas e Truques

### 1. Planejar antes de aplicar

```bash
# Ver mudanças sem aplicar
terraform plan

# Salvar plano
terraform plan -out=tfplan

# Aplicar plano salvo
terraform apply tfplan
```

### 2. Importar recursos existentes

```bash
# Se recursos já existem
terraform import aws_vpc.main vpc-12345678
```

### 3. Taint/Untaint

```bash
# Forçar recriação de recurso
terraform taint aws_instance.app

# Remover taint
terraform untaint aws_instance.app
```

### 4. State Management

```bash
# Listar recursos no state
terraform state list

# Ver detalhes de recurso
terraform state show aws_vpc.main

# Mover recurso no state
terraform state mv aws_instance.old aws_instance.new
```

### 5. Debugging

```bash
# Modo debug
export TF_LOG=DEBUG
terraform apply

# Logs em arquivo
export TF_LOG_PATH=./terraform.log
```

---

## 🐛 Troubleshooting Comum

### "Provider not configured"

```bash
# Verificar credenciais
aws sts get-caller-identity    # AWS
az account show                # Azure
gcloud auth list               # GCP
```

### "State lock"

```bash
# Forçar unlock (USE COM CUIDADO!)
terraform force-unlock <LOCK_ID>
```

### "Resource already exists"

```bash
# Importar recurso
terraform import <resource_type>.<name> <resource_id>
```

### "Insufficient permissions"

```bash
# Verificar permissões IAM/RBAC
# AWS: AmazonEKSClusterPolicy, etc.
# Azure: Contributor role
# GCP: Kubernetes Engine Admin
```

---

## 📚 Documentação e Referências

### Terraform
- [Terraform Docs](https://www.terraform.io/docs)
- [Terraform Registry](https://registry.terraform.io/)
- [Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/index.html)

### Providers
- [AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [GCP Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)

### Kubernetes
- [EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)
- [AKS Best Practices](https://learn.microsoft.com/azure/aks/best-practices)
- [GKE Best Practices](https://cloud.google.com/kubernetes-engine/docs/best-practices)

---

## 🎯 Próximos Passos

1. **Backend Remoto:** Configurar S3/Azure Blob/GCS para state compartilhado
2. **CI/CD:** Integrar com GitHub Actions/GitLab CI
3. **GitOps:** Terraform Cloud ou Atlantis
4. **Módulos:** Criar módulos reutilizáveis
5. **Testing:** Terratest para testes automatizados
6. **Cost Optimization:** Implementar auto-shutdown para dev
7. **Multi-Region:** Deploy em múltiplas regiões

---

**🎉 Infraestrutura como Código Implementada com Sucesso!**

Documentação completa, exemplos práticos e pronta para produção! 🚀

