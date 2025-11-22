#!/usr/bin/env python3

import os
import re

SERVICES = [
    "product-catalog-service",
    "shopping-cart-service",
    "order-management-service",
    "inventory-service",
    "payment-service",
    "shipping-service",
    "notification-service",
    "accounting-service"
]

BASE_DIR = "/home/user/SupplyBoost/services"

# The text to replace
OLD_OBSERVABILITY = """        <!-- Observability -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>"""

NEW_OBSERVABILITY = """        <!-- Observability -->
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
        </dependency>"""

def update_pom(service_name):
    pom_path = os.path.join(BASE_DIR, service_name, "pom.xml")

    if not os.path.exists(pom_path):
        print(f"  ✗ POM not found: {pom_path}")
        return False

    with open(pom_path, 'r') as f:
        content = f.read()

    if "spring-cloud-starter-sleuth" in content:
        content = content.replace(OLD_OBSERVABILITY, NEW_OBSERVABILITY)

        with open(pom_path, 'w') as f:
            f.write(content)

        print(f"  ✓ Updated {service_name}")
        return True
    else:
        print(f"  - Skipped {service_name} (already updated or different structure)")
        return False

print("Updating all service POMs...")
for service in SERVICES:
    print(f"Processing {service}...")
    update_pom(service)

print("\nDone!")
