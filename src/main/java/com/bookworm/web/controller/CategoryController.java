package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CategoryDTOs;
import com.bookworm.core.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Taxonomy hierarchy and category browsing.")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get full hierarchical category taxonomy tree", operationId = "getCategoryTree")
    @GetMapping
    public ResponseEntity<List<CategoryDTOs.CategoryNodeDTO>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @Operation(summary = "Get category details", operationId = "getCategoryDetails")
    @GetMapping("/{categoryIdOrSlug}")
    public ResponseEntity<CategoryDTOs.CategoryDetailResponse> getCategory(
        @PathVariable("categoryIdOrSlug") String categoryIdOrSlug
    ) {
        return ResponseEntity.ok(categoryService.getCategoryDetails(categoryIdOrSlug));
    }
}
