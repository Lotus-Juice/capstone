package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CartDTOs.*;
import com.bookworm.core.service.CartService;
import com.bookworm.web.advice.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Web Unit Tests")
class CartControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartDTO sampleCartDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        sampleCartDto = CartDTO.builder()
            .cartId(UUID.randomUUID())
            .status("ACTIVE")
            .subtotal(new BigDecimal("999.00"))
            .itemCount(1)
            .items(List.of(
                CartItemDTO.builder()
                    .itemId(UUID.randomUUID())
                    .variantId(UUID.randomUUID())
                    .bookTitle("Clean Code")
                    .format("PAPERBACK")
                    .quantity(1)
                    .unitPrice(new BigDecimal("999.00"))
                    .lineTotal(new BigDecimal("999.00"))
                    .build()
            ))
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/cart")
    class GetCartTests {

        @Test
        @DisplayName("Should retrieve active cart with 200 OK")
        void shouldGetActiveCart() throws Exception {
            given(cartService.getActiveCart(any(), any())).willReturn(sampleCartDto);

            mockMvc.perform(get("/api/v1/cart")
                    .header("X-Guest-Session-Token", "guest-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.itemCount").value(1))
                .andExpect(jsonPath("$.subtotal").value(999.00));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cart/items")
    class AddItemTests {

        @Test
        @DisplayName("Should add item to cart with 200 OK")
        void shouldAddItemToCart() throws Exception {
            AddCartItemRequest request = AddCartItemRequest.builder()
                .variantId(UUID.randomUUID())
                .quantity(1)
                .build();

            given(cartService.addItemToCart(any(), any(), any(AddCartItemRequest.class))).willReturn(sampleCartDto);

            mockMvc.perform(post("/api/v1/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cart/items/{cartItemId}")
    class UpdateQuantityTests {

        @Test
        @DisplayName("Should update cart item quantity with 200 OK")
        void shouldUpdateQuantity() throws Exception {
            UUID itemId = UUID.randomUUID();
            UpdateCartItemRequest request = new UpdateCartItemRequest();
            request.setQuantity(3);

            given(cartService.updateItemQuantity(any(), any(), eq(itemId), eq(3))).willReturn(sampleCartDto);

            mockMvc.perform(patch("/api/v1/cart/items/" + itemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId}")
    class RemoveItemTests {

        @Test
        @DisplayName("Should delete cart item with 200 OK")
        void shouldRemoveItem() throws Exception {
            UUID itemId = UUID.randomUUID();
            given(cartService.removeItemFromCart(any(), any(), eq(itemId))).willReturn(sampleCartDto);

            mockMvc.perform(delete("/api/v1/cart/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cart/merge")
    class MergeCartTests {

        @Test
        @DisplayName("Should merge guest cart into user cart with 200 OK")
        void shouldMergeGuestCart() throws Exception {
            MergeCartRequest request = new MergeCartRequest();
            request.setGuestSessionToken("guest-token-abc");

            given(cartService.mergeGuestCart(any(), eq("guest-token-abc"))).willReturn(sampleCartDto);

            mockMvc.perform(post("/api/v1/cart/merge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }
}
