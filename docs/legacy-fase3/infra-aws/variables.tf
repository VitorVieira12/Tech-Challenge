# ============================================================================
# VARIÁVEIS GERAIS
# ============================================================================

variable "project_name" {
  description = "Nome do projeto (usado em tags e nomes de recursos)"
  type        = string
  default     = "tech-challenge"
}

variable "environment" {
  description = "Ambiente (dev, staging, production)"
  type        = string
  default     = "production"
}

variable "aws_region" {
  description = "Região AWS onde os recursos serão criados"
  type        = string
  default     = "us-east-1"
}

variable "tags" {
  description = "Tags adicionais para todos os recursos"
  type        = map(string)
  default = {
    Project     = "tech-challenge"
    Environment = "production"
    ManagedBy   = "terraform"
  }
}

# ============================================================================
# VARIÁVEIS DE REDE (VPC)
# ============================================================================

variable "vpc_cidr" {
  description = "CIDR block para a VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Lista de availability zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks para subnets privadas"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks para subnets públicas"
  type        = list(string)
  default     = ["10.0.101.0/24", "10.0.102.0/24"]
}

variable "database_subnet_cidrs" {
  description = "CIDR blocks para subnets de banco de dados"
  type        = list(string)
  default     = ["10.0.201.0/24", "10.0.202.0/24"]
}

# ============================================================================
# VARIÁVEIS DO EKS (KUBERNETES)
# ============================================================================

variable "cluster_name" {
  description = "Nome do cluster EKS"
  type        = string
  default     = "tech-challenge-eks"
}

variable "cluster_version" {
  description = "Versão do Kubernetes"
  type        = string
  default     = "1.31"  # Atualizado para versão mais recente suportada
}

variable "node_instance_types" {
  description = "Tipos de instância EC2 para os nodes"
  type        = list(string)
  default     = ["t3.micro"]  # Free Tier: t2.micro ou t3.micro (750h/mês)
}

variable "node_desired_size" {
  description = "Número desejado de nodes"
  type        = number
  default     = 1  # Free Tier: 1 node (750h/mês)
}

variable "node_min_size" {
  description = "Número mínimo de nodes"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Número máximo de nodes"
  type        = number
  default     = 4
}

variable "node_disk_size" {
  description = "Tamanho do disco dos nodes em GB"
  type        = number
  default     = 20
}

# ============================================================================
# VARIÁVEIS DO RDS (POSTGRESQL)
# ============================================================================

variable "db_identifier" {
  description = "Identificador da instância RDS"
  type        = string
  default     = "tech-challenge-db"
}

variable "db_engine_version" {
  description = "Versão do PostgreSQL"
  type        = string
  default     = "15.7"  # Versão disponível mais recente
}

variable "db_instance_class" {
  description = "Classe da instância RDS"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Armazenamento alocado em GB"
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Armazenamento máximo para auto-scaling em GB"
  type        = number
  default     = 100
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "tech_challenge_db"
}

variable "db_username" {
  description = "Nome de usuário do banco de dados"
  type        = string
  default     = "tech_admin"
  sensitive   = true
}

variable "db_password" {
  description = "Senha do banco de dados (use variável de ambiente ou Secrets Manager)"
  type        = string
  sensitive   = true
}

variable "db_backup_retention_period" {
  description = "Período de retenção de backups em dias"
  type        = number
  default     = 0  # Free tier: 0 = sem backup automático (economizar custos)
}

variable "db_backup_window" {
  description = "Janela de backup preferencial (UTC)"
  type        = string
  default     = "03:00-04:00"
}

variable "db_maintenance_window" {
  description = "Janela de manutenção preferencial (UTC)"
  type        = string
  default     = "sun:04:00-sun:05:00"
}

variable "db_multi_az" {
  description = "Habilitar Multi-AZ para alta disponibilidade"
  type        = bool
  default     = false
}

variable "db_publicly_accessible" {
  description = "Tornar o banco de dados publicamente acessível"
  type        = bool
  default     = false
}

variable "db_skip_final_snapshot" {
  description = "Pular snapshot final ao deletar (apenas dev)"
  type        = bool
  default     = false
}

# ============================================================================
# VARIÁVEIS DE SEGURANÇA
# ============================================================================

variable "allowed_cidr_blocks" {
  description = "CIDR blocks permitidos para acesso ao banco de dados"
  type        = list(string)
  default     = ["10.0.0.0/16"]
}

variable "enable_deletion_protection" {
  description = "Habilitar proteção contra deleção acidental"
  type        = bool
  default     = true
}

