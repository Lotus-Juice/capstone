package com.bookworm.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BookDTOs {

    private BookDTOs() {}

    public enum BookFormat {
        PAPERBACK, HARDCOVER, EBOOK, AUDIO
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookVariantDTO {
        private UUID variantId;
        private String formatType;
        private String isbn;
        private String sku;
        private BigDecimal listPrice;
        private BigDecimal sellingPrice;
        private String currency;
        private boolean inStock;
        private int stockQuantity;
        private Integer weightGrams;
        private Integer pageCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookSummaryDTO {
        private UUID id;
        private String title;
        private String slug;
        private AuthorSummaryDTO author;
        private String publisher;
        private String coverImageUrl;
        private String primaryCategory;
        private BigDecimal averageRating;
        private int ratingsCount;
        private List<String> availableFormats;
        private BigDecimal startingPrice;
        private String currency;
        private String deliveryEstimate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorSummaryDTO {
        private UUID id;
        private String name;
        private String slug;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookCategoryDTO {
        private UUID id;
        private String name;
        private String slug;
        private boolean isPrimary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetailResponse {
        private UUID id;
        private String title;
        private String subtitle;
        private String slug;
        private String synopsis;
        private String language;
        private String coverImageUrl;
        private LocalDate publishedDate;
        private int copiesSold;
        private BigDecimal averageRating;
        private int ratingsCount;
        private boolean isWishlisted;
        private AuthorDTOs.AuthorDTO author;
        private PublisherDTOs.PublisherDTO publisher;
        private List<BookCategoryDTO> categories;
        private List<BookVariantDTO> variants;
    }
}
