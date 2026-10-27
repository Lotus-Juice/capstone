package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.PaymentRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefundEntity, UUID>, JpaSpecificationExecutor<PaymentRefundEntity> {

    List<PaymentRefundEntity> findByPaymentTransactionId(UUID paymentTransactionId);

    Optional<PaymentRefundEntity> findByGatewayRefundId(String gatewayRefundId);
}
