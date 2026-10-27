package com.bookworm.integration;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.api.v1.dto.AuthDTOs.AuthResponse;
import com.bookworm.api.v1.dto.AuthDTOs.RegisterRequest;
import com.bookworm.api.v1.dto.ReviewDTOs.BookReviewsResponse;
import com.bookworm.api.v1.dto.ReviewDTOs.ReviewDTO;
import com.bookworm.api.v1.dto.ReviewDTOs.SubmitReviewRequest;
import com.bookworm.api.v1.dto.WishlistDTOs.WishlistItemDTO;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Wishlist & Review Full API Integration Tests (PostgreSQL Testcontainers)")
class WishlistAndReviewApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    private String customerJwt;
    private UUID testBookId;

    @BeforeEach
    void setUpUserAndBook() {
        // Register customer
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("wishlist.user." + UUID.randomUUID() + "@example.com");
        registerReq.setPassword("P@ssword123!");
        registerReq.setFirstName("Wishlist");
        registerReq.setLastName("User");

        ResponseEntity<AuthResponse> authRes = restTemplate.postForEntity(
            "/api/v1/auth/register",
            registerReq,
            AuthResponse.class
        );
        customerJwt = authRes.getBody().getAccessToken();

        // Create book for testing
        BookEntity book = bookRepository.save(BookEntity.builder()
            .title("Domain-Driven Design Reference")
            .slug("ddd-reference-" + UUID.randomUUID())
            .synopsis("Definitions and pattern summaries for DDD.")
            .coverImageUrl("https://images.example.com/ddd-ref.jpg")
            .averageRating(new BigDecimal("4.00"))
            .ratingsCount(1)
            .build());
        testBookId = book.getId();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(customerJwt);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Wishlist Flow: Add Book -> List Wishlist -> Remove Book")
    void executeWishlistLifecycle() {
        HttpHeaders headers = getAuthHeaders();

        // 1. Add book to wishlist
        ResponseEntity<WishlistItemDTO> addRes = restTemplate.postForEntity(
            "/api/v1/wishlist/" + testBookId,
            new HttpEntity<>(null, headers),
            WishlistItemDTO.class
        );
        assertThat(addRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(addRes.getBody()).isNotNull();

        // 2. List wishlist items
        ResponseEntity<List<WishlistItemDTO>> listRes = restTemplate.exchange(
            "/api/v1/wishlist",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<>() {}
        );
        assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listRes.getBody()).isNotEmpty();
        assertThat(listRes.getBody()).anyMatch(w -> w.getBookId().equals(testBookId));

        // 3. Remove book from wishlist
        ResponseEntity<Void> deleteRes = restTemplate.exchange(
            "/api/v1/wishlist/" + testBookId,
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            Void.class
        );
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Review Flow: Submit Review -> Recalculate Rating -> Fetch Book Reviews")
    void executeReviewLifecycle() {
        HttpHeaders headers = getAuthHeaders();

        // 1. Submit 5-star review
        SubmitReviewRequest reviewReq = new SubmitReviewRequest();
        reviewReq.setRating(5);
        reviewReq.setReviewText("Outstanding guide for domain modeling!");

        ResponseEntity<ReviewDTO> submitRes = restTemplate.postForEntity(
            "/api/v1/books/" + testBookId + "/reviews",
            new HttpEntity<>(reviewReq, headers),
            ReviewDTO.class
        );
        assertThat(submitRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(submitRes.getBody()).isNotNull();
        assertThat(submitRes.getBody().getRating()).isEqualTo(5);

        // 2. Fetch book reviews
        ResponseEntity<BookReviewsResponse> reviewsRes = restTemplate.getForEntity(
            "/api/v1/books/" + testBookId + "/reviews",
            BookReviewsResponse.class
        );
        assertThat(reviewsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reviewsRes.getBody()).isNotNull();
        assertThat(reviewsRes.getBody().getTotalReviews()).isEqualTo(2);
        // Initial 4.0 (1 rating) + 5.0 (1 rating) = 9.0 / 2 = 4.50
        assertThat(reviewsRes.getBody().getAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
    }
}
