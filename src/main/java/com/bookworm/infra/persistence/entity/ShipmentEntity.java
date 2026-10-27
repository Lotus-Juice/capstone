package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipments")
@SQLRestriction("is_deleted = false")
public class ShipmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "carrier_name", nullable = false, length = 100)
    private String carrierName;

    @Column(name = "tracking_number", nullable = false, length = 100)
    private String trackingNumber;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "MANIFESTED"; // MANIFESTED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED_ATTEMPT, RETURNED_TO_ORIGIN

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShipmentCheckpointEntity> checkpoints = new ArrayList<>();
}
