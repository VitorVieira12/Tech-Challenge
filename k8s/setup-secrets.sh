#!/bin/bash
# Run this script after EKS cluster is ready
# Replace the values below with your actual credentials

RDS_ENDPOINT="tech-challenge-db.cgxmk6mmaeyg.us-east-1.rds.amazonaws.com"
# Already configured - just update DB_PASSWORD, MONGODB_URI and MERCADOPAGO_TOKEN below
DB_PASSWORD="<YOUR_DB_PASSWORD>"
MONGODB_URI="<YOUR_MONGODB_URI>"
MERCADOPAGO_TOKEN="<YOUR_MERCADOPAGO_TOKEN>"
JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
RABBITMQ_HOST="rabbitmq.rabbitmq.svc.cluster.local"

echo "Creating namespaces..."
kubectl create namespace rabbitmq --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace os-service --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace billing-service --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace execution-service --dry-run=client -o yaml | kubectl apply -f -

echo "Creating OS Service secrets..."
kubectl create secret generic os-service-secrets \
  --namespace os-service \
  --from-literal=db-url="jdbc:postgresql://${RDS_ENDPOINT}:5432/techdb" \
  --from-literal=db-username="postgres" \
  --from-literal=db-password="${DB_PASSWORD}" \
  --from-literal=jwt-secret="${JWT_SECRET}" \
  --from-literal=rabbitmq-host="${RABBITMQ_HOST}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Creating Billing Service secrets..."
kubectl create secret generic billing-service-secrets \
  --namespace billing-service \
  --from-literal=db-url="jdbc:postgresql://${RDS_ENDPOINT}:5432/billing_service" \
  --from-literal=db-username="postgres" \
  --from-literal=db-password="${DB_PASSWORD}" \
  --from-literal=rabbitmq-host="${RABBITMQ_HOST}" \
  --from-literal=mercadopago-token="${MERCADOPAGO_TOKEN}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Creating Execution Service secrets..."
kubectl create secret generic execution-service-secrets \
  --namespace execution-service \
  --from-literal=mongodb-uri="${MONGODB_URI}" \
  --from-literal=rabbitmq-host="${RABBITMQ_HOST}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Creating RabbitMQ secret..."
kubectl create secret generic rabbitmq-secret \
  --namespace rabbitmq \
  --from-literal=username="guest" \
  --from-literal=password="guest" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "All secrets created!"
