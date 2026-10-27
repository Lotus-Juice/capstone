package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.AddressDTOs.AddressDTO;
import com.bookworm.api.v1.dto.AddressDTOs.CreateAddressRequest;
import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.api.v1.dto.CartDTOs.AddCartItemRequest;
import com.bookworm.api.v1.dto.CartDTOs.CartDTO;
import com.bookworm.api.v1.dto.OrderDTOs.*;
import com.bookworm.api.v1.dto.PaymentDTOs.*;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.BookVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Full Order Workflow End-to-End Integration Test (PostgreSQL Testcontainers)")
class FullOrderWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookVariantRepository bookVariantRepository;

    private String customerJwt;
    private UUID testVariantId;
    private int initialStock;

    @BeforeEach
    void setupWorkflowData() {
        // 1. Register Customer
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("workflow.user." + UUID.randomUUID() + "@example.com");
        registerReq.setPassword("SecureWorkflowPass123!");
        registerReq.setFirstName("Workflow");
        registerReq.setLastName("Tester");

        ResponseEntity<AuthResponse> authRes = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerReq,
            AuthResponse.class
        );
        customerJwt = authRes.getBody().getAccessToken();

        // 2. Create Book and Variant with specific stock
        BookEntity book = bookRepository.save(BookEntity.builder()
            .title("Continuous Delivery in Practice")
            .slug("continuous-delivery-" + UUID.randomUUID())
            .synopsis("Automated release pipelines and cloud deployments.")
            .coverImageUrl("https://images.example.com/cd.jpg")
            .copiesSold(0)
            .build());

        initialStock = 20;
        BookVariantEntity variant = bookVariantRepository.save(BookVariantEntity.builder()
            .book(book)
            .formatType(BookVariantEntity.FormatType.PAPERBACK)
            .isbn("978-0132350-" + UUID.randomUUID().toString().substring(0, 4))
            .sku("CD-PB-" + UUID.randomUUID().toString().substring(0, 4))
            .listPrice(new BigDecimal("700.00"))
            .sellingPrice(new BigDecimal("550.00"))
            .stockQuantity(initialStock)
            .isActive(true)
            .build());

        testVariantId = variant.getId();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(customerJwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Complete E2E Cycle: Address -> Cart -> Checkout -> Stock Deduction -> Payment -> Reorder -> Cancel & Stock Restore")
    void executeFullEcommerceOrderLifecycle() {
        HttpHeaders headers = getAuthHeaders();

        // Step 1: Create Shipping Address
        CreateAddressRequest addressReq = new CreateAddressRequest();
        addressReq.setRecipientName("Workflow Tester");
        addressReq.setRecipientPhone("+919876543210");
        addressReq.setAddressLine1("789 Enterprise Blvd");
        addressReq.setCity("Bengaluru");
        addressReq.setState("Karnataka");
        addressReq.setPincode("560001");
        addressReq.setCountry("India");
        addressReq.setDefault(true);

        ResponseEntity<AddressDTO> addressRes = restTemplate.postForEntity(
            "/api/v1/users/addresses",
            new HttpEntity<>(addressReq, headers),
            AddressDTO.class
        );
        assertThat(addressRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID addressId = addressRes.getBody().getId();

        // Step 2: Add 3 units of Book Variant to Cart
        AddCartItemRequest addCartReq = AddCartItemRequest.builder()
            .variantId(testVariantId)
            .quantity(3)
            .build();

        ResponseEntity<CartDTO> cartRes = restTemplate.postForEntity(
            "/api/v1/cart/items",
            new HttpEntity<>(addCartReq, headers),
            CartDTO.class
        );
        assertThat(cartRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID cartId = cartRes.getBody().getCartId();
        assertThat(cartRes.getBody().getItemCount()).isEqualTo(1);

        // Step 3: Checkout Cart to place Order
        CreateOrderRequest checkoutReq = new CreateOrderRequest();
        checkoutReq.setCartId(cartId);
        checkoutReq.setShippingAddressId(addressId);

        ResponseEntity<OrderDetailResponse> orderRes = restTemplate.postForEntity(
            "/api/v1/orders",
            new HttpEntity<>(checkoutReq, headers),
            OrderDetailResponse.class
        );
        assertThat(orderRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderDetailResponse orderDetail = orderRes.getBody();
        UUID orderId = orderDetail.getOrderId();
        assertThat(orderDetail.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(orderDetail.getOrderNumber()).startsWith("BW-");

        // Step 4: Verify Atomic Inventory Stock Deduction in Database (20 - 3 = 17)
        BookVariantEntity variantAfterOrder = bookVariantRepository.findById(testVariantId).orElseThrow();
        assertThat(variantAfterOrder.getStockQuantity()).isEqualTo(initialStock - 3);

        // Step 5: Initiate Payment Transaction
        InitiatePaymentRequest initPayReq = new InitiatePaymentRequest();
        initPayReq.setOrderId(orderId);
        initPayReq.setPaymentMethod("UPI");
        initPayReq.setPaymentGateway("RAZORPAY");
        initPayReq.setUpiVpa("customer@okhdfcbank");

        ResponseEntity<InitiatePaymentResponse> initPayRes = restTemplate.postForEntity(
            "/api/v1/payments/initiate",
            new HttpEntity<>(initPayReq, headers),
            InitiatePaymentResponse.class
        );
        assertThat(initPayRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID paymentId = initPayRes.getBody().getPaymentId();
        assertThat(initPayRes.getBody().getStatus()).isEqualTo("PENDING_AUTH");

        // Step 6: Verify Payment & Confirm Order
        VerifyPaymentRequest verifyPayReq = new VerifyPaymentRequest();
        verifyPayReq.setPaymentId(paymentId);
        verifyPayReq.setGatewaySignature("simulated_valid_hmac_signature_2025");

        ResponseEntity<VerifyPaymentResponse> verifyPayRes = restTemplate.postForEntity(
            "/api/v1/payments/verify",
            new HttpEntity<>(verifyPayReq, headers),
            VerifyPaymentResponse.class
        );
        assertThat(verifyPayRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyPayRes.getBody().getStatus()).isEqualTo("CAPTURED");
        assertThat(verifyPayRes.getBody().getOrderStatus()).isEqualTo("CONFIRMED");

        // Step 7: 1-Click Reorder ("Buy It Again") -> Loads items back into Cart
        ResponseEntity<CartDTO> reorderRes = restTemplate.postForEntity(
            "/api/v1/orders/" + orderId + "/reorder",
            new HttpEntity<>(null, headers),
            CartDTO.class
        );
        assertThat(reorderRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reorderRes.getBody().getStatus()).isEqualTo("ACTIVE");

        // Step 8: Cancel Order & Verify Inventory Restoration (Stock restored from 17 back to 20)
        CancelOrderRequest cancelReq = new CancelOrderRequest();
        cancelReq.setReason("Testing order cancellation and inventory restoration");

        ResponseEntity<CancelOrderResponse> cancelRes = restTemplate.postForEntity(
            "/api/v1/orders/" + orderId + "/cancel",
            new HttpEntity<>(cancelReq, headers),
            CancelOrderResponse.class
        );
        assertThat(cancelRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelRes.getBody().getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelRes.getBody().isRefundInitiated()).isTrue();

        BookVariantEntity variantAfterCancel = bookVariantRepository.findById(testVariantId).orElseThrow();
        assertThat(variantAfterCancel.getStockQuantity()).isEqualTo(initialStock);
    }
}
