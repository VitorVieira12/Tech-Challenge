# 🏗️ Infraestrutura como Código - Terraform

Este diretório contém scripts Terraform para provisionar automaticamente a infraestrutura necessária para o Tech Challenge em diferentes provedores de cloud.

## 📁 Estrutura

```
infra/
├── aws/                    # AWS (EKS + RDS PostgreSQL)
│   ├── main.tf
│   ├── variables.tf
│   ├── provider.tf
│   ├── outputs.tf
│   ├── vpc.tf
│   ├── eks.tf
│   ├── rds.tf
│   └── terraform.tfvars.example
│
├── azure/                  # Azure (AKS + Azure Database for PostgreSQL)
│   ├── main.tf
│   ├── variables.tf
│   ├── provider.tf
│   ├── outputs.tf
│   ├── aks.tf
│   ├── postgresql.tf
│   └── terraform.tfvars.example
│
├── gcp/                    # Google Cloud (GKE + Cloud SQL)
│   ├── main.tf
│   ├── variables.tf
│   ├── provider.tf
│   ├── outputs.tf
│   ├── gke.tf
│   ├── cloudsql.tf
│   └── terraform.tfvars.example
│
├── modules/                # Módulos reutilizáveis
│   └── (futura expansão)
│
└── README.md               # Este arquivo
```

---

## 🚀 Provedores Suportados

### 1. AWS - Amazon Web Services
- **Kubernetes:** EKS (Elastic Kubernetes Service)
- **Database:** RDS PostgreSQL
- **Network:** VPC, Subnets, NAT Gateway, Internet Gateway
- **Security:** Security Groups, IAM Roles

### 2. Azure - Microsoft Azure
- **Kubernetes:** AKS (Azure Kubernetes Service)
- **Database:** Azure Database for PostgreSQL
- **Network:** Virtual Network, Subnets
- **Security:** Network Security Groups, Managed Identity

### 3. GCP - Google Cloud Platform
- **Kubernetes:** GKE (Google Kubernetes Engine)
- **Database:** Cloud SQL for PostgreSQL
- **Network:** VPC, Subnets
- **Security:** Firewall Rules, Service Accounts

---

## 📋 Pré-requisitos

### Ferramentas Necessárias

1. **Terraform** >= 1.5.0
   ```bash
   # Instalar Terraform
   # Windows (Chocolatey):
   choco install terraform
   
   # Linux:
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   
   # macOS (Homebrew):
   brew install terraform
   
   # Verificar instalação:
   terraform version
   ```

2. **Cloud CLI** (escolha o provider)
   - **AWS CLI:** `aws configure`
   - **Azure CLI:** `az login`
   - **gcloud CLI:** `gcloud auth login`

3. **kubectl** para gerenciar Kubernetes

---

## 🎯 Quick Start

### AWS (EKS + RDS)

```bash
# 1. Navegar para o diretório AWS
cd infra/aws

# 2. Configurar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com suas configurações

# 3. Inicializar Terraform
terraform init

# 4. Planejar (preview)
terraform plan

# 5. Aplicar
terraform apply

# 6. Obter kubeconfig
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 7. Verificar conexão
kubectl get nodes
```

### Azure (AKS + PostgreSQL)

```bash
# 1. Navegar para o diretório Azure
cd infra/azure

# 2. Configurar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com suas configurações

# 3. Inicializar Terraform
terraform init

# 4. Planejar
terraform plan

# 5. Aplicar
terraform apply

# 6. Obter kubeconfig
az aks get-credentials --name tech-challenge-aks --resource-group tech-challenge-rg

# 7. Verificar conexão
kubectl get nodes
```

### GCP (GKE + Cloud SQL)

```bash
# 1. Navegar para o diretório GCP
cd infra/gcp

# 2. Configurar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com suas configurações

# 3. Inicializar Terraform
terraform init

# 4. Planejar
terraform plan

# 5. Aplicar
terraform apply

# 6. Obter kubeconfig
gcloud container clusters get-credentials tech-challenge-gke --zone us-central1-a

# 7. Verificar conexão
kubectl get nodes
```

---

## 🔧 Configurações Principais

### Recursos Provisionados

#### Kubernetes Cluster
- **Nodes:** 2-4 (auto-scaling)
- **Node Type:** 
  - AWS: t3.medium (2 vCPU, 4GB RAM)
  - Azure: Standard_D2s_v3 (2 vCPU, 8GB RAM)
  - GCP: e2-medium (2 vCPU, 4GB RAM)
- **Kubernetes Version:** Latest stable

#### PostgreSQL Database
- **Instance Type:**
  - AWS: db.t3.micro (2 vCPU, 1GB RAM)
  - Azure: B_Gen5_1 (1 vCore, 2GB RAM)
  - GCP: db-f1-micro (1 vCPU, 0.6GB RAM)
- **Storage:** 20GB (SSD)
- **Version:** PostgreSQL 15
- **High Availability:** Opcional (variável)
- **Backup:** Automático (7 dias)

#### Network
- **VPC/VNet:** Dedicado com CIDR 10.0.0.0/16
- **Subnets:** 
  - Public (NAT/Load Balancer)
  - Private (Kubernetes nodes)
  - Database (PostgreSQL)
- **Security:** Security Groups/NSGs com regras mínimas

---

## 💰 Estimativa de Custos

### AWS (us-east-1)
- **EKS Cluster:** ~$73/mês
- **EC2 Nodes (2x t3.medium):** ~$60/mês
- **RDS PostgreSQL (t3.micro):** ~$15/mês
- **Network (NAT, etc):** ~$45/mês
- **Total:** ~$193/mês

### Azure (East US)
- **AKS:** Grátis (paga só os nodes)
- **Nodes (2x D2s_v3):** ~$70/mês
- **PostgreSQL (B_Gen5_1):** ~$25/mês
- **Network:** ~$20/mês
- **Total:** ~$115/mês

### GCP (us-central1)
- **GKE:** ~$73/mês
- **Nodes (2x e2-medium):** ~$50/mês
- **Cloud SQL (db-f1-micro):** ~$15/mês
- **Network:** ~$30/mês
- **Total:** ~$168/mês

⚠️ **Importante:** Valores aproximados. Use as calculadoras oficiais para estimativas exatas:
- [AWS Calculator](https://calculator.aws/)
- [Azure Calculator](https://azure.microsoft.com/pricing/calculator/)
- [GCP Calculator](https://cloud.google.com/products/calculator)

---

## 🔐 Segurança

### Credenciais

**NUNCA commite credenciais no Git!**

```bash
# Adicione ao .gitignore:
*.tfvars
*.tfstate
*.tfstate.backup
.terraform/
```

### Best Practices Implementadas

✅ **Secrets:** Armazenados em Secret Manager/Key Vault  
✅ **IAM:** Princípio do menor privilégio  
✅ **Network:** Subnets privadas para DB e nodes  
✅ **Encryption:** At rest e in transit  
✅ **Backup:** Automático e retenção configurável  
✅ **Monitoring:** CloudWatch/Monitor/Stackdriver  

---

## 📊 Variáveis Personalizáveis

### Comuns a todos os providers

```hcl
project_name        = "tech-challenge"
environment         = "production"
region              = "us-east-1" # ou equivalente

# Kubernetes
k8s_version         = "1.28"
node_count          = 2
node_instance_type  = "t3.medium" # ou equivalente

# Database
db_instance_class   = "db.t3.micro" # ou equivalente
db_storage_gb       = 20
db_username         = "tech_admin"
db_name             = "tech_challenge_db"

# Network
vpc_cidr            = "10.0.0.0/16"

# Tags/Labels
tags = {
  Project     = "tech-challenge"
  Environment = "production"
  ManagedBy   = "terraform"
}
```

---

## 🔄 Workflow Completo

### 1. Planejar Infraestrutura

```bash
cd infra/aws  # ou azure/gcp
terraform init
terraform plan -out=tfplan
```

### 2. Aplicar Mudanças

```bash
terraform apply tfplan
```

### 3. Verificar Recursos

```bash
# Ver outputs
terraform output

# Listar recursos
terraform state list
```

### 4. Fazer Deploy da Aplicação

```bash
# Configurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# Aplicar manifests K8s
kubectl apply -f ../../k8s/

# Verificar pods
kubectl get pods -n tech-challenge
```

### 5. Destruir Infraestrutura (quando não precisar mais)

```bash
# ATENÇÃO: Isso vai deletar TUDO!
terraform destroy
```

---

## 🧪 Ambientes (Dev/Staging/Prod)

### Workspaces do Terraform

```bash
# Criar workspace de staging
terraform workspace new staging

# Listar workspaces
terraform workspace list

# Mudar para workspace
terraform workspace select production

# Aplicar com variáveis específicas
terraform apply -var-file="staging.tfvars"
```

### Estrutura Recomendada

```
infra/
├── aws/
│   ├── dev.tfvars
│   ├── staging.tfvars
│   └── prod.tfvars
```

---

## 📈 Monitoramento

### Logs e Métricas

Após provisionar, configure:

1. **CloudWatch/Azure Monitor/Stackdriver**
   - Logs dos clusters
   - Métricas de recursos
   - Alarmes de custo

2. **Prometheus + Grafana** (opcional)
   ```bash
   helm install prometheus prometheus-community/kube-prometheus-stack
   ```

3. **Cost Management**
   - AWS Cost Explorer
   - Azure Cost Management
   - GCP Billing Reports

---

## 🐛 Troubleshooting

### Erro: "Provider not configured"

```bash
# Verificar credenciais
aws configure list
az account show
gcloud auth list
```

### Erro: "Resource already exists"

```bash
# Importar recurso existente
terraform import aws_vpc.main vpc-12345678
```

### Erro: "Insufficient permissions"

```bash
# Verificar permissões IAM/RBAC
# AWS:
aws sts get-caller-identity
# Azure:
az account show
# GCP:
gcloud auth list
```

### State Lock

```bash
# Forçar unlock (USE COM CUIDADO!)
terraform force-unlock <LOCK_ID>
```

---

## 🧹 Limpeza

### Destruir Infraestrutura

```bash
# Destruir aplicação primeiro
kubectl delete namespace tech-challenge

# Aguarde alguns minutos para LoadBalancers serem deletados

# Destruir infraestrutura
cd infra/aws  # ou azure/gcp
terraform destroy

# Confirme com "yes"
```

### Verificar Recursos Órfãos

```bash
# AWS
aws ec2 describe-instances --filters "Name=tag:Project,Values=tech-challenge"

# Azure
az resource list --tag Project=tech-challenge

# GCP
gcloud compute instances list --filter="labels.project=tech-challenge"
```

---

## 📚 Documentação Adicional

- [AWS EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)
- [Azure AKS Best Practices](https://learn.microsoft.com/azure/aks/best-practices)
- [GCP GKE Best Practices](https://cloud.google.com/kubernetes-engine/docs/best-practices)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Terraform GCP Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)

---

## 🎯 Próximos Passos

1. **CI/CD:** Integrar com GitHub Actions/GitLab CI
2. **GitOps:** Usar ArgoCD ou Flux
3. **Service Mesh:** Istio ou Linkerd
4. **Observability:** Prometheus, Grafana, Jaeger
5. **Cost Optimization:** Auto-scaling, Spot Instances
6. **Disaster Recovery:** Multi-region setup

---

## 💡 Dicas

1. **Use workspaces** para ambientes diferentes
2. **Backend remoto** (S3, Azure Blob, GCS) para state compartilhado
3. **State locking** para evitar conflitos em equipe
4. **Módulos** para reutilizar código
5. **Versionamento** de providers para reprodutibilidade
6. **Taint/Untaint** para forçar recriação de recursos

---

**📞 Suporte:**

Para dúvidas ou problemas, consulte:
- [Terraform Documentation](https://www.terraform.io/docs)
- [Cloud Provider Documentation](https://docs.aws.amazon.com/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/terraform)



