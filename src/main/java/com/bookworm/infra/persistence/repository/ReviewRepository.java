package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, JpaSpecificationExecutor<ReviewEntity> {

    Page<ReviewEntity> findByBookIdAndModerationStatusOrderByCreatedAtDesc(
        UUID bookId,
        String moderationStatus,
        Pageable pageable
    );

    Page<ReviewEntity> findByBookIdAndModerationStatusAndRatingOrderByCreatedAtDesc(
        UUID bookId,
        String moderationStatus,
        Integer rating,
        Pageable pageable
    );

    Optional<ReviewEntity> findByUserIdAndBookId(UUID userId, UUID bookId);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    long countByBookIdAndModerationStatus(UUID bookId, String moderationStatus);

    @Query("""
        SELECT r.rating AS rating, COUNT(r) AS count
        FROM ReviewEntity r
        WHERE r.book.id = :bookId AND r.moderationStatus = 'APPROVED'
        GROUP BY r.rating
    """)
    List<RatingCountProjection> getRatingDistributionByBookId(@Param("bookId") UUID bookId);

    interface RatingCountProjection {
        Integer getRating();
        Long getCount();
    }
}
