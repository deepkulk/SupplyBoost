package com.supplyboost.payment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.supplyboost.payment.BaseIntegrationTest;
import com.supplyboost.payment.dto.*;
import com.supplyboost.payment.model.PaymentStatus;
import com.supplyboost.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Integration tests for Payment Service. */
class PaymentIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private PaymentRepository paymentRepository;

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
  }

  @Test
  void createPayment_Success() {
    // Given: Valid payment request
    CreatePaymentRequest request = createValidPaymentRequest();

    // When: Create payment
    ResponseEntity<PaymentResponse> response =
        restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);

    // Then: Payment created successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isNotNull();
    assertThat(response.getBody().getOrderId()).isEqualTo(1L);
    assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(response.getBody().getCurrency()).isEqualTo("USD");
  }

  @Test
  void confirmPayment_Success() {
    // Given: Create a payment first
    CreatePaymentRequest createRequest = createValidPaymentRequest();
    ResponseEntity<PaymentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/payments", createRequest, PaymentResponse.class);
    String paymentId = createResponse.getBody().getPaymentId();

    // When: Confirm payment
    ConfirmPaymentRequest confirmRequest = new ConfirmPaymentRequest();
    confirmRequest.setPaymentMethod("CREDIT_CARD");

    ResponseEntity<PaymentResponse> response =
        restTemplate.postForEntity(
            "/api/v1/payments/" + paymentId + "/confirm",
            confirmRequest,
            PaymentResponse.class);

    // Then: Payment confirmed successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(response.getBody().getPaymentMethod()).isEqualTo("CREDIT_CARD");
  }

  @Test
  void getPaymentById_Success() {
    // Given: Create a payment first
    CreatePaymentRequest createRequest = createValidPaymentRequest();
    ResponseEntity<PaymentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/payments", createRequest, PaymentResponse.class);
    String paymentId = createResponse.getBody().getPaymentId();

    // When: Get payment by ID
    ResponseEntity<PaymentResponse> response =
        restTemplate.getForEntity("/api/v1/payments/" + paymentId, PaymentResponse.class);

    // Then: Payment retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getPaymentId()).isEqualTo(paymentId);
  }

  @Test
  void getPaymentByOrderId_Success() {
    // Given: Create a payment first
    CreatePaymentRequest createRequest = createValidPaymentRequest();
    ResponseEntity<PaymentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/payments", createRequest, PaymentResponse.class);
    Long orderId = createResponse.getBody().getOrderId();

    // When: Get payment by order ID
    ResponseEntity<PaymentResponse> response =
        restTemplate.getForEntity("/api/v1/payments/order/" + orderId, PaymentResponse.class);

    // Then: Payment retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
  }

  @Test
  void getUserPayments_Success() {
    // Given: Create multiple payments for the same user
    CreatePaymentRequest request1 = createValidPaymentRequest();
    CreatePaymentRequest request2 = createValidPaymentRequest();
    request2.setOrderId(2L);
    request2.setOrderNumber("ORD-002");

    restTemplate.postForEntity("/api/v1/payments", request1, PaymentResponse.class);
    restTemplate.postForEntity("/api/v1/payments", request2, PaymentResponse.class);

    // When: Get user payments
    ResponseEntity<PaymentResponse[]> response =
        restTemplate.getForEntity("/api/v1/payments/user/1", PaymentResponse[].class);

    // Then: All user payments retrieved
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void refundPayment_Success() {
    // Given: Create and confirm a payment first
    CreatePaymentRequest createRequest = createValidPaymentRequest();
    ResponseEntity<PaymentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/payments", createRequest, PaymentResponse.class);
    String paymentId = createResponse.getBody().getPaymentId();

    ConfirmPaymentRequest confirmRequest = new ConfirmPaymentRequest();
    confirmRequest.setPaymentMethod("CREDIT_CARD");
    restTemplate.postForEntity(
        "/api/v1/payments/" + paymentId + "/confirm", confirmRequest, PaymentResponse.class);

    // When: Refund payment
    RefundRequest refundRequest = new RefundRequest();
    refundRequest.setAmount(new BigDecimal("100.00"));
    refundRequest.setReason("Customer request");

    ResponseEntity<PaymentResponse> response =
        restTemplate.postForEntity(
            "/api/v1/payments/" + paymentId + "/refund", refundRequest, PaymentResponse.class);

    // Then: Payment refunded successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
  }

  @Test
  void createPayment_WithDifferentCurrency_Success() {
    // Given: Payment request with EUR currency
    CreatePaymentRequest request = createValidPaymentRequest();
    request.setCurrency("EUR");
    request.setAmount(new BigDecimal("85.50"));

    // When: Create payment
    ResponseEntity<PaymentResponse> response =
        restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);

    // Then: Payment created with EUR currency
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCurrency()).isEqualTo("EUR");
    assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("85.50"));
  }

  // Helper methods

  private CreatePaymentRequest createValidPaymentRequest() {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setOrderId(1L);
    request.setOrderNumber("ORD-001");
    request.setUserId(1L);
    request.setAmount(new BigDecimal("100.00"));
    request.setCurrency("USD");
    request.setCustomerEmail("john.doe@example.com");
    request.setCustomerName("John Doe");
    return request;
  }
}
