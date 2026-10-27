package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.CartDTOs;
import com.bookworm.infra.persistence.entity.CartEntity;
import com.bookworm.infra.persistence.entity.CartItemEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "itemsCount", expression = "java(entity.getItems() != null ? entity.getItems().size() : 0)")
    @Mapping(target = "summary", expression = "java(calculateSummary(entity))")
    CartDTOs.CartDTO toDTO(CartEntity entity);

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "bookId", source = "variant.book.id")
    @Mapping(target = "title", source = "variant.book.title")
    @Mapping(target = "author", expression = "java(extractAuthorName(item))")
    @Mapping(target = "format", source = "variant.formatType")
    @Mapping(target = "coverImageUrl", source = "variant.book.coverImageUrl")
    @Mapping(target = "unitPrice", source = "unitPriceAtAddition")
    @Mapping(target = "lineTotal", expression = "java(item.getUnitPriceAtAddition().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    @Mapping(target = "inStock", expression = "java(item.getVariant().getStockQuantity() >= item.getQuantity())")
    @Mapping(target = "deliveryEstimate", constant = "Delivery by Mon, 21 Jul")
    CartDTOs.CartItemDTO toItemDTO(CartItemEntity item);

    List<CartDTOs.CartItemDTO> toItemDTOList(List<CartItemEntity> items);

    default String extractAuthorName(CartItemEntity item) {
        if (item.getVariant() == null || item.getVariant().getBook() == null) return null;
        var bookAuthors = item.getVariant().getBook().getBookAuthors();
        if (bookAuthors == null || bookAuthors.isEmpty()) return null;
        var author = bookAuthors.get(0).getAuthor();
        return author == null ? null : author.getName();
    }

    default CartDTOs.CartSummaryDTO calculateSummary(CartEntity entity) {
        if (entity.getItems() == null || entity.getItems().isEmpty()) {
            return CartDTOs.CartSummaryDTO.builder()
                .itemsTotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .deliveryCharges(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .currency("INR")
                .build();
        }

        BigDecimal itemsTotal = entity.getItems().stream()
            .map(i -> i.getUnitPriceAtAddition().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Standard Indian GST / Tax Calculation (approx ~12% on book subtotal)
        BigDecimal tax = itemsTotal.multiply(new BigDecimal("0.12")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal delivery = itemsTotal.compareTo(new BigDecimal("500.00")) >= 0 ? BigDecimal.ZERO : new BigDecimal("40.00");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal grandTotal = itemsTotal.add(tax).add(delivery).subtract(discount);

        return CartDTOs.CartSummaryDTO.builder()
            .itemsTotal(itemsTotal)
            .taxAmount(tax)
            .deliveryCharges(delivery)
            .discountAmount(discount)
            .grandTotal(grandTotal)
            .currency("INR")
            .build();
    }
}
