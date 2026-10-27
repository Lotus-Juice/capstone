package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, UUID>, JpaSpecificationExecutor<ShipmentEntity> {

    @Query("""
        SELECT s FROM ShipmentEntity s
        LEFT JOIN FETCH s.checkpoints cp
        WHERE s.trackingNumber = :trackingNumber
        ORDER BY cp.scannedAt ASC
    """)
    Optional<ShipmentEntity> findByTrackingNumberWithCheckpoints(@Param("trackingNumber") String trackingNumber);

    Optional<ShipmentEntity> findByCarrierNameAndTrackingNumber(String carrierName, String trackingNumber);

    List<ShipmentEntity> findByOrderId(UUID orderId);
}
