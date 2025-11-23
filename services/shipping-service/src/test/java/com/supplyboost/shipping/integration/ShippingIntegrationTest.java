package com.supplyboost.shipping.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.supplyboost.shipping.BaseIntegrationTest;
import com.supplyboost.shipping.dto.AddressDto;
import com.supplyboost.shipping.dto.CreateShipmentRequest;
import com.supplyboost.shipping.dto.ShipmentResponse;
import com.supplyboost.shipping.model.ShipmentStatus;
import com.supplyboost.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Integration tests for Shipping Service. */
class ShippingIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ShipmentRepository shipmentRepository;

  @BeforeEach
  void setUp() {
    shipmentRepository.deleteAll();
  }

  @Test
  void createShipment_Success() {
    // Given: Valid shipment request
    CreateShipmentRequest request = createValidShipmentRequest();

    // When: Create shipment
    ResponseEntity<ShipmentResponse> response =
        restTemplate.postForEntity("/api/v1/shipments", request, ShipmentResponse.class);

    // Then: Shipment created successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getShipmentId()).isNotNull();
    assertThat(response.getBody().getTrackingNumber()).isNotNull();
    assertThat(response.getBody().getOrderId()).isEqualTo(1L);
    assertThat(response.getBody().getStatus()).isEqualTo(ShipmentStatus.PENDING);
    assertThat(response.getBody().getCarrier()).isEqualTo("FedEx");
  }

  @Test
  void getShipmentById_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    String shipmentId = createResponse.getBody().getShipmentId();

    // When: Get shipment by ID
    ResponseEntity<ShipmentResponse> response =
        restTemplate.getForEntity("/api/v1/shipments/" + shipmentId, ShipmentResponse.class);

    // Then: Shipment retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getShipmentId()).isEqualTo(shipmentId);
  }

  @Test
  void getShipmentByTrackingNumber_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    String trackingNumber = createResponse.getBody().getTrackingNumber();

    // When: Get shipment by tracking number
    ResponseEntity<ShipmentResponse> response =
        restTemplate.getForEntity(
            "/api/v1/shipments/tracking/" + trackingNumber, ShipmentResponse.class);

    // Then: Shipment retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTrackingNumber()).isEqualTo(trackingNumber);
  }

  @Test
  void getShipmentByOrderId_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    Long orderId = createResponse.getBody().getOrderId();

    // When: Get shipment by order ID
    ResponseEntity<ShipmentResponse> response =
        restTemplate.getForEntity("/api/v1/shipments/order/" + orderId, ShipmentResponse.class);

    // Then: Shipment retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
  }

  @Test
  void getUserShipments_Success() {
    // Given: Create multiple shipments for the same user
    CreateShipmentRequest request1 = createValidShipmentRequest();
    CreateShipmentRequest request2 = createValidShipmentRequest();
    request2.setOrderId(2L);
    request2.setOrderNumber("ORD-002");

    restTemplate.postForEntity("/api/v1/shipments", request1, ShipmentResponse.class);
    restTemplate.postForEntity("/api/v1/shipments", request2, ShipmentResponse.class);

    // When: Get user shipments
    ResponseEntity<ShipmentResponse[]> response =
        restTemplate.getForEntity("/api/v1/shipments/user/1", ShipmentResponse[].class);

    // Then: All user shipments retrieved
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void updateShipmentStatus_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    String shipmentId = createResponse.getBody().getShipmentId();

    // When: Update shipment status to SHIPPED
    String url = String.format("/api/v1/shipments/%s/status?status=SHIPPED", shipmentId);
    ResponseEntity<ShipmentResponse> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, ShipmentResponse.class);

    // Then: Status updated successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
  }

  @Test
  void updateShipmentStatus_ToInTransit_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    String shipmentId = createResponse.getBody().getShipmentId();

    // When: Update shipment status to IN_TRANSIT
    String url = String.format("/api/v1/shipments/%s/status?status=IN_TRANSIT", shipmentId);
    ResponseEntity<ShipmentResponse> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, ShipmentResponse.class);

    // Then: Status updated successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
  }

  @Test
  void updateShipmentStatus_ToDelivered_Success() {
    // Given: Create a shipment first
    CreateShipmentRequest createRequest = createValidShipmentRequest();
    ResponseEntity<ShipmentResponse> createResponse =
        restTemplate.postForEntity("/api/v1/shipments", createRequest, ShipmentResponse.class);
    String shipmentId = createResponse.getBody().getShipmentId();

    // When: Update shipment status to DELIVERED
    String url = String.format("/api/v1/shipments/%s/status?status=DELIVERED", shipmentId);
    ResponseEntity<ShipmentResponse> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, ShipmentResponse.class);

    // Then: Status updated successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
  }

  @Test
  void createShipment_WithDifferentCarrier_Success() {
    // Given: Shipment request with UPS carrier
    CreateShipmentRequest request = createValidShipmentRequest();
    request.setCarrier("UPS");

    // When: Create shipment
    ResponseEntity<ShipmentResponse> response =
        restTemplate.postForEntity("/api/v1/shipments", request, ShipmentResponse.class);

    // Then: Shipment created with UPS carrier
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCarrier()).isEqualTo("UPS");
  }

  // Helper methods

  private CreateShipmentRequest createValidShipmentRequest() {
    CreateShipmentRequest request = new CreateShipmentRequest();
    request.setOrderId(1L);
    request.setOrderNumber("ORD-001");
    request.setUserId(1L);
    request.setCarrier("FedEx");
    request.setCustomerName("John Doe");
    request.setCustomerEmail("john.doe@example.com");
    request.setCustomerPhone("+1234567890");

    AddressDto shippingAddress = new AddressDto();
    shippingAddress.setLine1("123 Main St");
    shippingAddress.setLine2("Apt 4B");
    shippingAddress.setCity("New York");
    shippingAddress.setState("NY");
    shippingAddress.setPostalCode("10001");
    shippingAddress.setCountry("USA");
    request.setShippingAddress(shippingAddress);

    return request;
  }
}
