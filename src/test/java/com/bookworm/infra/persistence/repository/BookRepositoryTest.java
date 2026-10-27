package com.bookworm.infra.persistence.repository;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.infra.persistence.entity.BookEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository JPA Integration Tests (PostgreSQL Testcontainers)")
@Transactional
class BookRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Should find book with details by slug")
    void shouldFindBookBySlugWithDetails() {
        String slug = "test-book-" + UUID.randomUUID();
        BookEntity book = BookEntity.builder()
            .title("Test Software Architecture")
            .slug(slug)
            .synopsis("Deep dive into modern architectural paradigms.")
            .coverImageUrl("https://images.example.com/test-arch.jpg")
            .copiesSold(10)
            .averageRating(new BigDecimal("4.50"))
            .ratingsCount(2)
            .build();

        bookRepository.save(book);

        Optional<BookEntity> found = bookRepository.findBySlugWithDetails(slug);
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Software Architecture");
    }

    @Test
    @DisplayName("Should increment copies sold atomically")
    void shouldIncrementCopiesSold() {
        String slug = "sales-book-" + UUID.randomUUID();
        BookEntity book = BookEntity.builder()
            .title("Bestselling Novel")
            .slug(slug)
            .synopsis("Top-selling mystery thriller.")
            .coverImageUrl("https://images.example.com/thriller.jpg")
            .copiesSold(100)
            .build();

        BookEntity saved = bookRepository.save(book);

        bookRepository.incrementCopiesSold(saved.getId(), 5);

        BookEntity reloaded = bookRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getCopiesSold()).isEqualTo(105);
    }

    @Test
    @DisplayName("Should update ratings aggregate atomically")
    void shouldUpdateRatingsAggregate() {
        String slug = "rating-book-" + UUID.randomUUID();
        BookEntity book = BookEntity.builder()
            .title("Book for Rating")
            .slug(slug)
            .synopsis("Reviewable book.")
            .coverImageUrl("https://images.example.com/rating.jpg")
            .averageRating(new BigDecimal("3.00"))
            .ratingsCount(1)
            .build();

        BookEntity saved = bookRepository.save(book);

        bookRepository.updateRatingsAggregate(saved.getId(), new BigDecimal("4.50"), 2);

        BookEntity reloaded = bookRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(reloaded.getRatingsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should search books by title keyword")
    void shouldSearchByKeyword() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        BookEntity book = BookEntity.builder()
            .title("Microservices Patterns " + randomSuffix)
            .slug("microservices-patterns-" + randomSuffix)
            .synopsis("Comprehensive patterns for distributed applications.")
            .coverImageUrl("https://images.example.com/microservices.jpg")
            .build();

        bookRepository.save(book);

        Page<BookEntity> matches = bookRepository.searchByKeyword("Microservices", PageRequest.of(0, 10));
        assertThat(matches.getContent()).isNotEmpty();
        assertThat(matches.getContent()).anyMatch(b -> b.getTitle().contains("Microservices Patterns"));
    }
}
