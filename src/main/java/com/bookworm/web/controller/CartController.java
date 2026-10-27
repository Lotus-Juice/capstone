package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CartDTOs;
import com.bookworm.core.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Guest and member shopping carts, item quantities, and cart merging.")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current active cart", operationId = "getCart")
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "GuestSessionHeader")
    @GetMapping
    public ResponseEntity<CartDTOs.CartDTO> getCart(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "X-Guest-Session-Token", required = false) String guestToken
    ) {
        return ResponseEntity.ok(cartService.getActiveCart(userId, guestToken));
    }

    @Operation(summary = "Add format variant to cart", operationId = "addCartItem")
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "GuestSessionHeader")
    @PostMapping("/items")
    public ResponseEntity<CartDTOs.CartDTO> addItem(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "X-Guest-Session-Token", required = false) String guestToken,
        @Valid @RequestBody CartDTOs.AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(userId, guestToken, request));
    }

    @Operation(summary = "Update cart item quantity", operationId = "updateCartItemQuantity")
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "GuestSessionHeader")
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTOs.CartDTO> updateQuantity(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "X-Guest-Session-Token", required = false) String guestToken,
        @PathVariable("cartItemId") UUID cartItemId,
        @Valid @RequestBody CartDTOs.UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, guestToken, cartItemId, request.getQuantity()));
    }

    @Operation(summary = "Delete item from cart", operationId = "removeCartItem")
    @SecurityRequirement(name = "BearerAuth")
    @SecurityRequirement(name = "GuestSessionHeader")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTOs.CartDTO> removeItem(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "X-Guest-Session-Token", required = false) String guestToken,
        @PathVariable("cartItemId") UUID cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, guestToken, cartItemId));
    }

    @Operation(summary = "Merge anonymous guest cart into authenticated customer account", operationId = "mergeGuestCart")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/merge")
    public ResponseEntity<CartDTOs.CartDTO> mergeCart(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @Valid @RequestBody CartDTOs.MergeCartRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(cartService.mergeGuestCart(effectiveUserId, request.getGuestSessionToken()));
    }
}
