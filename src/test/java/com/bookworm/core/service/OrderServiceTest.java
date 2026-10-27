package com.bookworm.core.service;

import com.bookworm.api.v1.dto.OrderDTOs.CancelOrderRequest;
import com.bookworm.api.v1.dto.OrderDTOs.CancelOrderResponse;
import com.bookworm.api.v1.dto.OrderDTOs.CreateOrderRequest;
import com.bookworm.api.v1.dto.OrderDTOs.OrderDetailResponse;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.InsufficientStockException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.OrderMapper;
import com.bookworm.infra.persistence.entity.*;
import com.bookworm.infra.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private BookVariantRepository bookVariantRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository couponRedemptionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UserEntity sampleUser;
    private UserAddressEntity sampleAddress;
    private BookEntity sampleBook;
    private BookVariantEntity sampleVariant;
    private CartEntity sampleCart;
    private CartItemEntity sampleCartItem;
    private OrderEntity sampleOrder;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
            .id(UUID.randomUUID())
            .email("bob@example.com")
            .firstName("Bob")
            .lastName("Builder")
            .role(UserEntity.Role.CUSTOMER)
            .build();

        sampleAddress = UserAddressEntity.builder()
            .id(UUID.randomUUID())
            .user(sampleUser)
            .recipientName("Bob Builder")
            .recipientPhone("+919876543210")
            .addressLine1("123 Tech Park")
            .city("Bengaluru")
            .state("Karnataka")
            .pincode("560001")
            .country("India")
            .build();

        sampleBook = BookEntity.builder()
            .id(UUID.randomUUID())
            .title("Clean Architecture")
            .slug("clean-architecture")
            .bookAuthors(new ArrayList<>())
            .build();

        sampleVariant = BookVariantEntity.builder()
            .id(UUID.randomUUID())
            .book(sampleBook)
            .formatType(BookVariantEntity.FormatType.PAPERBACK)
            .isbn("9780134494166")
            .sku("CA-PAPERBACK-01")
            .listPrice(new BigDecimal("800.00"))
            .sellingPrice(new BigDecimal("600.00"))
            .stockQuantity(10)
            .isActive(true)
            .build();

        sampleCart = CartEntity.builder()
            .id(UUID.randomUUID())
            .user(sampleUser)
            .status(CartEntity.CartStatus.ACTIVE)
            .items(new ArrayList<>())
            .build();

        sampleCartItem = CartItemEntity.builder()
            .id(UUID.randomUUID())
            .cart(sampleCart)
            .variant(sampleVariant)
            .quantity(1)
            .unitPriceAtAddition(new BigDecimal("600.00"))
            .build();

        sampleCart.getItems().add(sampleCartItem);

        sampleOrder = OrderEntity.builder()
            .id(UUID.randomUUID())
            .orderNumber("BW-2025-12345")
            .user(sampleUser)
            .shippingAddress(sampleAddress)
            .itemsSubtotal(new BigDecimal("600.00"))
            .taxAmount(new BigDecimal("72.00"))
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .grandTotal(new BigDecimal("672.00"))
            .status("CONFIRMED")
            .placedAt(Instant.now())
            .lineItems(new ArrayList<>())
            .build();

        OrderLineItemEntity sampleLineItem = OrderLineItemEntity.builder()
            .id(UUID.randomUUID())
            .order(sampleOrder)
            .variant(sampleVariant)
            .bookTitleSnapshot("Clean Architecture")
            .authorNameSnapshot("Robert C. Martin")
            .formatTypeSnapshot(BookVariantEntity.FormatType.PAPERBACK)
            .unitPrice(new BigDecimal("600.00"))
            .quantity(1)
            .lineTotal(new BigDecimal("600.00"))
            .build();

        sampleOrder.getLineItems().add(sampleLineItem);
    }

    @Nested
    @DisplayName("Checkout Order")
    class CheckoutTests {

        @Test
        @DisplayName("Should successfully checkout cart, deduct stock atomically, and create order")
        void shouldCheckoutSuccessfully() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCartId(sampleCart.getId());
            request.setShippingAddressId(sampleAddress.getId());

            given(cartRepository.findById(sampleCart.getId())).willReturn(Optional.of(sampleCart));
            given(userAddressRepository.findByIdAndUserId(sampleAddress.getId(), sampleUser.getId()))
                .willReturn(Optional.of(sampleAddress));
            given(bookVariantRepository.deductStockIfAvailable(sampleVariant.getId(), 1)).willReturn(1);
            given(orderRepository.save(any(OrderEntity.class))).willReturn(sampleOrder);
            given(orderMapper.toDetailResponse(any(OrderEntity.class))).willReturn(new OrderDetailResponse());

            OrderDetailResponse response = orderService.checkoutOrder(sampleUser.getId(), request);

            assertThat(response).isNotNull();
            assertThat(sampleCart.getStatus()).isEqualTo(CartEntity.CartStatus.CHECKED_OUT);
            verify(bookVariantRepository).deductStockIfAvailable(sampleVariant.getId(), 1);
            verify(orderRepository).save(any(OrderEntity.class));
            verify(cartRepository).save(sampleCart);
        }

        @Test
        @DisplayName("Should throw InsufficientStockException if atomic stock deduction returns 0")
        void shouldThrowExceptionWhenStockInsufficientAtCheckout() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCartId(sampleCart.getId());
            request.setShippingAddressId(sampleAddress.getId());

            given(cartRepository.findById(sampleCart.getId())).willReturn(Optional.of(sampleCart));
            given(userAddressRepository.findByIdAndUserId(sampleAddress.getId(), sampleUser.getId()))
                .willReturn(Optional.of(sampleAddress));
            given(bookVariantRepository.deductStockIfAvailable(sampleVariant.getId(), 1)).willReturn(0);

            assertThatThrownBy(() -> orderService.checkoutOrder(sampleUser.getId(), request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for SKU: CA-PAPERBACK-01");
        }
    }

    @Nested
    @DisplayName("Order Cancellation")
    class CancellationTests {

        @Test
        @DisplayName("Should cancel confirmed order and restore deducted stock quantity")
        void shouldCancelOrderAndRestoreStock() {
            CancelOrderRequest request = new CancelOrderRequest();
            request.setReason("Changed mind");

            given(orderRepository.findByIdAndUserIdWithDetails(sampleOrder.getId(), sampleUser.getId()))
                .willReturn(Optional.of(sampleOrder));

            CancelOrderResponse response = orderService.cancelOrder(sampleUser.getId(), sampleOrder.getId(), request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("CANCELLED");
            assertThat(response.isRefundInitiated()).isTrue();
            verify(bookVariantRepository).restoreStock(sampleVariant.getId(), 1);
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("Should throw BusinessException when attempting to cancel an already dispatched order")
        void shouldThrowExceptionWhenOrderDispatched() {
            sampleOrder.setStatus("DISPATCHED");
            CancelOrderRequest request = new CancelOrderRequest();
            request.setReason("Too late to cancel");

            given(orderRepository.findByIdAndUserIdWithDetails(sampleOrder.getId(), sampleUser.getId()))
                .willReturn(Optional.of(sampleOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(sampleUser.getId(), sampleOrder.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Orders in status 'DISPATCHED' cannot be cancelled");
        }
    }
}
