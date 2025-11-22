#!/bin/bash

#############################################
# SupplyBoost Kubernetes Deployment Script
#############################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="supplyboost"
K8S_DIR="../infrastructure/kubernetes"
SERVICES=(
  "identity-service"
  "product-catalog-service"
  "shopping-cart-service"
  "order-management-service"
  "inventory-service"
  "payment-service"
  "shipping-service"
  "notification-service"
  "accounting-service"
)

echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}  SupplyBoost Kubernetes Deployment${NC}"
echo -e "${GREEN}=====================================${NC}\n"

# Function to print colored messages
print_info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
  print_error "kubectl is not installed. Please install kubectl first."
  exit 1
fi

# Check if we can connect to the cluster
if ! kubectl cluster-info &> /dev/null; then
  print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
  exit 1
fi

print_info "Connected to Kubernetes cluster"

# Create namespace if it doesn't exist
print_info "Creating namespace: $NAMESPACE"
kubectl apply -f "$K8S_DIR/namespace.yml"

# Apply shared resources
print_info "Applying shared ConfigMap..."
kubectl apply -f "$K8S_DIR/shared-configmap.yml"

# Deploy infrastructure services (PostgreSQL, Kafka, Redis, etc.)
print_info "Deploying infrastructure services..."
if [ -d "$K8S_DIR/infrastructure" ]; then
  kubectl apply -f "$K8S_DIR/infrastructure/"
else
  print_warn "Infrastructure directory not found. Skipping infrastructure deployment."
fi

# Deploy each microservice
print_info "Deploying microservices..."
for service in "${SERVICES[@]}"; do
  SERVICE_DIR="$K8S_DIR/$service"

  if [ -d "$SERVICE_DIR" ]; then
    print_info "Deploying $service..."

    # Apply in order: ConfigMap, Secret, Deployment, Service, HPA
    [ -f "$SERVICE_DIR/configmap.yml" ] && kubectl apply -f "$SERVICE_DIR/configmap.yml"
    [ -f "$SERVICE_DIR/secret.yml" ] && kubectl apply -f "$SERVICE_DIR/secret.yml"
    [ -f "$SERVICE_DIR/deployment.yml" ] && kubectl apply -f "$SERVICE_DIR/deployment.yml"
    [ -f "$SERVICE_DIR/service.yml" ] && kubectl apply -f "$SERVICE_DIR/service.yml"
    [ -f "$SERVICE_DIR/hpa.yml" ] && kubectl apply -f "$SERVICE_DIR/hpa.yml"

    print_info "$service deployed successfully"
  else
    print_warn "Kubernetes manifests not found for $service. Skipping."
  fi
done

# Apply Ingress
print_info "Applying Ingress configuration..."
kubectl apply -f "$K8S_DIR/ingress.yml"

# Wait for deployments to be ready
print_info "Waiting for deployments to be ready..."
for service in "${SERVICES[@]}"; do
  if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
    print_info "Waiting for $service..."
    kubectl wait --for=condition=available --timeout=300s deployment/"$service" -n "$NAMESPACE" || true
  fi
done

# Display deployment status
echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}       Deployment Status${NC}"
echo -e "${GREEN}=====================================${NC}\n"

kubectl get pods -n "$NAMESPACE"
echo ""
kubectl get svc -n "$NAMESPACE"
echo ""
kubectl get ingress -n "$NAMESPACE"

echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}  Deployment completed successfully!${NC}"
echo -e "${GREEN}=====================================${NC}\n"

print_info "You can access the application at: https://supplyboost.example.com"
print_info "Monitor with: kubectl get pods -n $NAMESPACE --watch"
print_info "View logs with: kubectl logs -f deployment/<service-name> -n $NAMESPACE"

exit 0
