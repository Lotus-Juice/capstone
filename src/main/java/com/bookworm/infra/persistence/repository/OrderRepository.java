package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Page<OrderEntity> findByUserIdOrderByPlacedAtDesc(UUID userId, Pageable pageable);

    @Query("""
        SELECT o FROM OrderEntity o
        LEFT JOIN FETCH o.lineItems li
        LEFT JOIN FETCH li.variant v
        LEFT JOIN FETCH v.book
        LEFT JOIN FETCH o.shippingAddress
        LEFT JOIN FETCH o.paymentTransactions
        WHERE o.id = :id
    """)
    Optional<OrderEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT o FROM OrderEntity o
        LEFT JOIN FETCH o.lineItems li
        LEFT JOIN FETCH li.variant v
        LEFT JOIN FETCH v.book
        LEFT JOIN FETCH o.shippingAddress
        WHERE o.id = :id AND o.user.id = :userId
    """)
    Optional<OrderEntity> findByIdAndUserIdWithDetails(@Param("id") UUID id, @Param("userId") UUID userId);

    Page<OrderEntity> findByStatus(String status, Pageable pageable);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.status = :status WHERE o.id = :orderId")
    void updateOrderStatus(@Param("orderId") UUID orderId, @Param("status") String status);
}
