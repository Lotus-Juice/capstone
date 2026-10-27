package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.SearchDTOs;
import com.bookworm.core.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text and multi-faceted search and autocomplete suggestions.")
public class SearchController {

    private final CatalogService catalogService;

    @Operation(summary = "Search books with full-text and multi-faceted filtering", operationId = "searchBooks")
    @GetMapping
    public ResponseEntity<SearchDTOs.SearchResultResponse> search(
        @RequestParam("q") String query,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "format", required = false) String format,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(catalogService.searchBooks(query, category, format, language, minPrice, maxPrice, pageable));
    }

    @Operation(summary = "Autocomplete search suggestions", operationId = "getSearchSuggestions")
    @GetMapping("/suggestions")
    public ResponseEntity<SearchDTOs.SearchSuggestionsResponse> suggestions(
        @RequestParam("q") String query
    ) {
        return ResponseEntity.ok(catalogService.getSearchSuggestions(query));
    }
}
