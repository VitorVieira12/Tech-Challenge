#!/bin/bash

###############################################################################
# Script de Deploy Automatizado - Tech Challenge Kubernetes
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

# Funções auxiliares
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

# Verificar pré-requisitos
check_prerequisites() {
    log_info "Verificando pré-requisitos..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl não encontrado. Por favor, instale o kubectl."
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Não foi possível conectar ao cluster Kubernetes."
        exit 1
    fi
    
    log_success "Pré-requisitos verificados"
}

# Criar namespace
create_namespace() {
    log_info "Criando namespace..."
    kubectl apply -f namespace.yaml
    log_success "Namespace criado/atualizado"
}

# Aplicar ConfigMap e Secrets
apply_configs() {
    log_info "Aplicando ConfigMaps e Secrets..."
    kubectl apply -f configmap.yaml
    kubectl apply -f secret.yaml
    log_success "ConfigMaps e Secrets aplicados"
}

# Deploy PostgreSQL
deploy_postgres() {
    log_info "Fazendo deploy do PostgreSQL..."
    kubectl apply -f postgres-pvc.yaml
    kubectl apply -f postgres-deployment.yaml
    kubectl apply -f postgres-service.yaml
    
    log_info "Aguardando PostgreSQL ficar pronto..."
    kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=180s
    log_success "PostgreSQL está pronto"
}

# Deploy da Aplicação
deploy_app() {
    log_info "Fazendo deploy da aplicação..."
    kubectl apply -f app-deployment.yaml
    kubectl apply -f app-service.yaml
    
    log_info "Aguardando pods da aplicação ficarem prontos..."
    kubectl wait --for=condition=ready pod -l app=tech-challenge -n $NAMESPACE --timeout=180s
    log_success "Aplicação está pronta"
}

# Aplicar HPA
apply_hpa() {
    log_info "Aplicando Horizontal Pod Autoscaler..."
    kubectl apply -f hpa.yaml
    log_success "HPA configurado"
}

# Aplicar Ingress (opcional)
apply_ingress() {
    if [ -f ingress.yaml ]; then
        log_info "Aplicando Ingress..."
        kubectl apply -f ingress.yaml
        log_success "Ingress configurado"
    else
        log_warning "Arquivo ingress.yaml não encontrado, pulando..."
    fi
}

# Mostrar status
show_status() {
    log_info "Status do deployment:"
    echo ""
    
    log_info "Pods:"
    kubectl get pods -n $NAMESPACE
    echo ""
    
    log_info "Services:"
    kubectl get svc -n $NAMESPACE
    echo ""
    
    log_info "HPA:"
    kubectl get hpa -n $NAMESPACE
    echo ""
    
    log_info "Ingress:"
    kubectl get ingress -n $NAMESPACE 2>/dev/null || log_warning "Nenhum Ingress configurado"
}

# Obter URL da aplicação
get_app_url() {
    log_info "Obtendo URL da aplicação..."
    
    # Verificar se é Minikube
    if command -v minikube &> /dev/null && minikube status &> /dev/null; then
        URL=$(minikube service tech-challenge-service -n $NAMESPACE --url)
        log_success "URL da aplicação (Minikube): $URL"
    else
        # Aguardar LoadBalancer obter IP externo
        log_info "Aguardando LoadBalancer obter IP externo..."
        EXTERNAL_IP=""
        for i in {1..30}; do
            EXTERNAL_IP=$(kubectl get svc tech-challenge-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
            if [ -n "$EXTERNAL_IP" ]; then
                break
            fi
            sleep 2
        done
        
        if [ -n "$EXTERNAL_IP" ]; then
            log_success "URL da aplicação: http://$EXTERNAL_IP"
        else
            log_warning "LoadBalancer ainda não obteve IP externo. Execute: kubectl get svc -n $NAMESPACE"
        fi
    fi
}

# Função principal
main() {
    echo ""
    log_info "=== Tech Challenge - Deploy Kubernetes ==="
    echo ""
    
    check_prerequisites
    create_namespace
    apply_configs
    deploy_postgres
    deploy_app
    apply_hpa
    apply_ingress
    
    echo ""
    log_success "=== Deploy concluído com sucesso! ==="
    echo ""
    
    show_status
    get_app_url
    
    echo ""
    log_info "Para acompanhar os logs:"
    echo "  kubectl logs -f deployment/tech-challenge-app -n $NAMESPACE"
    echo ""
    log_info "Para acompanhar o HPA:"
    echo "  kubectl get hpa -n $NAMESPACE -w"
    echo ""
}

# Executar script
main

