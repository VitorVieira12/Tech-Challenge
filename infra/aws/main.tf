# ============================================================================
# TECH CHALLENGE - INFRAESTRUTURA AWS
# ============================================================================
#
# Este arquivo define a infraestrutura principal para o Tech Challenge na AWS:
# - VPC com subnets públicas, privadas e de banco de dados
# - Cluster EKS (Elastic Kubernetes Service)
# - RDS PostgreSQL
# - Security Groups
# - IAM Roles e Policies
#
# ============================================================================

# ============================================================================
# DATA SOURCES
# ============================================================================

data "aws_caller_identity" "current" {}

data "aws_availability_zones" "available" {
  state = "available"
}

# ============================================================================
# LOCALS
# ============================================================================

locals {
  cluster_name = "${var.project_name}-${var.environment}-eks"
  
  common_tags = merge(
    var.tags,
    {
      Environment = var.environment
      Terraform   = "true"
    }
  )
}

# ============================================================================
# MÓDULOS
# ============================================================================

# VPC e Networking são definidos em vpc.tf
# EKS é definido em eks.tf
# RDS é definido em rds.tf

