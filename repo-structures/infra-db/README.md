# tech-challenge-infra-db

Infraestrutura de Banco de Dados (RDS PostgreSQL) usando Terraform

## 📋 Descrição

Este repositório contém o código Terraform para provisionar:
- AWS RDS PostgreSQL 15
- VPC com subnets públicas e privadas
- Security Groups
- Backup policies
- CloudWatch Alarms
- Secrets Manager para credenciais

## 🚀 Tecnologias

- **IaC**: Terraform 1.6+
- **Cloud**: AWS
- **Database**: RDS PostgreSQL 15.4
- **Networking**: VPC, Subnets, NAT Gateway
- **Monitoring**: CloudWatch
- **Secrets**: AWS Secrets Manager

## 📦 Estrutura

```
infra-db/
├── terraform/
│   ├── main.tf              # Provider e backend
│   ├── variables.tf         # Variáveis
│   ├── vpc.tf               # VPC e networking
│   ├── rds.tf               # RDS PostgreSQL
│   ├── outputs.tf           # Outputs
│   └── terraform.tfvars.example
├── .github/workflows/
│   └── terraform.yml        # CI/CD pipeline
├── README.md
└── .gitignore
```

## 🛠️ Setup Local

### Pré-requisitos

- Terraform 1.6+
- AWS CLI configurado
- Credenciais AWS com permissões:
  - VPC (full)
  - RDS (full)
  - Secrets Manager (full)
  - CloudWatch (read/write)

### Instalação

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/tech-challenge-infra-db.git
cd tech-challenge-infra-db/terraform

# Copiar terraform.tfvars
cp terraform.tfvars.example terraform.tfvars

# Editar com seus valores
nano terraform.tfvars
```

### Configuração

Edite `terraform.tfvars`:

```hcl
aws_region = "us-east-1"
db_username = "admin"
db_password = "SuaSenhaForteAqui123!"
environment = "production"
```

### Deploy

```bash
# Inicializar Terraform
terraform init

# Validar configuração
terraform validate

# Ver plano de execução
terraform plan

# Aplicar mudanças
terraform apply
```

## 📊 Recursos Provisionados

### VPC
- CIDR: 10.0.0.0/16
- 2 subnets públicas (para NAT)
- 2 subnets privadas (para RDS)
- Internet Gateway
- NAT Gateway
- Route Tables

### RDS PostgreSQL
- **Engine**: PostgreSQL 15.4
- **Instance Class**: db.t3.micro (Free Tier)
- **Storage**: 20GB (auto-scaling até 40GB)
- **Multi-AZ**: Desabilitado (dev), Habilitado (prod)
- **Backup**: 7 dias de retenção
- **Encryption**: Habilitado
- **Logs**: postgresql, upgrade

### Security
- Security Group permitindo PostgreSQL (5432) apenas da VPC
- Credenciais armazenadas no Secrets Manager
- Encryption at rest habilitado

### Monitoring
- **CPU Utilization** > 80%
- **Free Storage Space** < 5GB
- **Database Connections** > 80

## 🔐 Secrets

### GitHub Secrets Necessários

```bash
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
DB_USERNAME=admin
DB_PASSWORD=SuaSenhaForteAqui123!
```

### Secrets Manager

Após deploy, as credenciais ficam em:
```
AWS Secrets Manager → tech-challenge-db-credentials
```

Acessar via CLI:
```bash
aws secretsmanager get-secret-value \
  --secret-id tech-challenge-db-credentials \
  --query SecretString \
  --output text | jq
```

## 📡 Outputs

Após deploy, os seguintes outputs estarão disponíveis:

```bash
terraform output

# RDS Endpoint
rds_endpoint = "tech-challenge-db.xxxxx.us-east-1.rds.amazonaws.com:5432"

# VPC ID
vpc_id = "vpc-xxxxx"

# Connection String
terraform output -raw connection_string
```

## 🔄 CI/CD Pipeline

O pipeline GitHub Actions executa:

### Pull Request
1. Terraform Format Check
2. Terraform Init
3. Terraform Validate
4. Terraform Plan
5. Comentário no PR com o plano

### Push to Main
1. Terraform Format Check
2. Terraform Init
3. Terraform Validate
4. Terraform Plan
5. **Terraform Apply** (automático)
6. Notificação de sucesso

## 🧪 Testes

### Validar Terraform

```bash
terraform fmt -check
terraform validate
terraform plan
```

### Testar Conexão com RDS

```bash
# Após deploy
export DB_HOST=$(terraform output -raw rds_address)
export DB_PORT=$(terraform output -raw rds_port)
export DB_NAME=$(terraform output -raw rds_database_name)

# Testar conexão (de dentro da VPC ou via SSH tunnel)
psql -h $DB_HOST -p $DB_PORT -U admin -d $DB_NAME
```

## 📈 Custos Estimados

### Free Tier (12 meses)
- RDS db.t3.micro: 750 horas/mês (GRÁTIS)
- 20 GB storage (GRÁTIS)
- 20 GB backups (GRÁTIS)

### Após Free Tier
- **db.t3.micro**: ~$15/mês
- **NAT Gateway**: ~$30/mês
- **Storage**: ~$2.30/mês (20GB)
- **Total**: ~$47/mês

💡 **Dica**: Destruir recursos após demonstração!

## 🗑️ Destruir Recursos

```bash
# Destruir tudo
terraform destroy

# Confirmar com 'yes'
```

⚠️ **ATENÇÃO**: Isso deleta o banco permanentemente!

## 🔧 Troubleshooting

### Erro: "Subnet group doesn't meet availability zone coverage requirement"

**Solução**: Certifique-se de ter subnets em pelo menos 2 AZs diferentes.

### Erro: "Insufficient permissions"

**Solução**: Verifique se sua IAM role tem as permissões necessárias.

### Erro: "Terraform state locked"

**Solução**:
```bash
terraform force-unlock LOCK_ID
```

### Conexão RDS timeout

**Problemas possíveis**:
1. Security Group não permite seu IP
2. RDS em subnet privada sem VPN/Bastion
3. Network ACLs bloqueando tráfego

**Solução**:
- Usar bastion host ou AWS SSM Session Manager
- Verificar Security Groups
- Usar Lambda para testar conectividade

## 📚 Referências

- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [PostgreSQL 15 Release Notes](https://www.postgresql.org/docs/15/release-15.html)

## 👥 Autores

Tech Challenge Team - Fase 3

## 📝 Licença

MIT

---

**Banco de dados provisionado com segurança e best practices!** 🗄️

