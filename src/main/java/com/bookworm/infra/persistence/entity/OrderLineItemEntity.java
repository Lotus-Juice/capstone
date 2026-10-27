package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_line_items")
@SQLRestriction("is_deleted = false")
public class OrderLineItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private BookVariantEntity variant;

    @Column(name = "book_title_snapshot", nullable = false, length = 300)
    private String bookTitleSnapshot;

    @Column(name = "author_name_snapshot", nullable = false, length = 200)
    private String authorNameSnapshot;

    @Column(name = "format_type_snapshot", nullable = false, length = 30)
    private String formatTypeSnapshot;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;
}
