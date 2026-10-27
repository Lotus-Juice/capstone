package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID>, JpaSpecificationExecutor<PaymentTransactionEntity> {

    Optional<PaymentTransactionEntity> findByGatewayTransactionId(String gatewayTransactionId);

    List<PaymentTransactionEntity> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    Optional<PaymentTransactionEntity> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(UUID orderId, String status);

    @Modifying
    @Query("""
        UPDATE PaymentTransactionEntity p
        SET p.status = :status, p.gatewayTransactionId = :gatewayTxnId, p.paidAt = :paidAt
        WHERE p.id = :paymentId
    """)
    void updatePaymentStatus(
        @Param("paymentId") UUID paymentId,
        @Param("status") String status,
        @Param("gatewayTxnId") String gatewayTxnId,
        @Param("paidAt") Instant paidAt
    );
}
