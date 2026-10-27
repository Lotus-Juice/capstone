package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.WishlistDTOs;
import com.bookworm.core.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Customer book wishlists and saved reads.")
@SecurityRequirement(name = "BearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get current user wishlist", operationId = "getWishlist")
    @GetMapping
    public ResponseEntity<WishlistDTOs.WishlistResponse> getWishlist(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(wishlistService.getWishlist(effectiveUserId, pageable));
    }

    @Operation(summary = "Add book to wishlist", operationId = "addToWishlist")
    @PostMapping("/items/{bookId}")
    public ResponseEntity<WishlistDTOs.WishlistActionResponse> addItem(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("bookId") UUID bookId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        WishlistDTOs.WishlistActionResponse response = wishlistService.addToWishlist(effectiveUserId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remove book from wishlist", operationId = "removeFromWishlist")
    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> removeItem(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("bookId") UUID bookId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        wishlistService.removeFromWishlist(effectiveUserId, bookId);
        return ResponseEntity.noContent().build();
    }
}
