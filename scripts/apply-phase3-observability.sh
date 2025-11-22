#!/bin/bash

# Script to apply Phase 3 observability configurations to all services

SERVICES=(
  "product-catalog-service"
  "shopping-cart-service"
  "order-management-service"
  "inventory-service"
  "payment-service"
  "shipping-service"
  "notification-service"
  "accounting-service"
)

BASE_DIR="/home/user/SupplyBoost/services"
LOGBACK_SOURCE="$BASE_DIR/identity-service/src/main/resources/logback-spring.xml"

echo "Applying Phase 3 observability configurations to all services..."

for service in "${SERVICES[@]}"; do
  echo "Processing $service..."

  SERVICE_DIR="$BASE_DIR/$service"
  RESOURCES_DIR="$SERVICE_DIR/src/main/resources"

  # Create resources directory if it doesn't exist
  mkdir -p "$RESOURCES_DIR"

  # Copy logback configuration
  if [ -f "$LOGBACK_SOURCE" ]; then
    cp "$LOGBACK_SOURCE" "$RESOURCES_DIR/logback-spring.xml"
    echo "  ✓ Copied logback-spring.xml"
  fi

done

echo "Done! All services updated."
