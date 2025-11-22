# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying SupplyBoost to a Kubernetes cluster.

## Directory Structure

```
kubernetes/
├── namespace.yml                 # Namespace definition
├── shared-configmap.yml          # Shared configuration across services
├── ingress.yml                   # Ingress configuration with NGINX
├── identity-service/             # Identity service manifests
│   ├── deployment.yml
│   ├── service.yml
│   ├── configmap.yml
│   ├── secret.yml
│   └── hpa.yml
└── [other-services]/             # Similar structure for each service
```

## Prerequisites

- Kubernetes cluster (v1.27+)
- kubectl configured
- NGINX Ingress Controller installed
- cert-manager (for TLS certificates)
- Metrics Server (for HPA)

## Quick Start

### 1. Install Prerequisites

```bash
# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml

# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Install Metrics Server (if not already installed)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### 2. Deploy SupplyBoost

```bash
# From the project root
./scripts/deploy-to-kubernetes.sh
```

Or manually:

```bash
# Create namespace
kubectl apply -f namespace.yml

# Apply shared configuration
kubectl apply -f shared-configmap.yml

# Deploy infrastructure services (PostgreSQL, Kafka, etc.)
kubectl apply -f infrastructure/

# Deploy each microservice
for service in identity-service product-catalog-service ...; do
    kubectl apply -f $service/
done

# Apply Ingress
kubectl apply -f ingress.yml
```

### 3. Verify Deployment

```bash
# Check pods
kubectl get pods -n supplyboost

# Check services
kubectl get svc -n supplyboost

# Check ingress
kubectl get ingress -n supplyboost

# Check HPA
kubectl get hpa -n supplyboost
```

## Configuration

### Secrets

Before deploying, update secrets in each service's `secret.yml`:

```yaml
# Example: identity-service/secret.yml
stringData:
  database.username: "your-username"
  database.password: "your-secure-password"
  jwt.secret: "your-jwt-secret-key"
```

**⚠️ Important:** Never commit real secrets to Git. Use tools like:
- Sealed Secrets
- External Secrets Operator
- HashiCorp Vault

### ConfigMaps

Review and update ConfigMaps for your environment:
- Database URLs
- Kafka bootstrap servers
- External service endpoints

## Scaling

### Manual Scaling

```bash
# Scale a specific service
kubectl scale deployment identity-service --replicas=5 -n supplyboost
```

### Auto-Scaling (HPA)

HPA is configured for all services. It will automatically scale based on:
- CPU utilization (target: 70%)
- Memory utilization (target: 80%)

```bash
# View HPA status
kubectl get hpa -n supplyboost

# Describe HPA for a service
kubectl describe hpa identity-service-hpa -n supplyboost
```

## Monitoring

Access monitoring tools:

```bash
# Port-forward Grafana
kubectl port-forward svc/grafana-service 3000:3000 -n supplyboost
# Access at: http://localhost:3000

# Port-forward Prometheus
kubectl port-forward svc/prometheus-service 9090:9090 -n supplyboost
# Access at: http://localhost:9090

# Port-forward Jaeger
kubectl port-forward svc/jaeger-service 16686:16686 -n supplyboost
# Access at: http://localhost:16686
```

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n supplyboost

# Describe pod
kubectl describe pod <pod-name> -n supplyboost

# Check logs
kubectl logs <pod-name> -n supplyboost

# Get events
kubectl get events -n supplyboost --sort-by='.lastTimestamp'
```

### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints -n supplyboost

# Test service internally
kubectl run -it --rm debug --image=busybox --restart=Never -n supplyboost -- wget -O- http://identity-service:8081/actuator/health
```

### Database Connection Issues

```bash
# Check if database is running
kubectl get pods -n supplyboost | grep postgres

# Test database connection
kubectl exec -it <app-pod> -n supplyboost -- /bin/bash
psql -h postgres-service -U supplyboost -d identity_db
```

## Rollback

```bash
# Rollback a deployment
kubectl rollout undo deployment/identity-service -n supplyboost

# Check rollout history
kubectl rollout history deployment/identity-service -n supplyboost

# Rollback to specific revision
kubectl rollout undo deployment/identity-service --to-revision=2 -n supplyboost
```

## Cleanup

```bash
# Delete all resources in namespace
kubectl delete namespace supplyboost

# Or delete individually
kubectl delete -f .
```

## Production Checklist

Before deploying to production:

- [ ] Update all secrets with production values
- [ ] Configure TLS certificates (Let's Encrypt or custom)
- [ ] Set appropriate resource limits
- [ ] Configure backup strategy for databases
- [ ] Set up monitoring and alerting
- [ ] Configure log aggregation
- [ ] Review and adjust HPA thresholds
- [ ] Set up disaster recovery procedures
- [ ] Configure network policies for security
- [ ] Review and harden RBAC permissions

## Additional Resources

- [Deployment Runbook](../../docs/operations/DEPLOYMENT_RUNBOOK.md)
- [Troubleshooting Guide](../../docs/operations/TROUBLESHOOTING_GUIDE.md)
- [Phase 3 Summary](../../docs/PHASE_3_SUMMARY.md)
- [Architecture Documentation](../../docs/ARCHITECTURE.md)
