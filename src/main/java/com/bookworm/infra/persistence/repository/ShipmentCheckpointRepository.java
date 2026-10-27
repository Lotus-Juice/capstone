package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.ShipmentCheckpointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentCheckpointRepository extends JpaRepository<ShipmentCheckpointEntity, UUID>, JpaSpecificationExecutor<ShipmentCheckpointEntity> {

    List<ShipmentCheckpointEntity> findByShipmentIdOrderByScannedAtAsc(UUID shipmentId);
}
