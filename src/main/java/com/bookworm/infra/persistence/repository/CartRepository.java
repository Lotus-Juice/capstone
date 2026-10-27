package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, UUID>, JpaSpecificationExecutor<CartEntity> {

    @Query("""
        SELECT c FROM CartEntity c
        LEFT JOIN FETCH c.items i
        LEFT JOIN FETCH i.variant v
        LEFT JOIN FETCH v.book b
        WHERE c.user.id = :userId AND c.status = 'ACTIVE'
    """)
    Optional<CartEntity> findActiveCartByUserIdWithItems(@Param("userId") UUID userId);

    @Query("""
        SELECT c FROM CartEntity c
        LEFT JOIN FETCH c.items i
        LEFT JOIN FETCH i.variant v
        LEFT JOIN FETCH v.book b
        WHERE c.guestSessionToken = :guestToken AND c.status = 'ACTIVE'
    """)
    Optional<CartEntity> findActiveCartByGuestTokenWithItems(@Param("guestToken") String guestToken);

    Optional<CartEntity> findByUserIdAndStatus(UUID userId, String status);

    Optional<CartEntity> findByGuestSessionTokenAndStatus(String guestSessionToken, String status);

    void deleteByStatusAndExpiresAtBefore(String status, Instant expirationThreshold);
}
