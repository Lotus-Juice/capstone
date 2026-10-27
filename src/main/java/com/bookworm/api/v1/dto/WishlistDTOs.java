package com.bookworm.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WishlistDTOs {

    private WishlistDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistItemDTO {
        private UUID wishlistId;
        private UUID bookId;
        private String title;
        private String authorName;
        private String coverImageUrl;
        private BigDecimal sellingPrice;
        private BigDecimal rating;
        private Instant addedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistResponse {
        private List<WishlistItemDTO> items;
        private long totalItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WishlistActionResponse {
        private UUID bookId;
        private boolean inWishlist;
        private String message;
    }
}
