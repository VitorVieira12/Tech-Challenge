#!/bin/bash

###############################################################################
# Script de Limpeza - Tech Challenge Kubernetes
###############################################################################

set -e  # Exit on error

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Namespace
NAMESPACE="tech-challenge"

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

confirm() {
    read -p "$(echo -e ${YELLOW}⚠️  $1 [y/N]:${NC}) " response
    case "$response" in
        [yY][eE][sS]|[yY]) 
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

main() {
    echo ""
    log_warning "=== Tech Challenge - Limpeza Kubernetes ==="
    echo ""
    
    if ! confirm "Deseja realmente deletar TODOS os recursos do namespace $NAMESPACE?"; then
        log_info "Operação cancelada."
        exit 0
    fi
    
    log_info "Deletando recursos..."
    
    # Deletar HPA
    log_info "Deletando HPA..."
    kubectl delete -f hpa.yaml 2>/dev/null || log_warning "HPA não encontrado"
    
    # Deletar Ingress
    log_info "Deletando Ingress..."
    kubectl delete -f ingress.yaml 2>/dev/null || log_warning "Ingress não encontrado"
    
    # Deletar Aplicação
    log_info "Deletando aplicação..."
    kubectl delete -f app-service.yaml 2>/dev/null || log_warning "Service da aplicação não encontrado"
    kubectl delete -f app-deployment.yaml 2>/dev/null || log_warning "Deployment da aplicação não encontrado"
    
    # Deletar PostgreSQL
    log_info "Deletando PostgreSQL..."
    kubectl delete -f postgres-service.yaml 2>/dev/null || log_warning "Service do PostgreSQL não encontrado"
    kubectl delete -f postgres-deployment.yaml 2>/dev/null || log_warning "Deployment do PostgreSQL não encontrado"
    kubectl delete -f postgres-pvc.yaml 2>/dev/null || log_warning "PVC não encontrado"
    
    # Deletar ConfigMap e Secrets
    log_info "Deletando ConfigMaps e Secrets..."
    kubectl delete -f configmap.yaml 2>/dev/null || log_warning "ConfigMap não encontrado"
    kubectl delete -f secret.yaml 2>/dev/null || log_warning "Secret não encontrado"
    
    # Deletar Namespace
    if confirm "Deseja deletar o namespace $NAMESPACE (isso pode levar alguns minutos)?"; then
        log_info "Deletando namespace..."
        kubectl delete namespace $NAMESPACE 2>/dev/null || log_warning "Namespace não encontrado"
        log_success "Namespace deletado"
    else
        log_info "Namespace mantido"
    fi
    
    echo ""
    log_success "=== Limpeza concluída! ==="
    echo ""
}

main

