# Notification Service

## Overview

The Notification Service manages email notifications for order confirmations, payment confirmations, and shipment tracking. It consumes Kafka events and sends templated emails with retry logic.

## Features

- **Email Templates**: Professional HTML email templates using Thymeleaf
- **Kafka Event Consumers**: Listen for order, payment, and shipment events
- **Retry Logic**: Automatic retry with configurable attempts
- **Notification History**: Track all sent notifications
- **Mock Mode**: Development mode that logs emails without sending
- **Async Processing**: Non-blocking email sending
- **Multiple Email Types**:
  - Order confirmation emails
  - Payment confirmation emails
  - Shipment tracking notifications

## Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Email**: Spring Mail with JavaMailSender
- **Templates**: Thymeleaf for HTML email rendering
- **Database**: PostgreSQL with Liquibase migrations
- **Messaging**: Apache Kafka for event consumption
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 12+
- Apache Kafka
- SMTP Server or MailDev (for development)

## Configuration

Environment variables:
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: notification_db)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka servers
- `MAIL_HOST`: SMTP server host
- `MAIL_PORT`: SMTP server port
- `MAIL_USERNAME`: SMTP username
- `MAIL_PASSWORD`: SMTP password
- `EMAIL_ENABLED`: Enable/disable email sending (default: true)
- `EMAIL_MOCK_MODE`: Use mock mode for testing (default: false)

## Running the Service

### Local Development

```bash
# Build the service
mvn clean install

# Run the service
mvn spring-boot:run
```

### Docker

```bash
mvn clean package
docker build -t supplyboost/notification-service:latest .
docker run -d -p 8086:8086 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e MAIL_HOST=maildev \
  supplyboost/notification-service:latest
```

## Kafka Events Consumed

### Order Created Event
**Topic**: `order.created`
```json
{
  "orderId": 1,
  "orderNumber": "ORD-123",
  "userId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "totalAmount": 99.99,
  "status": "CREATED",
  "createdAt": "2025-11-23T10:00:00"
}
```

### Payment Processed Event
**Topic**: `payment.processed`
```json
{
  "paymentId": 1,
  "paymentNumber": "PAY-456",
  "orderId": 1,
  "orderNumber": "ORD-123",
  "amount": 99.99,
  "paymentMethod": "CARD",
  "status": "SUCCEEDED",
  "customerEmail": "john@example.com"
}
```

### Shipment Created Event
**Topic**: `shipment.created`
```json
{
  "shipmentId": 1,
  "shipmentNumber": "SHIP-789",
  "trackingNumber": "1Z123456",
  "orderId": 1,
  "orderNumber": "ORD-123",
  "carrier": "UPS",
  "estimatedDelivery": "2025-11-28T10:00:00"
}
```

## Email Templates

Located in `src/main/resources/templates/`:
- `order-confirmation.html`: Order confirmation email
- `payment-confirmation.html`: Payment confirmation email
- `shipment-notification.html`: Shipment tracking email

## Notification History

All notifications are tracked in the database with:
- Recipient details
- Notification type
- Status (PENDING, SENT, FAILED)
- Retry count
- Failure reason (if applicable)
- Sent timestamp

## Mock Mode

For development and testing, enable mock mode:
```yaml
email:
  mock-mode: true
```

This will log email details without sending actual emails.

## Retry Configuration

```yaml
email:
  retry:
    max-attempts: 3
    delay: 5000  # milliseconds
```

## API Documentation

http://localhost:8086/swagger-ui.html

## Health Check

http://localhost:8086/actuator/health

## Monitoring

- Metrics: http://localhost:8086/actuator/prometheus
- Health: http://localhost:8086/actuator/health

## License

Apache License 2.0
