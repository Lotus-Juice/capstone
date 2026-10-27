package com.bookworm.api.v1.dto;

import lombok.*;

import java.util.List;

public final class SearchDTOs {

    private SearchDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchResultResponse {
        private List<BookDTOs.BookSummaryDTO> content;
        private FacetsDTO facets;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetsDTO {
        private List<FacetItemDTO> categories;
        private List<FacetItemDTO> formats;
        private List<FacetItemDTO> languages;
        private List<PriceRangeFacetDTO> priceRanges;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetItemDTO {
        private String name;
        private long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceRangeFacetDTO {
        private String range;
        private long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchSuggestionsResponse {
        private List<SuggestionItemDTO> suggestions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestionItemDTO {
        private String type; // BOOK, AUTHOR, CATEGORY
        private String id;
        private String text;
    }
}
