package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.WishlistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, UUID>, JpaSpecificationExecutor<WishlistEntity> {

    @Query("""
        SELECT w FROM WishlistEntity w
        JOIN FETCH w.book b
        WHERE w.user.id = :userId
        ORDER BY w.createdAt DESC
    """)
    Page<WishlistEntity> findByUserIdWithBook(@Param("userId") UUID userId, Pageable pageable);

    Optional<WishlistEntity> findByUserIdAndBookId(UUID userId, UUID bookId);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    void deleteByUserIdAndBookId(UUID userId, UUID bookId);

    long countByUserId(UUID userId);
}
