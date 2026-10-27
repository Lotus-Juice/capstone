package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.OrderDTOs;
import com.bookworm.infra.persistence.entity.OrderEntity;
import com.bookworm.infra.persistence.entity.OrderLineItemEntity;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "lineItems", source = "lineItems")
    @Mapping(target = "payment", expression = "java(extractPaymentSummary(entity))")
    OrderDTOs.OrderDetailResponse toDetailResponse(OrderEntity entity);

    @Mapping(target = "bookTitle", source = "bookTitleSnapshot")
    @Mapping(target = "author", source = "authorNameSnapshot")
    @Mapping(target = "format", source = "formatTypeSnapshot")
    OrderDTOs.OrderLineItemDTO toLineItemDTO(OrderLineItemEntity lineItem);

    List<OrderDTOs.OrderLineItemDTO> toLineItemDTOList(List<OrderLineItemEntity> lineItems);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "itemsPreview", expression = "java(extractItemsPreview(entity))")
    @Mapping(target = "canReorder", constant = "true")
    OrderDTOs.OrderHistorySummaryDTO toHistorySummaryDTO(OrderEntity entity);

    List<OrderDTOs.OrderHistorySummaryDTO> toHistorySummaryDTOList(List<OrderEntity> entities);

    default OrderDTOs.PaymentSummaryDTO extractPaymentSummary(OrderEntity entity) {
        if (entity.getPaymentTransactions() == null || entity.getPaymentTransactions().isEmpty()) {
            return null;
        }
        var latestPayment = entity.getPaymentTransactions().get(0);
        return OrderDTOs.PaymentSummaryDTO.builder()
            .paymentMethod(latestPayment.getPaymentMethod())
            .maskedCard(latestPayment.getCardLastFour() != null ? "XXXX-XXXX-XXXX-" + latestPayment.getCardLastFour() : null)
            .status(latestPayment.getStatus())
            .build();
    }

    default List<OrderDTOs.ItemPreviewDTO> extractItemsPreview(OrderEntity entity) {
        if (entity.getLineItems() == null || entity.getLineItems().isEmpty()) {
            return Collections.emptyList();
        }
        return entity.getLineItems().stream()
            .map(li -> OrderDTOs.ItemPreviewDTO.builder()
                .title(li.getBookTitleSnapshot())
                .coverImageUrl(li.getVariant() != null && li.getVariant().getBook() != null
                    ? li.getVariant().getBook().getCoverImageUrl() : null)
                .build())
            .toList();
    }
}
