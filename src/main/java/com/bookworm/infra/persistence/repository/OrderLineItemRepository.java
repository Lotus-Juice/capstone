package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.OrderLineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderLineItemRepository extends JpaRepository<OrderLineItemEntity, UUID>, JpaSpecificationExecutor<OrderLineItemEntity> {

    List<OrderLineItemEntity> findByOrderId(UUID orderId);
}
