package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CartDTOs.AddToCartRequest;
import com.bookworm.api.v1.dto.CartDTOs.CartResponse;
import com.bookworm.api.v1.dto.CartDTOs.UpdateCartItemRequest;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.InsufficientStockException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.CartMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import com.bookworm.infra.persistence.entity.CartEntity;
import com.bookworm.infra.persistence.entity.CartItemEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.BookVariantRepository;
import com.bookworm.infra.persistence.repository.CartItemRepository;
import com.bookworm.infra.persistence.repository.CartRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookVariantRepository bookVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private UserEntity sampleUser;
    private BookEntity sampleBook;
    private BookVariantEntity sampleVariant;
    private CartEntity sampleCart;
    private CartItemEntity sampleCartItem;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
            .id(UUID.randomUUID())
            .email("customer@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .role(UserEntity.Role.CUSTOMER)
            .build();

        sampleBook = BookEntity.builder()
            .id(UUID.randomUUID())
            .title("Domain-Driven Design")
            .slug("domain-driven-design")
            .authors(new ArrayList<>())
            .build();

        sampleVariant = BookVariantEntity.builder()
            .id(UUID.randomUUID())
            .book(sampleBook)
            .formatType(BookVariantEntity.FormatType.HARDCOVER)
            .isbn("9780321125217")
            .sku("DDD-HARDCOVER-01")
            .listPrice(new BigDecimal("1200.00"))
            .sellingPrice(new BigDecimal("999.00"))
            .stockQuantity(15)
            .isActive(true)
            .build();

        sampleCart = CartEntity.builder()
            .id(UUID.randomUUID())
            .user(sampleUser)
            .status(CartEntity.CartStatus.ACTIVE)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .items(new ArrayList<>())
            .build();

        sampleCartItem = CartItemEntity.builder()
            .id(UUID.randomUUID())
            .cart(sampleCart)
            .variant(sampleVariant)
            .quantity(2)
            .unitPriceAtAddition(new BigDecimal("999.00"))
            .build();
    }

    @Nested
    @DisplayName("Add Item to Cart")
    class AddItemTests {

        @Test
        @DisplayName("Should successfully add a new item to the user's active cart")
        void shouldAddItemToCartSuccessfully() {
            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(sampleVariant.getId());
            request.setQuantity(2);

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(cartRepository.findByUserIdAndStatus(sampleUser.getId(), CartEntity.CartStatus.ACTIVE))
                .willReturn(Optional.of(sampleCart));
            given(bookVariantRepository.findById(sampleVariant.getId())).willReturn(Optional.of(sampleVariant));
            given(cartItemRepository.findByCartIdAndVariantId(sampleCart.getId(), sampleVariant.getId()))
                .willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItemEntity.class))).willReturn(sampleCartItem);
            given(cartMapper.toDto(any(CartEntity.class))).willReturn(new CartResponse());

            CartResponse response = cartService.addItem(sampleUser.getId(), null, request);

            assertThat(response).isNotNull();
            verify(cartItemRepository).save(any(CartItemEntity.class));
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available inventory")
        void shouldThrowExceptionWhenOutOfStock() {
            sampleVariant.setStockQuantity(1); // Only 1 available

            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(sampleVariant.getId());
            request.setQuantity(5); // Requesting 5

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(cartRepository.findByUserIdAndStatus(sampleUser.getId(), CartEntity.CartStatus.ACTIVE))
                .willReturn(Optional.of(sampleCart));
            given(bookVariantRepository.findById(sampleVariant.getId())).willReturn(Optional.of(sampleVariant));

            assertThatThrownBy(() -> cartService.addItem(sampleUser.getId(), null, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Requested quantity (5) exceeds available stock (1)");
        }

        @Test
        @DisplayName("Should enforce per-item quantity cap of 20")
        void shouldEnforceMaxPerItemCap() {
            sampleCart.getItems().add(sampleCartItem);
            sampleCartItem.setQuantity(15);

            AddToCartRequest request = new AddToCartRequest();
            request.setVariantId(sampleVariant.getId());
            request.setQuantity(10); // 15 + 10 = 25 > 20

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(cartRepository.findByUserIdAndStatus(sampleUser.getId(), CartEntity.CartStatus.ACTIVE))
                .willReturn(Optional.of(sampleCart));
            given(bookVariantRepository.findById(sampleVariant.getId())).willReturn(Optional.of(sampleVariant));
            given(cartItemRepository.findByCartIdAndVariantId(sampleCart.getId(), sampleVariant.getId()))
                .willReturn(Optional.of(sampleCartItem));

            assertThatThrownBy(() -> cartService.addItem(sampleUser.getId(), null, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot exceed maximum allowable limit of 20 units per item");
        }
    }

    @Nested
    @DisplayName("Update Cart Item Quantity")
    class UpdateItemTests {

        @Test
        @DisplayName("Should update cart item quantity when within stock bounds")
        void shouldUpdateCartItemQuantity() {
            sampleCart.getItems().add(sampleCartItem);

            UpdateCartItemRequest request = new UpdateCartItemRequest();
            request.setQuantity(4);

            given(cartItemRepository.findById(sampleCartItem.getId())).willReturn(Optional.of(sampleCartItem));
            given(cartMapper.toDto(any(CartEntity.class))).willReturn(new CartResponse());

            CartResponse response = cartService.updateItemQuantity(sampleUser.getId(), null, sampleCartItem.getId(), request);

            assertThat(response).isNotNull();
            assertThat(sampleCartItem.getQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when item does not exist")
        void shouldThrowExceptionWhenItemNotFound() {
            UUID randomItemId = UUID.randomUUID();
            UpdateCartItemRequest request = new UpdateCartItemRequest();
            request.setQuantity(3);

            given(cartItemRepository.findById(randomItemId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateItemQuantity(sampleUser.getId(), null, randomItemId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found");
        }
    }

    @Nested
    @DisplayName("Merge Guest Cart to Member Cart")
    class MergeCartTests {

        @Test
        @DisplayName("Should merge guest cart items into member cart on login")
        void shouldMergeGuestCartSuccessfully() {
            String guestToken = "guest-session-uuid-1234";

            CartEntity guestCart = CartEntity.builder()
                .id(UUID.randomUUID())
                .guestSessionToken(guestToken)
                .status(CartEntity.CartStatus.ACTIVE)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .items(new ArrayList<>())
                .build();

            CartItemEntity guestItem = CartItemEntity.builder()
                .id(UUID.randomUUID())
                .cart(guestCart)
                .variant(sampleVariant)
                .quantity(1)
                .unitPriceAtAddition(new BigDecimal("999.00"))
                .build();
            guestCart.getItems().add(guestItem);

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(cartRepository.findByUserIdAndStatus(sampleUser.getId(), CartEntity.CartStatus.ACTIVE))
                .willReturn(Optional.of(sampleCart));
            given(cartRepository.findByGuestSessionTokenAndStatus(guestToken, CartEntity.CartStatus.ACTIVE))
                .willReturn(Optional.of(guestCart));
            given(cartItemRepository.findByCartIdAndVariantId(sampleCart.getId(), sampleVariant.getId()))
                .willReturn(Optional.empty());
            given(cartMapper.toDto(any(CartEntity.class))).willReturn(new CartResponse());

            CartResponse response = cartService.mergeGuestCart(sampleUser.getId(), guestToken);

            assertThat(response).isNotNull();
            assertThat(guestCart.getStatus()).isEqualTo(CartEntity.CartStatus.MERGED);
            verify(cartItemRepository).save(any(CartItemEntity.class));
            verify(cartRepository).save(guestCart);
        }
    }
}
