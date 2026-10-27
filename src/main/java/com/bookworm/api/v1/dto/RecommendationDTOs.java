package com.bookworm.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class RecommendationDTOs {

    private RecommendationDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedBookDTO {
        private UUID id;
        private String title;
        private String author;
        private String format;
        private BigDecimal sellingPrice;
        private String coverImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HomeRecommendationsResponse {
        private List<BookDTOs.BookSummaryDTO> recommendedForYou;
        private List<BookDTOs.BookSummaryDTO> bestsellersThisMonth;
        private List<BookDTOs.BookSummaryDTO> newLaunches;
    }
}
