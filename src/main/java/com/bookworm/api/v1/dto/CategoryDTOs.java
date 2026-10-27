package com.bookworm.api.v1.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CategoryDTOs {

    private CategoryDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryNodeDTO {
        private UUID id;
        private String name;
        private String slug;
        private String description;
        private int displayOrder;
        @Builder.Default
        private List<CategoryNodeDTO> subcategories = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDetailResponse {
        private UUID id;
        private String name;
        private String slug;
        private String description;
        private CategorySummaryDTO parentCategory;
        private long totalBooks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummaryDTO {
        private UUID id;
        private String name;
        private String slug;
    }
}
