# Complete Purchase Journey E2E Test

This document outlines the end-to-end test for a complete purchase journey in SupplyBoost.

## Test Scenario: Happy Path Purchase Flow

### Prerequisites
- All microservices running
- Test database seeded with products
- Email service configured (MailDev for testing)

### Test Steps

#### 1. User Registration & Authentication
```
POST /api/v1/auth/register
{
  "username": "testuser_<timestamp>",
  "email": "test_<timestamp>@example.com",
  "password": "Test123!@#",
  "firstName": "John",
  "lastName": "Doe"
}

Expected: 201 Created
Response: { userId, accessToken, refreshToken }
```

#### 2. Browse Products
```
GET /api/v1/products?page=0&size=10&sort=name,asc

Expected: 200 OK
Response: { products[], totalElements, totalPages }
```

#### 3. Search Products
```
GET /api/v1/products/search?q=laptop&page=0&size=10

Expected: 200 OK
Response: { products[] with matching items }
```

#### 4. Add Products to Cart
```
POST /api/v1/cart/items
Headers: Authorization: Bearer <accessToken>
{
  "productId": 1,
  "quantity": 2
}

Expected: 201 Created
Response: { cartId, items[], totalPrice }
```

#### 5. Update Cart Quantity
```
PUT /api/v1/cart/items/{productId}
Headers: Authorization: Bearer <accessToken>
{
  "quantity": 3
}

Expected: 200 OK
Response: { updated cart with new quantities }
```

#### 6. View Cart
```
GET /api/v1/cart
Headers: Authorization: Bearer <accessToken>

Expected: 200 OK
Response: { cartId, items[], totalPrice, itemCount }
```

#### 7. Create Order
```
POST /api/v1/orders
Headers: Authorization: Bearer <accessToken>
{
  "cartId": "<cartId>",
  "userId": <userId>,
  "customerName": "John Doe",
  "customerEmail": "test@example.com",
  "customerPhone": "+1234567890",
  "shippingAddress": {
    "line1": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "billingAddress": { ... },
  "notes": "Deliver before 5 PM"
}

Expected: 201 Created
Response: { orderId, orderNumber, status: "CREATED", totalAmount }

Verify:
- Order created event published to Kafka
- Inventory reservations created
- Cart cleared after order creation
```

#### 8. Process Payment
```
POST /api/v1/payments
Headers: Authorization: Bearer <accessToken>
{
  "orderId": <orderId>,
  "orderNumber": "<orderNumber>",
  "userId": <userId>,
  "amount": <totalAmount>,
  "currency": "USD",
  "customerEmail": "test@example.com",
  "customerName": "John Doe"
}

Expected: 201 Created
Response: { paymentId, status: "PENDING" }

POST /api/v1/payments/{paymentId}/confirm
{
  "paymentMethod": "CREDIT_CARD"
}

Expected: 200 OK
Response: { paymentId, status: "COMPLETED", paymentMethod }

Verify:
- Payment confirmed event published to Kafka
- Order status updated to "PAYMENT_CONFIRMED"
- Order payment info updated
```

#### 9. Create Shipment
```
Kafka Event Handler processes OrderPaymentConfirmed event

POST /api/v1/shipments (triggered by saga orchestrator)
{
  "orderId": <orderId>,
  "orderNumber": "<orderNumber>",
  "userId": <userId>,
  "carrier": "FedEx",
  "customerName": "John Doe",
  "customerEmail": "test@example.com",
  "shippingAddress": { ... }
}

Expected: 201 Created
Response: { shipmentId, trackingNumber, status: "PENDING" }

Verify:
- Shipment created event published to Kafka
- Order shipment info updated
- Tracking number assigned
```

#### 10. Update Shipment Status
```
PUT /api/v1/shipments/{shipmentId}/status?status=SHIPPED

Expected: 200 OK
Response: { shipmentId, status: "SHIPPED" }

PUT /api/v1/shipments/{shipmentId}/status?status=IN_TRANSIT

Expected: 200 OK
Response: { shipmentId, status: "IN_TRANSIT" }

PUT /api/v1/shipments/{shipmentId}/status?status=DELIVERED

Expected: 200 OK
Response: { shipmentId, status: "DELIVERED" }

Verify:
- Order status updated to "SHIPPED", "IN_TRANSIT", "DELIVERED"
- Notification emails sent for each status change
```

#### 11. Generate Invoice
```
Kafka Event Handler processes OrderDelivered event

POST /api/v1/invoices (triggered automatically)
{
  "orderId": <orderId>,
  "orderNumber": "<orderNumber>",
  "customerName": "John Doe",
  "customerEmail": "test@example.com",
  "items": [...],
  "totalAmount": <totalAmount>
}

Expected: 201 Created
Response: { invoiceId, invoiceNumber, pdfUrl }

Verify:
- Invoice PDF generated
- Invoice email sent to customer
```

#### 12. Verify Complete Flow
```
GET /api/v1/orders/{orderId}
Headers: Authorization: Bearer <accessToken>

Expected: 200 OK
Response: {
  orderId,
  orderNumber,
  status: "DELIVERED",
  paymentId: "<paymentId>",
  paymentStatus: "COMPLETED",
  shipmentId: "<shipmentId>",
  trackingNumber: "<trackingNumber>",
  items[],
  totalAmount
}

GET /api/v1/shipments/tracking/{trackingNumber}

Expected: 200 OK
Response: { shipment details with status "DELIVERED" }
```

### Expected Events Published (in order)
1. UserRegistered
2. ProductViewed
3. ProductAddedToCart
4. CartUpdated
5. OrderCreated
6. InventoryReserved
7. PaymentInitiated
8. PaymentCompleted
9. OrderPaymentConfirmed
10. ShipmentCreated
11. OrderShipped
12. OrderInTransit
13. OrderDelivered
14. InvoiceGenerated
15. NotificationSent (multiple times)

### Expected Notifications (Email)
1. Order confirmation email
2. Payment confirmation email
3. Shipment created email
4. Order shipped email
5. Order delivered email
6. Invoice email with PDF attachment

### Observability Validation

#### Distributed Tracing (Jaeger)
- Verify single trace spans across all services
- Check trace propagation through Kafka events
- Validate service dependencies visualized correctly

#### Logs (Kibana)
- Search for orderNumber in logs
- Verify logs from all services (identity, product, cart, order, payment, shipping, notification, accounting)
- Check log levels and structured logging format

#### Metrics (Grafana)
- Order creation rate increased
- Payment success rate = 100%
- No error rates during test
- Response times within SLA (< 2s)

### Cleanup
```
DELETE /api/v1/cart (clear cart if needed)
DELETE test user and associated data
```

## Test Assertions

### Functional Assertions
- ✅ User can register and authenticate
- ✅ User can browse and search products
- ✅ User can add/update items in cart
- ✅ Order created successfully from cart
- ✅ Payment processed successfully
- ✅ Shipment created and tracked
- ✅ Order status updated through all stages
- ✅ Notifications sent at each stage
- ✅ Invoice generated with PDF

### Data Consistency Assertions
- ✅ Cart cleared after order creation
- ✅ Inventory decreased by ordered quantities
- ✅ Order payment info matches payment record
- ✅ Order shipment info matches shipment record
- ✅ All services show consistent order status

### Event-Driven Assertions
- ✅ All expected events published to Kafka
- ✅ Events consumed by appropriate services
- ✅ Saga orchestration completes successfully
- ✅ No orphaned transactions or hanging states

### Performance Assertions
- ✅ All API calls complete within 2 seconds
- ✅ End-to-end flow completes within 30 seconds
- ✅ No timeout errors
- ✅ Database queries optimized (< 100ms)

### Observability Assertions
- ✅ Distributed traces captured in Jaeger
- ✅ Logs ingested to Elasticsearch
- ✅ Metrics recorded in Prometheus
- ✅ No missing trace spans
- ✅ Correct trace context propagation

## Failure Scenarios to Test

### 1. Payment Failure
- Simulate payment gateway rejection
- Verify order remains in CREATED status
- Verify inventory reservation released after timeout
- Verify compensating transaction executed

### 2. Insufficient Inventory
- Attempt order with quantity > available stock
- Verify order creation fails gracefully
- Verify cart remains intact
- Verify appropriate error message

### 3. Service Unavailability
- Simulate shipping service down during order creation
- Verify saga compensation triggered
- Verify order rolled back
- Verify payment not processed

### 4. Duplicate Order Prevention
- Submit same cart twice rapidly
- Verify only one order created
- Verify idempotency maintained

## Success Criteria

The E2E test passes when:
1. All API calls return expected status codes
2. Order progresses through all statuses correctly
3. All events published and consumed successfully
4. All notifications delivered
5. Invoice generated successfully
6. Data consistency maintained across all services
7. Observability data captured correctly
8. No errors in service logs
9. All cleanup operations succeed

## Test Execution Time

Expected: 15-30 seconds for complete flow
Timeout: 60 seconds
