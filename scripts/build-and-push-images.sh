#!/bin/bash

#############################################
# Build and Push Docker Images
#############################################

set -e

# Configuration
REGISTRY="docker.io/supplyboost"
VERSION="0.1.0-SNAPSHOT"

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

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}Building and pushing Docker images...${NC}\n"

# Build all services
cd ..
echo "Building all services with Maven..."
mvn clean package -DskipTests

# Build and push each service image
for service in "${SERVICES[@]}"; do
  echo -e "\n${GREEN}Building Docker image for $service...${NC}"

  cd "services/$service"

  # Build image
  docker build -t "$REGISTRY/$service:$VERSION" -t "$REGISTRY/$service:latest" .

  # Push image (optional - uncomment if you want to push)
  # echo -e "${GREEN}Pushing $service to registry...${NC}"
  # docker push "$REGISTRY/$service:$VERSION"
  # docker push "$REGISTRY/$service:latest"

  cd ../..

  echo -e "${GREEN}✓ $service image built${NC}"
done

echo -e "\n${GREEN}All images built successfully!${NC}"
echo -e "\nTo push images to registry, run:"
echo -e "  docker login"
for service in "${SERVICES[@]}"; do
  echo -e "  docker push $REGISTRY/$service:$VERSION"
done
