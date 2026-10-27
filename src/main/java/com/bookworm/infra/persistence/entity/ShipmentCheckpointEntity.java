package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipment_checkpoints")
@SQLRestriction("is_deleted = false")
public class ShipmentCheckpointEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;

    @Column(name = "checkpoint_status", nullable = false, length = 50)
    private String checkpointStatus;

    @Column(name = "location_city", nullable = false, length = 100)
    private String locationCity;

    @Column(name = "activity_description", nullable = false, length = 255)
    private String activityDescription;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;
}
