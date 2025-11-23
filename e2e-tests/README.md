# End-to-End Tests

This module contains end-to-end tests for SupplyBoost that validate complete user journeys across multiple microservices.

## Test Scenarios

### 1. Complete Purchase Journey
Tests the full customer purchase flow:
1. User registration/authentication
2. Product browsing and search
3. Add products to cart
4. Update cart quantities
5. Checkout and create order
6. Payment processing
7. Shipment creation
8. Order status updates
9. Email notifications

### 2. Order Cancellation Flow
Tests the order cancellation scenario:
1. Create order
2. Initiate payment
3. Cancel order before shipment
4. Verify payment refund
5. Verify inventory restoration

### 3. Failed Payment Recovery
Tests payment failure scenarios:
1. Create order
2. Simulate payment failure
3. Verify order status update
4. Verify inventory reservation release
5. Retry payment
6. Complete order

## Running Tests

### Prerequisites
- Docker and Docker Compose installed
- All services running via `docker-compose up`
- Services accessible on default ports

### Run All E2E Tests
```bash
cd e2e-tests
npm install
npm test
```

### Run Specific Test Suite
```bash
npm test -- --grep "Complete Purchase Journey"
```

### Run with Verbose Output
```bash
npm test -- --reporter spec
```

## Test Configuration

Tests are configured via environment variables in `.env`:
- `IDENTITY_SERVICE_URL`: Default http://localhost:8081
- `PRODUCT_SERVICE_URL`: Default http://localhost:8082
- `CART_SERVICE_URL`: Default http://localhost:8083
- `ORDER_SERVICE_URL`: Default http://localhost:8084
- `PAYMENT_SERVICE_URL`: Default http://localhost:8086

## Test Data Management

Tests use isolated test data and clean up after execution. Each test:
1. Creates test users with unique identifiers
2. Uses dedicated test products
3. Cleans up created resources after completion

## Monitoring Test Execution

During test execution, you can monitor:
- **Jaeger**: http://localhost:16686 - Distributed traces
- **Kibana**: http://localhost:5601 - Application logs
- **Grafana**: http://localhost:3000 - Metrics and dashboards
- **Kafka UI**: http://localhost:8090 - Event flow

## Test Reports

Test results are generated in the `reports/` directory:
- `junit.xml`: JUnit-format test results
- `coverage/`: Code coverage reports
- `screenshots/`: Screenshots of failures (if applicable)

## Troubleshooting

### Tests Timing Out
- Increase timeout in test configuration
- Verify all services are running and healthy
- Check service logs for errors

### Authentication Failures
- Verify Identity Service is accessible
- Check JWT token configuration
- Ensure test user credentials are correct

### Service Connection Errors
- Verify Docker network connectivity
- Check service ports are not blocked
- Ensure all dependencies are running
