package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.BookDTOs;
import com.bookworm.core.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Product catalog, multi-format variants, and details.")
public class BookController {

    private final CatalogService catalogService;

    @Operation(summary = "Browse and filter books catalog", operationId = "listBooks")
    @GetMapping
    public ResponseEntity<Page<BookDTOs.BookSummaryDTO>> listBooks(
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "format", required = false) String format,
        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(value = "sortBy", defaultValue = "relevance") String sortBy,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(catalogService.listBooks(category, language, format, minPrice, maxPrice, sortBy, pageable));
    }

    @Operation(summary = "Get complete book details page (PDP)", operationId = "getBookDetails")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book details page"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{bookIdOrSlug}")
    public ResponseEntity<BookDTOs.BookDetailResponse> getBook(
        @PathVariable("bookIdOrSlug") String bookIdOrSlug,
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(catalogService.getBookDetails(bookIdOrSlug, userId));
    }
}
