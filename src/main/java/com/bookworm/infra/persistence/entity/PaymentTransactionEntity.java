package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_transactions")
@SQLRestriction("is_deleted = false")
public class PaymentTransactionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET

    @Column(name = "payment_gateway", nullable = false, length = 50)
    private String paymentGateway; // RAZORPAY, STRIPE, PAYTM

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "INITIATED"; // INITIATED, AUTHORIZED, CAPTURED, FAILED

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "upi_vpa", length = 100)
    private String upiVpa;

    @Column(name = "gateway_response_code", length = 100)
    private String gatewayResponseCode;

    @Column(name = "gateway_response_raw", columnDefinition = "jsonb")
    private String gatewayResponseRaw;

    @Column(name = "paid_at")
    private Instant paidAt;

    @OneToMany(mappedBy = "paymentTransaction", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentRefundEntity> refunds = new ArrayList<>();
}
