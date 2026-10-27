package com.bookworm.core.service;

import com.bookworm.api.v1.dto.ReviewDTOs.BookReviewsResponse;
import com.bookworm.api.v1.dto.ReviewDTOs.ReviewDTO;
import com.bookworm.api.v1.dto.ReviewDTOs.SubmitReviewRequest;
import com.bookworm.core.common.exception.DuplicateResourceException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.ReviewMapper;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.ReviewEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.ReviewRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UserEntity sampleUser;
    private BookEntity sampleBook;
    private ReviewEntity sampleReview;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
            .id(UUID.randomUUID())
            .firstName("Sarah")
            .lastName("Connor")
            .email("sarah@example.com")
            .build();

        sampleBook = BookEntity.builder()
            .id(UUID.randomUUID())
            .title("Atomic Habits")
            .averageRating(new BigDecimal("4.00"))
            .ratingsCount(1)
            .build();

        sampleReview = ReviewEntity.builder()
            .id(UUID.randomUUID())
            .user(sampleUser)
            .book(sampleBook)
            .rating(5)
            .reviewText("Phenomenal and life-changing read!")
            .isVerifiedPurchase(true)
            .moderationStatus("APPROVED")
            .build();
    }

    @Nested
    @DisplayName("Submit Review")
    class SubmitReviewTests {

        @Test
        @DisplayName("Should submit review and recalculate book aggregate rating")
        void shouldSubmitReviewSuccessfully() {
            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setRating(5);
            request.setReviewText("Phenomenal and life-changing read!");

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(bookRepository.findById(sampleBook.getId())).willReturn(Optional.of(sampleBook));
            given(reviewRepository.existsByUserIdAndBookId(sampleUser.getId(), sampleBook.getId())).willReturn(false);
            given(reviewMapper.toEntity(request)).willReturn(sampleReview);
            given(reviewRepository.save(any(ReviewEntity.class))).willReturn(sampleReview);
            given(reviewMapper.toDTO(sampleReview)).willReturn(ReviewDTO.builder().id(sampleReview.getId()).rating(5).build());

            ReviewDTO response = reviewService.submitReview(sampleUser.getId(), sampleBook.getId(), request);

            assertThat(response).isNotNull();
            assertThat(response.getRating()).isEqualTo(5);
            verify(bookRepository).updateRatingsAggregate(eq(sampleBook.getId()), eq(new BigDecimal("4.50")), eq(2));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when user has already reviewed the book")
        void shouldThrowExceptionOnDuplicateReview() {
            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setRating(5);

            given(userRepository.findById(sampleUser.getId())).willReturn(Optional.of(sampleUser));
            given(bookRepository.findById(sampleBook.getId())).willReturn(Optional.of(sampleBook));
            given(reviewRepository.existsByUserIdAndBookId(sampleUser.getId(), sampleBook.getId())).willReturn(true);

            assertThatThrownBy(() -> reviewService.submitReview(sampleUser.getId(), sampleBook.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Review already exists");
        }
    }

    @Nested
    @DisplayName("Get Book Reviews")
    class GetReviewsTests {

        @Test
        @DisplayName("Should return approved reviews page with rating distribution")
        void shouldGetBookReviews() {
            Page<ReviewEntity> page = new PageImpl<>(List.of(sampleReview));
            given(bookRepository.findById(sampleBook.getId())).willReturn(Optional.of(sampleBook));
            given(reviewRepository.findByBookIdAndModerationStatusOrderByCreatedAtDesc(eq(sampleBook.getId()), eq("APPROVED"), any(Pageable.class)))
                .willReturn(page);
            given(reviewRepository.getRatingDistributionByBookId(sampleBook.getId())).willReturn(Collections.emptyList());

            BookReviewsResponse response = reviewService.getBookReviews(sampleBook.getId(), null, PageRequest.of(0, 10));

            assertThat(response).isNotNull();
            assertThat(response.getTotalReviews()).isEqualTo(1);
            assertThat(response.getRatingDistribution()).containsKey("5");
        }
    }
}
