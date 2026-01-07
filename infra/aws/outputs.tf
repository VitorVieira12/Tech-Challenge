# ============================================================================
# OUTPUTS
# ============================================================================

# VPC Outputs
output "vpc_id" {
  description = "ID da VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "CIDR block da VPC"
  value       = aws_vpc.main.cidr_block
}

output "private_subnet_ids" {
  description = "IDs das subnets privadas"
  value       = aws_subnet.private[*].id
}

output "public_subnet_ids" {
  description = "IDs das subnets públicas"
  value       = aws_subnet.public[*].id
}

output "database_subnet_ids" {
  description = "IDs das subnets de banco de dados"
  value       = aws_subnet.database[*].id
}

# EKS Outputs
output "cluster_id" {
  description = "ID do cluster EKS"
  value       = module.eks.cluster_id
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Endpoint do cluster EKS"
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "Security group do cluster EKS"
  value       = module.eks.cluster_security_group_id
}

output "cluster_certificate_authority_data" {
  description = "Certificate authority data do cluster"
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "cluster_oidc_issuer_url" {
  description = "OIDC issuer URL do cluster"
  value       = module.eks.cluster_oidc_issuer_url
}

output "kubectl_config_command" {
  description = "Comando para configurar kubectl"
  value       = "aws eks update-kubeconfig --name ${module.eks.cluster_name} --region ${var.aws_region}"
}

# RDS Outputs
output "db_instance_endpoint" {
  description = "Endpoint da instância RDS"
  value       = aws_db_instance.postgresql.endpoint
}

output "db_instance_address" {
  description = "Endereço da instância RDS"
  value       = aws_db_instance.postgresql.address
}

output "db_instance_port" {
  description = "Porta da instância RDS"
  value       = aws_db_instance.postgresql.port
}

output "db_instance_name" {
  description = "Nome do banco de dados"
  value       = aws_db_instance.postgresql.db_name
}

output "db_instance_username" {
  description = "Username do banco de dados"
  value       = aws_db_instance.postgresql.username
  sensitive   = true
}

output "db_security_group_id" {
  description = "Security group do RDS"
  value       = aws_security_group.rds.id
}

# Connection String
output "db_connection_string" {
  description = "String de conexão JDBC para a aplicação"
  value       = "jdbc:postgresql://${aws_db_instance.postgresql.address}:${aws_db_instance.postgresql.port}/${aws_db_instance.postgresql.db_name}"
  sensitive   = true
}

# Kubeconfig
output "kubeconfig" {
  description = "Configuração do kubectl para conectar ao cluster"
  value = templatefile("${path.module}/kubeconfig.tpl", {
    cluster_name        = module.eks.cluster_name
    cluster_endpoint    = module.eks.cluster_endpoint
    cluster_ca          = module.eks.cluster_certificate_authority_data
    aws_region          = var.aws_region
  })
  sensitive = true
}

# Summary
output "deployment_summary" {
  description = "Resumo do deployment"
  value = <<-EOT
  
  ========================================
  🎉 INFRAESTRUTURA PROVISIONADA COM SUCESSO!
  ========================================
  
  📦 REGIÃO: ${var.aws_region}
  🏷️  AMBIENTE: ${var.environment}
  
  ☸️  KUBERNETES (EKS):
     Nome: ${module.eks.cluster_name}
     Endpoint: ${module.eks.cluster_endpoint}
     Versão: ${var.cluster_version}
     Nodes: ${var.node_desired_size} (min: ${var.node_min_size}, max: ${var.node_max_size})
  
  🗄️  BANCO DE DADOS (RDS PostgreSQL):
     Endpoint: ${aws_db_instance.postgresql.address}:${aws_db_instance.postgresql.port}
     Database: ${aws_db_instance.postgresql.db_name}
     Versão: ${var.db_engine_version}
     Instância: ${var.db_instance_class}
  
  🔧 PRÓXIMOS PASSOS:
  
  1. Configurar kubectl:
     $ aws eks update-kubeconfig --name ${module.eks.cluster_name} --region ${var.aws_region}
  
  2. Verificar nodes:
     $ kubectl get nodes
  
  3. Criar namespace da aplicação:
     $ kubectl create namespace tech-challenge
  
  4. Criar secret do banco de dados:
     $ kubectl create secret generic app-secrets -n tech-challenge \
       --from-literal=DB_USERNAME=${aws_db_instance.postgresql.username} \
       --from-literal=DB_PASSWORD=<sua-senha> \
       --from-literal=DB_HOST=${aws_db_instance.postgresql.address}
  
  5. Fazer deploy da aplicação:
     $ kubectl apply -f ../../k8s/
  
  ========================================
  EOT
}

