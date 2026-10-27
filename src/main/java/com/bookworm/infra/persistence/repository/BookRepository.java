package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID>, JpaSpecificationExecutor<BookEntity> {

    Optional<BookEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
        SELECT DISTINCT b FROM BookEntity b
        LEFT JOIN FETCH b.publisher
        LEFT JOIN FETCH b.bookAuthors ba
        LEFT JOIN FETCH ba.author
        LEFT JOIN FETCH b.variants
        WHERE b.id = :id
    """)
    Optional<BookEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT b FROM BookEntity b
        LEFT JOIN FETCH b.publisher
        LEFT JOIN FETCH b.bookAuthors ba
        LEFT JOIN FETCH ba.author
        LEFT JOIN FETCH b.variants
        WHERE b.slug = :slug
    """)
    Optional<BookEntity> findBySlugWithDetails(@Param("slug") String slug);

    // Bestsellers Carousel (sorted by copies_sold)
    List<BookEntity> findTop10ByOrderByCopiesSoldDesc();

    // New Launches Carousel (sorted by published_date)
    List<BookEntity> findTop10ByOrderByPublishedDateDesc();

    // Featured Curated Books
    List<BookEntity> findByIsFeaturedTrueOrderByAverageRatingDesc(Pageable pageable);

    // Related reads by category exclusion
    @Query("""
        SELECT DISTINCT b FROM BookEntity b
        JOIN b.bookCategories bc
        WHERE bc.category.id IN (
            SELECT bc2.category.id FROM BookCategoryEntity bc2 WHERE bc2.book.id = :bookId
        )
        AND b.id <> :bookId
        ORDER BY b.averageRating DESC, b.copiesSold DESC
    """)
    List<BookEntity> findRelatedBooksByCategory(@Param("bookId") UUID bookId, Pageable pageable);

    // Full-Text / Keyword search across title, subtitle, synopsis, author name
    @Query("""
        SELECT DISTINCT b FROM BookEntity b
        LEFT JOIN b.bookAuthors ba
        LEFT JOIN ba.author a
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.synopsis) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<BookEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE BookEntity b SET b.copiesSold = b.copiesSold + :quantity WHERE b.id = :bookId")
    void incrementCopiesSold(@Param("bookId") UUID bookId, @Param("quantity") int quantity);

    @Modifying
    @Query("""
        UPDATE BookEntity b
        SET b.averageRating = :avgRating, b.ratingsCount = :ratingsCount
        WHERE b.id = :bookId
    """)
    void updateRatingsAggregate(
        @Param("bookId") UUID bookId,
        @Param("avgRating") BigDecimal avgRating,
        @Param("ratingsCount") int ratingsCount
    );
}
