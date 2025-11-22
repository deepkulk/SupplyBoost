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

TRACING_CONFIG = """  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces"""

METRICS_ENHANCEMENT = """    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}"""

def update_application_yml(service_name):
    yml_path = os.path.join(BASE_DIR, service_name, "src/main/resources/application.yml")

    if not os.path.exists(yml_path):
        print(f"  ✗ application.yml not found: {service_name}")
        return False

    with open(yml_path, 'r') as f:
        content = f.read()

    # Check if already has tracing config
    if "tracing:" in content and "sampling:" in content:
        print(f"  - Skipped {service_name} (already has tracing config)")
        return False

    modified = False

    # Add enhanced metrics config if not present
    if "distribution:" not in content and "prometheus:" in content:
        # Add after prometheus enabled line
        content = re.sub(
            r'(prometheus:\s*\n\s*enabled:\s*true)',
            r'\1\n' + METRICS_ENHANCEMENT,
            content
        )
        modified = True

    # Add tracing config after metrics section
    if "tracing:" not in content:
        # Find the management section and add tracing
        if "management:" in content:
            # Find where to insert (after metrics export section)
            lines = content.split('\n')
            new_lines = []
            in_management = False
            added = False

            for i, line in enumerate(lines):
                new_lines.append(line)

                if line.strip().startswith('management:'):
                    in_management = True

                # Add after prometheus enabled line or after metrics section
                if in_management and not added:
                    if 'enabled: true' in line and i + 1 < len(lines):
                        # Check if next line is not indented more (end of metrics section)
                        next_line = lines[i + 1] if i + 1 < len(lines) else ''
                        if next_line and not next_line.startswith(' ' * (len(line) - len(line.lstrip()) + 2)):
                            new_lines.append(TRACING_CONFIG)
                            added = True
                            modified = True

            if modified:
                content = '\n'.join(new_lines)

    if modified:
        with open(yml_path, 'w') as f:
            f.write(content)
        print(f"  ✓ Updated {service_name}")
        return True
    else:
        print(f"  - No changes needed for {service_name}")
        return False

print("Updating all application.yml files...")
for service in SERVICES:
    update_application_yml(service)

print("\nDone!")
