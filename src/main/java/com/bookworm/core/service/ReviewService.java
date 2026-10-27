package com.bookworm.core.service;

import com.bookworm.api.v1.dto.ReviewDTOs;
import com.bookworm.core.common.exception.DuplicateResourceException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.ReviewMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.ReviewEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.ReviewRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public ReviewDTOs.BookReviewsResponse getBookReviews(UUID bookId, Integer ratingFilter, Pageable pageable) {
        log.info("Fetching reviews for book ID: {}, ratingFilter: {}", bookId, ratingFilter);

        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        Page<ReviewEntity> reviewPage = (ratingFilter != null)
            ? reviewRepository.findByBookIdAndModerationStatusAndRatingOrderByCreatedAtDesc(bookId, "APPROVED", ratingFilter, pageable)
            : reviewRepository.findByBookIdAndModerationStatusOrderByCreatedAtDesc(bookId, "APPROVED", pageable);

        List<ReviewRepository.RatingCountProjection> distribution = reviewRepository.getRatingDistributionByBookId(bookId);
        Map<String, Long> distMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distMap.put(String.valueOf(i), 0L);
        }
        for (ReviewRepository.RatingCountProjection proj : distribution) {
            distMap.put(String.valueOf(proj.getRating()), proj.getCount());
        }

        return ReviewDTOs.BookReviewsResponse.builder()
            .bookId(book.getId())
            .averageRating(book.getAverageRating())
            .totalReviews(book.getRatingsCount())
            .ratingDistribution(distMap)
            .content(reviewMapper.toDTOList(reviewPage.getContent()))
            .page(reviewPage.getNumber() + 1)
            .size(reviewPage.getSize())
            .totalElements(reviewPage.getTotalElements())
            .totalPages(reviewPage.getTotalPages())
            .build();
    }

    @Transactional
    public ReviewDTOs.ReviewDTO submitReview(UUID userId, UUID bookId, ReviewDTOs.SubmitReviewRequest request) {
        log.info("User ID: {} submitting review for book ID: {}, rating: {}", userId, bookId, request.getRating());

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateResourceException("Review", "bookId", bookId);
        }

        ReviewEntity review = reviewMapper.toEntity(request);
        review.setUser(user);
        review.setBook(book);
        review.setVerifiedPurchase(true);
        review.setModerationStatus("APPROVED");

        ReviewEntity savedReview = reviewRepository.save(review);

        // Recalculate Book Average Rating & Review Count
        int currentCount = book.getRatingsCount() != null ? book.getRatingsCount() : 0;
        BigDecimal currentAvg = book.getAverageRating() != null ? book.getAverageRating() : BigDecimal.ZERO;

        BigDecimal totalScore = currentAvg.multiply(BigDecimal.valueOf(currentCount)).add(BigDecimal.valueOf(request.getRating()));
        int newCount = currentCount + 1;
        BigDecimal newAvg = totalScore.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        bookRepository.updateRatingsAggregate(bookId, newAvg, newCount);

        log.info("Review submitted successfully. New book average rating: {}, count: {}", newAvg, newCount);
        return reviewMapper.toDTO(savedReview);
    }
}
