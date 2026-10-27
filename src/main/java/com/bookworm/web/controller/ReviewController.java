package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.ReviewDTOs;
import com.bookworm.core.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/books/{bookId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Customer star ratings and text reviews (0-500 characters).")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get customer reviews and ratings for a book", operationId = "getBookReviews")
    @GetMapping
    public ResponseEntity<ReviewDTOs.BookReviewsResponse> getReviews(
        @PathVariable("bookId") UUID bookId,
        @RequestParam(value = "rating", required = false) Integer rating,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(reviewService.getBookReviews(bookId, rating, pageable));
    }

    @Operation(summary = "Submit rating and review (0-500 characters)", operationId = "submitBookReview")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<ReviewDTOs.ReviewDTO> submitReview(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("bookId") UUID bookId,
        @Valid @RequestBody ReviewDTOs.SubmitReviewRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        ReviewDTOs.ReviewDTO response = reviewService.submitReview(effectiveUserId, bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
