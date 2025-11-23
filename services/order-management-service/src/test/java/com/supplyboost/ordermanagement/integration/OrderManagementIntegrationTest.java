package com.supplyboost.ordermanagement.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.supplyboost.ordermanagement.BaseIntegrationTest;
import com.supplyboost.ordermanagement.client.ShoppingCartClient;
import com.supplyboost.ordermanagement.dto.*;
import com.supplyboost.ordermanagement.model.OrderStatus;
import com.supplyboost.ordermanagement.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Integration tests for Order Management Service. */
class OrderManagementIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private OrderRepository orderRepository;

  @MockBean private ShoppingCartClient shoppingCartClient;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
  }

  @Test
  void createOrder_Success() {
    // Given: Mock shopping cart response
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);

    CreateOrderRequest request = createValidOrderRequest();

    // When: Create order
    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);

    // Then: Order created successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getOrderNumber()).startsWith("ORD-");
    assertThat(response.getBody().getUserId()).isEqualTo(1L);
    assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(response.getBody().getTotalAmount()).isGreaterThan(BigDecimal.ZERO);
    assertThat(response.getBody().getItems()).hasSize(2);
  }

  @Test
  void getOrderById_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    Long orderId = createResponse.getBody().getId();

    // When: Get order by ID
    ResponseEntity<OrderResponse> response =
        restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);

    // Then: Order retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(orderId);
  }

  @Test
  void getOrderByNumber_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    String orderNumber = createResponse.getBody().getOrderNumber();

    // When: Get order by number
    ResponseEntity<OrderResponse> response =
        restTemplate.getForEntity(
            "/api/v1/orders/number/" + orderNumber, OrderResponse.class);

    // Then: Order retrieved successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getOrderNumber()).isEqualTo(orderNumber);
  }

  @Test
  void getUserOrders_Success() {
    // Given: Create multiple orders for the same user
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);

    CreateOrderRequest request1 = createValidOrderRequest();
    CreateOrderRequest request2 = createValidOrderRequest();
    request2.setCartId("cart-456");

    restTemplate.postForEntity("/api/v1/orders", request1, OrderResponse.class);
    restTemplate.postForEntity("/api/v1/orders", request2, OrderResponse.class);

    // When: Get user orders
    ResponseEntity<OrderResponse[]> response =
        restTemplate.getForEntity("/api/v1/orders/user/1", OrderResponse[].class);

    // Then: All user orders retrieved
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void updateOrderStatus_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    Long orderId = createResponse.getBody().getId();

    // When: Update order status
    String url =
        String.format(
            "/api/v1/orders/%d/status?status=PAYMENT_CONFIRMED&reason=Payment received",
            orderId);
    ResponseEntity<OrderResponse> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, OrderResponse.class);

    // Then: Status updated successfully
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
  }

  @Test
  void updatePaymentInfo_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    Long orderId = createResponse.getBody().getId();

    // When: Update payment info
    String url =
        String.format(
            "/api/v1/orders/%d/payment?paymentId=pay_123&paymentStatus=COMPLETED&paymentMethod=CREDIT_CARD",
            orderId);
    ResponseEntity<Void> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);

    // Then: Payment info updated
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify the order was updated
    ResponseEntity<OrderResponse> getResponse =
        restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);
    assertThat(getResponse.getBody().getPaymentId()).isEqualTo("pay_123");
    assertThat(getResponse.getBody().getPaymentStatus()).isEqualTo("COMPLETED");
  }

  @Test
  void updateShipmentInfo_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    Long orderId = createResponse.getBody().getId();

    // When: Update shipment info
    String url =
        String.format(
            "/api/v1/orders/%d/shipment?shipmentId=ship_123&trackingNumber=TRACK123",
            orderId);
    ResponseEntity<Void> response =
        restTemplate.exchange(url, HttpMethod.PUT, null, Void.class);

    // Then: Shipment info updated
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify the order was updated
    ResponseEntity<OrderResponse> getResponse =
        restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);
    assertThat(getResponse.getBody().getShipmentId()).isEqualTo("ship_123");
    assertThat(getResponse.getBody().getTrackingNumber()).isEqualTo("TRACK123");
    assertThat(getResponse.getBody().getStatus()).isEqualTo(OrderStatus.SHIPPED);
  }

  @Test
  void cancelOrder_Success() {
    // Given: Create an order first
    CartDto mockCart = createMockCart();
    when(shoppingCartClient.getCart(anyString())).thenReturn(mockCart);
    CreateOrderRequest request = createValidOrderRequest();
    ResponseEntity<OrderResponse> createResponse =
        restTemplate.postForEntity("/api/v1/orders", request, OrderResponse.class);
    Long orderId = createResponse.getBody().getId();

    // When: Cancel order
    String url = String.format("/api/v1/orders/%d?reason=Customer request", orderId);
    ResponseEntity<Void> response =
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);

    // Then: Order cancelled
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // Verify the order status changed
    ResponseEntity<OrderResponse> getResponse =
        restTemplate.getForEntity("/api/v1/orders/" + orderId, OrderResponse.class);
    assertThat(getResponse.getBody().getStatus()).isEqualTo(OrderStatus.CANCELLED);
  }

  // Helper methods

  private CreateOrderRequest createValidOrderRequest() {
    CreateOrderRequest request = new CreateOrderRequest();
    request.setUserId(1L);
    request.setCartId("cart-123");
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

    AddressDto billingAddress = new AddressDto();
    billingAddress.setLine1("123 Main St");
    billingAddress.setLine2("Apt 4B");
    billingAddress.setCity("New York");
    billingAddress.setState("NY");
    billingAddress.setPostalCode("10001");
    billingAddress.setCountry("USA");
    request.setBillingAddress(billingAddress);

    request.setNotes("Please deliver before 5 PM");

    return request;
  }

  private CartDto createMockCart() {
    CartDto cart = new CartDto();
    cart.setCartId("cart-123");
    cart.setUserId(1L);

    CartItemDto item1 = new CartItemDto();
    item1.setProductId(1L);
    item1.setProductName("Product 1");
    item1.setProductSku("SKU-001");
    item1.setQuantity(2);
    item1.setUnitPrice(new BigDecimal("50.00"));
    item1.setSubtotal(new BigDecimal("100.00"));
    item1.setImageUrl("http://example.com/product1.jpg");

    CartItemDto item2 = new CartItemDto();
    item2.setProductId(2L);
    item2.setProductName("Product 2");
    item2.setProductSku("SKU-002");
    item2.setQuantity(1);
    item2.setUnitPrice(new BigDecimal("75.00"));
    item2.setSubtotal(new BigDecimal("75.00"));
    item2.setImageUrl("http://example.com/product2.jpg");

    cart.setItems(Arrays.asList(item1, item2));
    cart.setTotalPrice(new BigDecimal("175.00"));

    return cart;
  }
}
