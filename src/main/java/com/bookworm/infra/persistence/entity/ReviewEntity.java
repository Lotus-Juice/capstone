package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews")
@SQLRestriction("is_deleted = false")
public class ReviewEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1 to 5

    @Column(name = "review_text", length = 500)
    private String reviewText;

    @Column(name = "is_verified_purchase", nullable = false)
    @Builder.Default
    private boolean isVerifiedPurchase = false;

    @Column(name = "moderation_status", nullable = false, length = 30)
    @Builder.Default
    private String moderationStatus = "APPROVED"; // PENDING, APPROVED, REJECTED
}
