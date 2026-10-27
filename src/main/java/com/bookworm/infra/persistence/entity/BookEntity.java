package com.bookworm.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "books")
@SQLRestriction("is_deleted = false")
public class BookEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private PublisherEntity publisher;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Column(name = "slug", nullable = false, length = 350)
    private String slug;

    @Column(name = "synopsis", nullable = false, columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "language", nullable = false, length = 50)
    @Builder.Default
    private String language = "English";

    @Column(name = "cover_image_url", nullable = false, length = 500)
    private String coverImageUrl;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "copies_sold", nullable = false)
    @Builder.Default
    private Integer copiesSold = 0;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "ratings_count", nullable = false)
    @Builder.Default
    private Integer ratingsCount = 0;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private boolean isFeatured = false;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookAuthorEntity> bookAuthors = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookCategoryEntity> bookCategories = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookVariantEntity> variants = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewEntity> reviews = new ArrayList<>();
}
