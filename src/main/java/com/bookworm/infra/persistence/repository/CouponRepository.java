package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, UUID>, JpaSpecificationExecutor<CouponEntity> {

    Optional<CouponEntity> findByCodeIgnoreCase(String code);

    @Query("""
        SELECT c FROM CouponEntity c
        WHERE UPPER(c.code) = UPPER(:code)
          AND c.isActive = true
          AND :now BETWEEN c.validFrom AND c.validUntil
    """)
    Optional<CouponEntity> findActiveCouponByCode(@Param("code") String code, @Param("now") Instant now);

    @Query("""
        SELECT c FROM CouponEntity c
        WHERE c.isActive = true
          AND :now BETWEEN c.validFrom AND c.validUntil
        ORDER BY c.validUntil ASC
    """)
    List<CouponEntity> findAllActiveOffers(@Param("now") Instant now);
}
