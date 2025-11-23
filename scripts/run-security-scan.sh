#!/bin/bash

###################################################
# Security Scanning Script - Trivy + OWASP
###################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}  SupplyBoost Security Scan${NC}"
echo -e "${GREEN}=====================================${NC}\n"

# Check if trivy is installed
if ! command -v trivy &> /dev/null; then
  echo -e "${YELLOW}Trivy not found. Installing...${NC}"
  # Install trivy (adjust for your OS)
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo apt-key add -
    echo "deb https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
    sudo apt-get update
    sudo apt-get install trivy
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    brew install aquasecurity/trivy/trivy
  fi
fi

# Scan Docker images
echo -e "\n${GREEN}Scanning Docker images with Trivy...${NC}\n"

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

for service in "${SERVICES[@]}"; do
  IMAGE="supplyboost/$service:0.1.0-SNAPSHOT"
  echo -e "${GREEN}Scanning $IMAGE...${NC}"

  if docker image inspect "$IMAGE" &> /dev/null; then
    trivy image --severity HIGH,CRITICAL "$IMAGE"
  else
    echo -e "${YELLOW}Image $IMAGE not found. Skipping.${NC}"
  fi
done

# Run OWASP Dependency Check
echo -e "\n${GREEN}Running OWASP Dependency Check...${NC}\n"
bash ./run-owasp-check.sh

echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}  Security Scan Complete${NC}"
echo -e "${GREEN}=====================================${NC}\n"
echo -e "${YELLOW}Review the generated reports for vulnerabilities.${NC}"
