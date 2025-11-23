#!/bin/bash

###################################################
# OWASP Dependency Check - Security Vulnerability Scan
###################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Running OWASP Dependency Check...${NC}\n"

cd ..

# Run dependency check
mvn org.owasp:dependency-check-maven:check

echo -e "\n${GREEN}OWASP Dependency Check completed!${NC}"
echo -e "${YELLOW}Review the reports in each service's target/dependency-check-report.html${NC}"
