#!/bin/bash

# ============================================================================
# SCRIPT DE SETUP CI/CD - Tech Challenge
# ============================================================================
# Este script ajuda a configurar o ambiente AWS para CI/CD
# ============================================================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções auxiliares
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_header() {
    echo ""
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo ""
}

# Verificar se AWS CLI está instalado
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI não encontrado!"
        print_info "Instale em: https://aws.amazon.com/cli/"
        exit 1
    fi
    print_success "AWS CLI instalado: $(aws --version)"
}

# Verificar se kubectl está instalado
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_warning "kubectl não encontrado. Será necessário para verificar o cluster."
        return 1
    fi
    print_success "kubectl instalado: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
    return 0
}

# Verificar credenciais AWS
check_aws_credentials() {
    print_info "Verificando credenciais AWS..."
    
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "Credenciais AWS inválidas ou não configuradas!"
        print_info "Execute: aws configure"
        exit 1
    fi
    
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    USER_ARN=$(aws sts get-caller-identity --query Arn --output text)
    
    print_success "Autenticado na AWS"
    print_info "Account ID: $ACCOUNT_ID"
    print_info "User: $USER_ARN"
}

# Criar ECR repository se não existir
create_ecr_repository() {
    print_info "Verificando ECR repository..."
    
    REPO_NAME="tech-challenge"
    REGION="us-east-1"
    
    if aws ecr describe-repositories --repository-names $REPO_NAME --region $REGION &> /dev/null; then
        print_success "ECR repository já existe"
    else
        print_info "Criando ECR repository..."
        aws ecr create-repository \
            --repository-name $REPO_NAME \
            --region $REGION \
            --image-scanning-configuration scanOnPush=true \
            --encryption-configuration encryptionType=AES256
        
        print_success "ECR repository criado: $REPO_NAME"
    fi
    
    REPO_URI=$(aws ecr describe-repositories --repository-names $REPO_NAME --region $REGION --query 'repositories[0].repositoryUri' --output text)
    print_info "Repository URI: $REPO_URI"
}

# Listar informações necessárias para GitHub Secrets
print_github_secrets() {
    print_header "SECRETS PARA CONFIGURAR NO GITHUB"
    
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    
    echo "Adicione os seguintes secrets no GitHub:"
    echo "(Settings > Secrets and variables > Actions > New repository secret)"
    echo ""
    echo "1. AWS_ACCESS_KEY_ID"
    echo "   Value: [Sua Access Key ID]"
    echo ""
    echo "2. AWS_SECRET_ACCESS_KEY"
    echo "   Value: [Sua Secret Access Key]"
    echo ""
    echo "3. AWS_ACCOUNT_ID"
    echo "   Value: $ACCOUNT_ID"
    echo ""
    echo "4. DB_USERNAME"
    echo "   Value: tech_admin"
    echo ""
    echo "5. DB_PASSWORD"
    echo "   Value: [Crie uma senha forte]"
    echo ""
    echo "6. JWT_SECRET"
    echo "   Value: [Gere uma chave aleatória]"
    echo ""
    echo "7. ADMIN_USERNAME"
    echo "   Value: admin"
    echo ""
    echo "8. ADMIN_PASSWORD"
    echo "   Value: [Crie uma senha forte]"
    echo ""
    
    print_info "Após configurar, continue com o deploy da infraestrutura."
}

# Verificar se cluster EKS existe
check_eks_cluster() {
    print_info "Verificando cluster EKS..."
    
    CLUSTER_NAME="tech-challenge-eks"
    REGION="us-east-1"
    
    if aws eks describe-cluster --name $CLUSTER_NAME --region $REGION &> /dev/null; then
        print_success "Cluster EKS existe: $CLUSTER_NAME"
        
        # Configurar kubectl
        if check_kubectl; then
            print_info "Configurando kubectl..."
            aws eks update-kubeconfig --name $CLUSTER_NAME --region $REGION
            print_success "kubectl configurado"
            
            print_info "Verificando nodes..."
            kubectl get nodes
        fi
    else
        print_warning "Cluster EKS não encontrado"
        print_info "Execute primeiro: cd infra/aws && terraform apply"
    fi
}

# Função principal
main() {
    print_header "TECH CHALLENGE - CI/CD SETUP"
    
    # 1. Verificar ferramentas
    print_header "1. Verificando Ferramentas"
    check_aws_cli
    check_kubectl
    
    # 2. Verificar credenciais
    print_header "2. Verificando Credenciais AWS"
    check_aws_credentials
    
    # 3. Criar ECR repository
    print_header "3. Configurando ECR"
    create_ecr_repository
    
    # 4. Verificar cluster EKS
    print_header "4. Verificando Cluster EKS"
    check_eks_cluster
    
    # 5. Mostrar secrets necessários
    print_github_secrets
    
    # Conclusão
    print_header "SETUP CONCLUÍDO"
    print_success "Ambiente AWS configurado!"
    print_info ""
    print_info "Próximos passos:"
    print_info "1. Configure os secrets no GitHub (veja lista acima)"
    print_info "2. Se ainda não fez, provisione a infraestrutura:"
    print_info "   cd infra/aws && terraform apply"
    print_info "3. Faça um push para main para testar a pipeline:"
    print_info "   git add . && git commit -m 'test: CI/CD' && git push origin main"
    print_info "4. Acompanhe em: https://github.com/seu-usuario/Tech-Challenge/actions"
    echo ""
}

# Executar
main



