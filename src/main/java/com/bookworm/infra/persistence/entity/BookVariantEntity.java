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
@Table(name = "book_variants")
@SQLRestriction("is_deleted = false")
public class BookVariantEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "format_type", nullable = false, length = 30)
    private String formatType; // PAPERBACK, HARDCOVER, EBOOK, AUDIO

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "list_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
