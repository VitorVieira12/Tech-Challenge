# 🚀 AWS Infrastructure - EKS + RDS

Infraestrutura completa para o Tech Challenge na AWS usando Terraform.

## 📦 O que será criado

- ✅ **VPC** com subnets públicas, privadas e de banco de dados em 2 AZs
- ✅ **EKS Cluster** (Kubernetes) com node group auto-scaling
- ✅ **RDS PostgreSQL** 15.4 com backups automáticos
- ✅ **NAT Gateways** para conectividade dos nodes
- ✅ **Security Groups** configurados
- ✅ **IAM Roles** e policies necessárias

## ⚡ Quick Start

```bash
# 1. Configurar credenciais AWS
aws configure

# 2. Copiar e editar variáveis
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars

# 3. Definir senha do banco
export TF_VAR_db_password="SuaSenhaSuperSegura123!"

# 4. Inicializar Terraform
terraform init

# 5. Planejar
terraform plan

# 6. Aplicar (leva ~15-20 min)
terraform apply

# 7. Configurar kubectl
aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1

# 8. Verificar
kubectl get nodes
```

## 💰 Custos Estimados

- **EKS:** ~$73/mês
- **EC2 (2x t3.medium):** ~$60/mês  
- **RDS (t3.micro):** ~$15/mês
- **NAT Gateway:** ~$45/mês
- **Total:** ~$193/mês

## 🔐 Segurança

### Senha do Banco

**NUNCA** coloque a senha no arquivo `.tfvars`!

```bash
# Opção 1: Variável de ambiente
export TF_VAR_db_password="senha-segura"

# Opção 2: Prompt interativo
terraform apply -var="db_password=$(read -sp 'DB Password: '; echo $REPLY)"

# Opção 3: AWS Secrets Manager (recomendado para produção)
```

## 📊 Arquitetura

```
Internet
   ↓
Internet Gateway
   ↓
Public Subnets (2 AZs)
   ├─ NAT Gateways
   └─ Load Balancers
   ↓
Private Subnets (2 AZs)
   └─ EKS Nodes (t3.medium)
   ↓
Database Subnets (2 AZs)
   └─ RDS PostgreSQL (t3.micro)
```

## 🔧 Personalização

Edite `terraform.tfvars`:

```hcl
# Produção: mais recursos
node_instance_types = ["t3.large"]
node_desired_size   = 3
db_instance_class   = "db.t3.small"
db_multi_az         = true

# Desenvolvimento: econômico
node_instance_types = ["t3.small"]
node_desired_size   = 1
db_instance_class   = "db.t3.micro"
db_multi_az         = false
```

## 📤 Outputs Úteis

```bash
# Ver todos os outputs
terraform output

# Endpoint do banco
terraform output db_instance_endpoint

# Comando kubectl
terraform output kubectl_config_command

# String de conexão
terraform output -raw db_connection_string
```

## 🗑️ Destruir Infraestrutura

```bash
# ATENÇÃO: Isso deleta TUDO!

# 1. Deletar aplicação primeiro
kubectl delete namespace tech-challenge

# 2. Aguarde LoadBalancers serem removidos (2-3 min)

# 3. Destruir infra
terraform destroy

# 4. Confirme com "yes"
```

## 🐛 Troubleshooting

### Erro: "Error creating EKS Cluster"

```bash
# Verifique permissões IAM
aws sts get-caller-identity

# Usuário precisa de:
# - AmazonEKSClusterPolicy
# - AmazonEKSServicePolicy
```

### Erro: "Cannot create VPC"

```bash
# Verifique limites de VPC
aws ec2 describe-account-attributes --attribute-names max-vpcs
```

### Erro: "DB instance already exists"

```bash
# Importar DB existente
terraform import aws_db_instance.postgresql tech-challenge-db
```

## 📚 Documentação

- [AWS EKS](https://docs.aws.amazon.com/eks/)
- [AWS RDS](https://docs.aws.amazon.com/rds/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)


