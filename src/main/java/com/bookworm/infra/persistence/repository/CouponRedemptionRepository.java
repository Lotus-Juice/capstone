package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.CouponRedemptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemptionEntity, UUID>, JpaSpecificationExecutor<CouponRedemptionEntity> {

    long countByCouponId(UUID couponId);

    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    @Query("SELECT r FROM CouponRedemptionEntity r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<CouponRedemptionEntity> findByUserId(@Param("userId") UUID userId);
}
