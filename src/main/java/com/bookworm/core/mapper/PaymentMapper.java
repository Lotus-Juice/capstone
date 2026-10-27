package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.PaymentDTOs;
import com.bookworm.infra.persistence.entity.PaymentTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "payableAmount", source = "amount")
    PaymentDTOs.InitiatePaymentResponse toInitiateResponse(PaymentTransactionEntity entity);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderStatus", source = "order.status")
    @Mapping(target = "message", constant = "Your purchase of the following reads is successful.")
    PaymentDTOs.VerifyPaymentResponse toVerifyResponse(PaymentTransactionEntity entity);
}
