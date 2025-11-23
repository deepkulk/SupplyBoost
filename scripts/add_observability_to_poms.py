#!/usr/bin/env python3

import os

SERVICES = [
    "shopping-cart-service",
    "order-management-service",
    "inventory-service",
    "payment-service",
    "shipping-service",
    "notification-service",
    "accounting-service"
]

BASE_DIR = "/home/user/SupplyBoost/services"

OBSERVABILITY_DEPS = """
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
"""

def add_observability_to_pom(service_name):
    pom_path = os.path.join(BASE_DIR, service_name, "pom.xml")

    if not os.path.exists(pom_path):
        print(f"  ✗ POM not found: {service_name}")
        return False

    with open(pom_path, 'r') as f:
        content = f.read()

    # Check if already has observability
    if "micrometer-registry-prometheus" in content:
        print(f"  - Skipped {service_name} (already has observability deps)")
        return False

    # Find <!-- Testing --> and insert before it
    if "<!-- Testing -->" in content:
        content = content.replace("        <!-- Testing -->", OBSERVABILITY_DEPS + "\n        <!-- Testing -->")

        with open(pom_path, 'w') as f:
            f.write(content)

        print(f"  ✓ Added to {service_name}")
        return True
    else:
        print(f"  ✗ Could not find Testing section in {service_name}")
        return False

print("Adding observability dependencies to services...")
for service in SERVICES:
    add_observability_to_pom(service)

print("\nDone!")
