# Quick Start Guide

Get SupplyBoost up and running in under 30 minutes.

## Prerequisites

Install the following tools:

- **Java 17+**: [Download](https://adoptium.net/)
- **Maven 3.8+**: [Download](https://maven.apache.org/download.cgi)
- **Docker 20+**: [Get Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Install](https://docs.docker.com/compose/install/)
- **Git**: [Download](https://git-scm.com/)

## Step 1: Clone Repository

```bash
git clone https://github.com/deepkulk/SupplyBoost.git
cd SupplyBoost
```

## Step 2: Start Infrastructure

Start all infrastructure services (PostgreSQL, Kafka, Redis, etc.):

```bash
cd infrastructure/docker-compose
docker-compose up -d
```

Wait for all services to be healthy (~2 minutes):

```bash
docker-compose ps
```

## Step 3: Build Services

Build all microservices:

```bash
cd ../..
mvn clean install -DskipTests
```

## Step 4: Run Services

### Option A: Run All Services with Docker Compose

```bash
# Build Docker images
./scripts/build-and-push-images.sh

# Start all services
docker-compose -f infrastructure/docker-compose/services-compose.yml up -d
```

### Option B: Run Services Individually (Development)

Open multiple terminal windows and run:

```bash
# Terminal 1: Identity Service
cd services/identity-service
mvn spring-boot:run

# Terminal 2: Product Catalog Service
cd services/product-catalog-service
mvn spring-boot:run

# Terminal 3: Order Management Service
cd services/order-management-service
mvn spring-boot:run

# ... repeat for other services
```

## Step 5: Verify Deployment

### Health Checks

```bash
# Identity Service
curl http://localhost:8081/actuator/health

# Product Catalog Service
curl http://localhost:8082/actuator/health

# All services should return: {"status":"UP"}
```

### Test API Endpoints

```bash
# Register a user
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Browse products
curl http://localhost:8082/api/products
```

## Step 6: Access Monitoring

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **Kibana**: http://localhost:5601
- **Kafka UI**: http://localhost:8090

## Common Issues

### Port Already in Use

If you get "port already in use" errors:

```bash
# Find and kill the process
lsof -i :8081  # Replace with your port
kill -9 <PID>
```

### Database Connection Refused

Ensure PostgreSQL is running:

```bash
docker-compose ps postgres
docker-compose logs postgres
```

### Out of Memory

Increase Docker memory:
- Docker Desktop → Settings → Resources → Memory (minimum 8GB)

## Next Steps

- Read the [Architecture Documentation](ARCHITECTURE.md)
- Review [API Documentation](http://localhost:8081/swagger-ui.html)
- Check [Development Roadmap](ROADMAP.md)
- Learn about [Contributing](../CONTRIBUTING.md)

## Need Help?

- Check [Troubleshooting Guide](operations/TROUBLESHOOTING_GUIDE.md)
- Review [GitHub Issues](https://github.com/deepkulk/SupplyBoost/issues)
- See [Deployment Runbook](operations/DEPLOYMENT_RUNBOOK.md)

## Clean Up

To stop and remove all containers:

```bash
# Stop services
docker-compose down

# Remove volumes (⚠️ This deletes all data!)
docker-compose down -v
```

---

**Time to complete:** ~20-30 minutes (depending on download speeds)

**Next:** Explore the [Full Documentation](../README.md)
