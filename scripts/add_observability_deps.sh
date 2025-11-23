#!/bin/bash

# Script to add observability dependencies to all services that don't have them

SERVICES=(
  "shopping-cart-service"
  "order-management-service"
  "inventory-service"
  "payment-service"
  "shipping-service"
  "notification-service"
  "accounting-service"
)

BASE_DIR="/home/user/SupplyBoost/services"

OBSERVABILITY_DEPS='
        <!-- Observability -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Distributed Tracing -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>

        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-otlp</artifactId>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
        </dependency>
'

echo "Adding observability dependencies to services..."

for service in "${SERVICES[@]}"; do
  POM="$BASE_DIR/$service/pom.xml"

  if [ ! -f "$POM" ]; then
    echo "  ✗ POM not found: $service"
    continue
  fi

  # Check if already has observability deps
  if grep -q "micrometer-registry-prometheus" "$POM"; then
    echo "  - Skipped $service (already has deps)"
    continue
  fi

  # Add before <!-- Testing --> comment
  if grep -q "<!-- Testing -->" "$POM"; then
    sed -i "/<!-- Testing -->/i\\$OBSERVABILITY_DEPS" "$POM"
    echo "  ✓ Added to $service"
  else
    echo "  ✗ Could not find Testing section in $service"
  fi
done

echo "Done!"
