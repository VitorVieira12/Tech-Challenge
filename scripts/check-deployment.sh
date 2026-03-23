#!/bin/bash

# ============================================================================
# SCRIPT DE VERIFICAÇÃO DE DEPLOYMENT - Tech Challenge
# ============================================================================
# Este script verifica o status do deployment no Kubernetes
# ============================================================================

set -e

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

NAMESPACE="tech-challenge"

print_header() {
    echo ""
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Verificar se kubectl está configurado
if ! kubectl cluster-info &> /dev/null; then
    print_error "kubectl não configurado ou cluster inacessível"
    print_info "Execute: aws eks update-kubeconfig --name tech-challenge-eks --region us-east-1"
    exit 1
fi

print_header "VERIFICAÇÃO DE DEPLOYMENT"

# 1. Verificar namespace
print_header "1. Namespace"
if kubectl get namespace $NAMESPACE &> /dev/null; then
    print_success "Namespace existe: $NAMESPACE"
else
    print_error "Namespace não encontrado: $NAMESPACE"
    exit 1
fi

# 2. Verificar Pods
print_header "2. Status dos Pods"
echo ""
kubectl get pods -n $NAMESPACE -o wide
echo ""

READY_PODS=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | grep -c "Running" || echo "0")
TOTAL_PODS=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | wc -l || echo "0")

if [ "$READY_PODS" -gt 0 ]; then
    print_success "$READY_PODS de $TOTAL_PODS pods rodando"
else
    print_error "Nenhum pod rodando!"
fi

# 3. Verificar Services
print_header "3. Services"
echo ""
kubectl get svc -n $NAMESPACE
echo ""

LOADBALANCER_URL=$(kubectl get svc tech-challenge-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")

if [ -n "$LOADBALANCER_URL" ]; then
    print_success "LoadBalancer provisionado"
    print_info "URL: http://$LOADBALANCER_URL"
else
    print_error "LoadBalancer ainda não provisionado"
fi

# 4. Verificar HPA
print_header "4. Horizontal Pod Autoscaler"
echo ""
kubectl get hpa -n $NAMESPACE 2>/dev/null || print_info "HPA não encontrado"
echo ""

# 5. Verificar ConfigMaps
print_header "5. ConfigMaps"
kubectl get configmap -n $NAMESPACE

# 6. Verificar Secrets
print_header "6. Secrets"
kubectl get secret -n $NAMESPACE

# 7. Eventos recentes
print_header "7. Eventos Recentes"
echo ""
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -10
echo ""

# 8. Logs da aplicação (últimas 20 linhas)
print_header "8. Logs da Aplicação (últimas 20 linhas)"
echo ""
kubectl logs -n $NAMESPACE -l app=tech-challenge --tail=20 --all-containers=true 2>/dev/null || print_info "Pods ainda não estão prontos"
echo ""

# 9. Health check
print_header "9. Health Check"
if [ -n "$LOADBALANCER_URL" ]; then
    print_info "Testando health endpoint..."
    if curl -s -o /dev/null -w "%{http_code}" "http://$LOADBALANCER_URL/actuator/health" | grep -q "200"; then
        print_success "Health check: OK"
        echo ""
        curl -s "http://$LOADBALANCER_URL/actuator/health" | jq . || cat
    else
        print_error "Health check falhou"
    fi
else
    print_info "Aguardando LoadBalancer para testar health check"
fi

# 10. Resumo
print_header "RESUMO"
echo ""
echo "Namespace: $NAMESPACE"
echo "Pods rodando: $READY_PODS de $TOTAL_PODS"
echo "URL da aplicação: ${LOADBALANCER_URL:-'Aguardando provisionamento'}"
echo ""

if [ "$READY_PODS" -gt 0 ] && [ -n "$LOADBALANCER_URL" ]; then
    print_success "Deployment OK! Aplicação rodando."
    echo ""
    print_info "Acesse a documentação da API:"
    echo "http://$LOADBALANCER_URL/swagger-ui.html"
else
    print_error "Deployment incompleto. Verifique os logs acima."
fi

echo ""


