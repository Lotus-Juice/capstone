package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReviewDTOs {

    private ReviewDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitReviewRequest {

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1 star")
        @Max(value = 5, message = "Rating cannot exceed 5 stars")
        private Integer rating;

        @Size(max = 500, message = "Review comment cannot exceed 500 characters")
        private String reviewText;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDTO {
        private UUID reviewId;
        private String userName;
        private int rating;
        private String reviewText;
        private boolean isVerifiedPurchase;
        private Instant createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewsResponse {
        private UUID bookId;
        private BigDecimal averageRating;
        private int totalReviews;
        private Map<String, Long> ratingDistribution;
        private List<ReviewDTO> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
