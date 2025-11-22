# Deployment Runbook

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Deployment Steps](#deployment-steps)
4. [Post-Deployment Validation](#post-deployment-validation)
5. [Rollback Procedure](#rollback-procedure)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### Tools Required
- `kubectl` (v1.27+)
- `docker` (v20+)
- `maven` (v3.8+)
- `java` (v17+)
- Access to Kubernetes cluster

### Access Requirements
- Kubernetes cluster admin access
- Docker registry credentials
- Database connection credentials
- Kafka cluster access

## Pre-Deployment Checklist

- [ ] All tests passing (`mvn clean verify`)
- [ ] Code review completed
- [ ] Security scan completed (OWASP + Trivy)
- [ ] Database migrations reviewed
- [ ] Configuration reviewed (ConfigMaps, Secrets)
- [ ] Monitoring dashboards updated
- [ ] Rollback plan documented
- [ ] Team notified of deployment

## Deployment Steps

### 1. Build and Test

```bash
# Navigate to project root
cd /path/to/SupplyBoost

# Run all tests
mvn clean test

# Build artifacts
mvn clean package -DskipTests

# Run security scans
./scripts/run-security-scan.sh
```

### 2. Build Docker Images

```bash
# Build all service images
./scripts/build-and-push-images.sh

# Verify images
docker images | grep supplyboost
```

### 3. Push to Registry (if deploying to cloud)

```bash
# Login to Docker registry
docker login docker.io

# Push images
docker push supplyboost/identity-service:0.1.0-SNAPSHOT
docker push supplyboost/product-catalog-service:0.1.0-SNAPSHOT
# ... repeat for all services
```

### 4. Deploy to Kubernetes

```bash
# Deploy all services
./scripts/deploy-to-kubernetes.sh

# Or deploy manually
kubectl apply -f infrastructure/kubernetes/namespace.yml
kubectl apply -f infrastructure/kubernetes/shared-configmap.yml
kubectl apply -f infrastructure/kubernetes/identity-service/
kubectl apply -f infrastructure/kubernetes/ingress.yml
```

### 5. Apply Database Migrations

```bash
# Migrations run automatically via Liquibase on startup
# Monitor logs to ensure successful migration
kubectl logs -f deployment/identity-service -n supplyboost | grep liquibase
```

## Post-Deployment Validation

### Health Checks

```bash
# Check pod status
kubectl get pods -n supplyboost

# Check service endpoints
kubectl get svc -n supplyboost

# Check ingress
kubectl get ingress -n supplyboost

# Test health endpoints
curl https://supplyboost.example.com/api/identity/actuator/health
```

### Smoke Tests

```bash
# 1. User Registration
curl -X POST https://supplyboost.example.com/api/identity/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"Test123!"}'

# 2. Browse Products
curl https://supplyboost.example.com/api/catalog/products

# 3. Check Metrics
curl https://supplyboost.example.com/api/identity/actuator/prometheus
```

### Monitoring

- [ ] Check Grafana dashboards: http://grafana.supplyboost.example.com
- [ ] Verify Prometheus targets: http://prometheus.supplyboost.example.com/targets
- [ ] Review Jaeger traces: http://jaeger.supplyboost.example.com
- [ ] Check Kibana logs: http://kibana.supplyboost.example.com

### Performance Validation

```bash
# Run load tests
cd tests/gatling
mvn gatling:test
```

## Rollback Procedure

### Quick Rollback

```bash
# Rollback deployment to previous version
kubectl rollout undo deployment/identity-service -n supplyboost

# Check rollout status
kubectl rollout status deployment/identity-service -n supplyboost
```

### Full Rollback

```bash
# 1. Tag the current state
kubectl get deployment identity-service -n supplyboost -o yaml > rollback-backup.yml

# 2. Apply previous manifests
git checkout <previous-commit>
kubectl apply -f infrastructure/kubernetes/identity-service/

# 3. Verify rollback
kubectl get pods -n supplyboost
kubectl logs -f deployment/identity-service -n supplyboost
```

### Database Rollback

```bash
# If database migration fails, rollback using Liquibase
# SSH into pod
kubectl exec -it <pod-name> -n supplyboost -- /bin/bash

# Run rollback command
java -jar liquibase.jar rollbackCount 1 \
  --url=jdbc:postgresql://postgres:5432/identity_db \
  --username=supplyboost \
  --password=<password> \
  --changeLogFile=db/changelog/db.changelog-master.xml
```

## Troubleshooting

### Pods Not Starting

```bash
# Check pod events
kubectl describe pod <pod-name> -n supplyboost

# Check logs
kubectl logs <pod-name> -n supplyboost

# Common issues:
# - Image pull errors: Verify image exists and registry credentials
# - OOMKilled: Increase memory limits in deployment.yml
# - CrashLoopBackOff: Check application logs for errors
```

### Service Unavailable

```bash
# Check service endpoints
kubectl get endpoints -n supplyboost

# Verify service selector matches pod labels
kubectl get pods --show-labels -n supplyboost

# Test service directly
kubectl run -it --rm debug --image=busybox --restart=Never -- wget -O- http://identity-service:8081/actuator/health
```

### Database Connection Issues

```bash
# Verify database is running
kubectl get pods -n supplyboost | grep postgres

# Test database connectivity
kubectl exec -it <app-pod> -n supplyboost -- /bin/bash
psql -h postgres-service -U supplyboost -d identity_db

# Check credentials in secrets
kubectl get secret identity-service-secret -n supplyboost -o yaml
```

### High Memory/CPU Usage

```bash
# Check resource usage
kubectl top pods -n supplyboost

# Check metrics in Grafana
# Navigate to: System Overview Dashboard

# If needed, scale horizontally
kubectl scale deployment identity-service --replicas=5 -n supplyboost

# Or increase resource limits
kubectl edit deployment identity-service -n supplyboost
```

### Kafka Connection Issues

```bash
# Verify Kafka is running
kubectl get pods -n supplyboost | grep kafka

# Check Kafka topics
kubectl exec -it kafka-0 -n supplyboost -- kafka-topics --list --bootstrap-server localhost:9092

# Test connectivity from service
kubectl exec -it <pod-name> -n supplyboost -- /bin/bash
telnet kafka-service 9092
```

## Emergency Contacts

- **On-Call Engineer**: +1-XXX-XXX-XXXX
- **DevOps Lead**: devops@supplyboost.com
- **Platform Team**: platform@supplyboost.com
- **Slack Channel**: #supplyboost-incidents

## Useful Commands

```bash
# Watch deployment progress
kubectl get pods -n supplyboost --watch

# Stream logs from all pods of a service
kubectl logs -f -l app=identity-service -n supplyboost

# Execute command in pod
kubectl exec -it <pod-name> -n supplyboost -- /bin/bash

# Port forward for local debugging
kubectl port-forward svc/identity-service 8081:8081 -n supplyboost

# Get recent events
kubectl get events -n supplyboost --sort-by='.lastTimestamp'

# Describe all resources
kubectl describe all -n supplyboost
```
