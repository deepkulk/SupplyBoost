# Accounting Service

## Overview

The Accounting Service manages invoice generation, revenue recognition, and financial tracking. It generates PDF invoices, tracks payment status, and consumes Kafka events for automated accounting workflows.

## Features

- **Invoice Generation**: Automatic invoice creation with unique invoice numbers
- **PDF Creation**: Professional PDF invoices using iText library
- **Tax Calculation**: Configurable tax rate with automatic calculation
- **Revenue Recognition**: Track revenue on shipment events
- **Invoice Status Tracking**: DRAFT → ISSUED → PAID → CANCELLED
- **Kafka Event Consumers**: Listen for payment and shipment events
- **RESTful API**: Complete invoice management endpoints
- **Multi-tenant Ready**: Track invoices per customer

## Technology Stack

- **Framework**: Spring Boot 3.2.1
- **PDF Generation**: iText 7.2.5
- **Database**: PostgreSQL with Liquibase migrations
- **Messaging**: Apache Kafka for event consumption
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 12+
- Apache Kafka

## Configuration

Environment variables:
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 5432)
- `DB_NAME`: Database name (default: accounting_db)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka servers
- `PDF_STORAGE_PATH`: Path to store generated PDFs (default: /tmp/invoices)
- `TAX_RATE`: Tax rate as decimal (default: 0.08 for 8%)
- `COMPANY_NAME`: Company name for invoices
- `COMPANY_ADDRESS`: Company address
- `COMPANY_EMAIL`: Company email
- `COMPANY_PHONE`: Company phone

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
docker build -t supplyboost/accounting-service:latest .
docker run -d -p 8088:8088 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -v /path/to/invoices:/tmp/invoices \
  supplyboost/accounting-service:latest
```

## API Endpoints

### Create Invoice
```http
POST /api/v1/invoices
{
  "orderId": 1,
  "orderNumber": "ORD-123",
  "userId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "subtotal": 99.99,
  "paymentId": "PAY-456"
}
```

### Get Invoice by Order
```http
GET /api/v1/invoices/order/{orderNumber}
```

### Issue Invoice (Generate PDF)
```http
POST /api/v1/invoices/{invoiceId}/issue
```

### Mark Invoice as Paid
```http
POST /api/v1/invoices/{invoiceId}/paid
```

### Get Customer Invoices
```http
GET /api/v1/invoices/customer/{email}
```

## Invoice Status Flow

```
DRAFT → ISSUED → PAID
           ↓
      CANCELLED
```

## Kafka Events Consumed

### Payment Processed Event
**Topic**: `payment.processed`

Triggers:
- Invoice creation (if not exists)
- Invoice status update to DRAFT

### Shipment Created Event
**Topic**: `shipment.created`

Triggers:
- Revenue recognition entry creation
- Invoice status update to ISSUED
- PDF invoice generation

## Revenue Recognition

Revenue is recognized when shipment is created, following accounting principles:
- Revenue status: PENDING → RECOGNIZED
- Recognition type: SHIPMENT_BASED
- Tracked with order, invoice, and shipment references

## PDF Invoice Details

Generated invoices include:
- Company header with logo and contact info
- Invoice number and dates
- Customer billing information
- Order line items
- Subtotal, tax, and total amounts
- Payment status
- Professional formatting

PDFs are stored at the configured `PDF_STORAGE_PATH`.

## Tax Configuration

```yaml
accounting:
  tax-rate: 0.08  # 8% tax
  company-name: "SupplyBoost Inc."
  company-address: "123 Business St, San Francisco, CA 94102"
  company-email: "billing@supplyboost.com"
  company-phone: "+1 (415) 555-0123"
  pdf-storage-path: "/tmp/invoices"
```

## API Documentation

http://localhost:8088/swagger-ui.html

## Health Check

http://localhost:8088/actuator/health

## Monitoring

- Metrics: http://localhost:8088/actuator/prometheus
- Health: http://localhost:8088/actuator/health

## Database Schema

### Invoices Table
- Invoice metadata and financial details
- Customer information
- Order and payment references
- PDF file path
- Status tracking

### Revenue Recognition Table
- Revenue recognition entries
- Recognition type and status
- Amount and date tracking
- Order and shipment references

## Error Handling

- Duplicate invoice prevention (one per order)
- Validation for required fields
- Graceful handling of PDF generation failures
- Retry logic for Kafka event consumption

## Future Enhancements

- Multi-currency support
- Invoice templates customization
- Automated dunning for overdue invoices
- Payment reconciliation
- Financial reporting APIs
- Integration with accounting software (QuickBooks, Xero)

## License

Apache License 2.0
