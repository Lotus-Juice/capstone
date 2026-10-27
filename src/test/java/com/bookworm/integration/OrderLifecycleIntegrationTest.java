package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.api.v1.dto.CartDTOs.AddToCartRequest;
import com.bookworm.api.v1.dto.CartDTOs.CartResponse;
import com.bookworm.api.v1.dto.OrderDTOs.CreateOrderRequest;
import com.bookworm.api.v1.dto.OrderDTOs.OrderDetailResponse;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import com.bookworm.infra.persistence.entity.UserAddressEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.BookVariantRepository;
import com.bookworm.infra.persistence.repository.UserAddressRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Lifecycle Integration Test (PostgreSQL Testcontainers)")
class OrderLifecycleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookVariantRepository bookVariantRepository;

    private String userJwtToken;
    private UUID userId;
    private UUID addressId;
    private UUID variantId;

    @BeforeEach
    void setUpTestData() {
        // Register customer
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("order.flow." + UUID.randomUUID() + "@example.com");
        registerReq.setPassword("P@ssword123!");
        registerReq.setFirstName("Order");
        registerReq.setLastName("Tester");

        ResponseEntity<AuthResponse> authRes = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerReq,
            AuthResponse.class
        );

        userJwtToken = authRes.getBody().getAccessToken();
        UserEntity user = userRepository.findByEmailIgnoreCase(registerReq.getEmail()).orElseThrow();
        userId = user.getId();

        // Create shipping address
        UserAddressEntity address = UserAddressEntity.builder()
            .user(user)
            .recipientName("Order Tester")
            .recipientPhone("+919988776655")
            .addressLine1("456 Innovation Road")
            .city("Mumbai")
            .state("Maharashtra")
            .pincode("400001")
            .country("India")
            .isDefault(true)
            .build();
        address = userAddressRepository.save(address);
        addressId = address.getId();

        // Create test book and variant
        BookEntity book = BookEntity.builder()
            .title("Spring Boot in Action")
            .slug("spring-boot-in-action-" + UUID.randomUUID())
            .synopsis("Comprehensive guide to enterprise Spring Boot applications.")
            .coverImageUrl("https://images.example.com/books/spring-in-action.jpg")
            .build();
        book = bookRepository.save(book);

        BookVariantEntity variant = BookVariantEntity.builder()
            .book(book)
            .formatType(BookVariantEntity.FormatType.PAPERBACK)
            .isbn("9781617292545-" + UUID.randomUUID().toString().substring(0, 4))
            .sku("SB-PAPERBACK-" + UUID.randomUUID().toString().substring(0, 4))
            .listPrice(new BigDecimal("999.00"))
            .sellingPrice(new BigDecimal("799.00"))
            .stockQuantity(50)
            .isActive(true)
            .build();
        variant = bookVariantRepository.save(variant);
        variantId = variant.getId();
    }

    @Test
    @DisplayName("Complete E2E Order Flow: Add Item to Cart -> Checkout -> Stock Deduction")
    void completeOrderPlacementAndStockDeductionFlow() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userJwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. Add item to user's cart
        AddToCartRequest addCartReq = new AddToCartRequest();
        addCartReq.setVariantId(variantId);
        addCartReq.setQuantity(2);

        HttpEntity<AddToCartRequest> addCartEntity = new HttpEntity<>(addCartReq, headers);
        ResponseEntity<CartResponse> cartRes = restTemplate.postForEntity(
            "/api/v1/cart/items",
            addCartEntity,
            CartResponse.class
        );

        assertThat(cartRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cartRes.getBody()).isNotNull();
        UUID cartId = cartRes.getBody().getCartId();

        // 2. Checkout the cart to create order
        CreateOrderRequest checkoutReq = new CreateOrderRequest();
        checkoutReq.setCartId(cartId);
        checkoutReq.setShippingAddressId(addressId);

        HttpEntity<CreateOrderRequest> checkoutEntity = new HttpEntity<>(checkoutReq, headers);
        ResponseEntity<OrderDetailResponse> orderRes = restTemplate.postForEntity(
            "/api/v1/orders/checkout",
            checkoutEntity,
            OrderDetailResponse.class
        );

        assertThat(orderRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderRes.getBody()).isNotNull();
        assertThat(orderRes.getBody().getOrderNumber()).startsWith("BW-");
        assertThat(orderRes.getBody().getStatus()).isEqualTo("PENDING_PAYMENT");

        // 3. Verify stock was atomically deducted from 50 to 48
        BookVariantEntity updatedVariant = bookVariantRepository.findById(variantId).orElseThrow();
        assertThat(updatedVariant.getStockQuantity()).isEqualTo(48);
    }
}
