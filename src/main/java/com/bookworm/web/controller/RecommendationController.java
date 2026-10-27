package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.RecommendationDTOs;
import com.bookworm.core.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Home showcase carousels and related reads.")
public class RecommendationController {

    private final CatalogService catalogService;

    @Operation(summary = "Get Home Page showcase feeds (Recommended, Bestsellers, New Launches)", operationId = "getHomeRecommendations")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/home")
    public ResponseEntity<RecommendationDTOs.HomeRecommendationsResponse> getHomeFeeds(
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(catalogService.getHomeRecommendations(userId));
    }

    @Operation(summary = "Get related / cross-sell reads for a specific book", operationId = "getRelatedReads")
    @GetMapping("/related/{bookId}")
    public ResponseEntity<List<RecommendationDTOs.RelatedBookDTO>> getRelated(
        @PathVariable("bookId") UUID bookId,
        @RequestParam(value = "limit", defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(catalogService.getRelatedReads(bookId, limit));
    }
}
