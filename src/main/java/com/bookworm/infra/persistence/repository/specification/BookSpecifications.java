package com.bookworm.infra.persistence.repository.specification;

import com.bookworm.infra.persistence.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Reusable JPA Specifications for dynamic multi-faceted book catalog search and filtering.
 */
public final class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<BookEntity> hasCategorySlug(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isBlank()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<BookEntity, BookCategoryEntity> bookCat = root.join("bookCategories", JoinType.INNER);
            Join<BookCategoryEntity, CategoryEntity> cat = bookCat.join("category", JoinType.INNER);
            return cb.equal(cb.lower(cat.get("slug")), categorySlug.toLowerCase());
        };
    }

    public static Specification<BookEntity> hasLanguage(String language) {
        return (root, query, cb) -> {
            if (language == null || language.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("language")), language.toLowerCase());
        };
    }

    public static Specification<BookEntity> hasFormat(String formatType) {
        return (root, query, cb) -> {
            if (formatType == null || formatType.isBlank()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<BookEntity, BookVariantEntity> variants = root.join("variants", JoinType.INNER);
            return cb.and(
                cb.equal(cb.upper(variants.get("formatType")), formatType.toUpperCase()),
                cb.isTrue(variants.get("isActive"))
            );
        };
    }

    public static Specification<BookEntity> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<BookEntity, BookVariantEntity> variants = root.join("variants", JoinType.INNER);
            if (minPrice != null && maxPrice != null) {
                return cb.between(variants.get("sellingPrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return cb.greaterThanOrEqualTo(variants.get("sellingPrice"), minPrice);
            } else {
                return cb.lessThanOrEqualTo(variants.get("sellingPrice"), maxPrice);
            }
        };
    }

    public static Specification<BookEntity> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            query.distinct(true);
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            Join<BookEntity, BookAuthorEntity> bookAuthors = root.join("bookAuthors", JoinType.LEFT);
            Join<BookAuthorEntity, AuthorEntity> author = bookAuthors.join("author", JoinType.LEFT);

            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("subtitle")), pattern),
                cb.like(cb.lower(root.get("synopsis")), pattern),
                cb.like(cb.lower(author.get("name")), pattern)
            );
        };
    }

    public static Specification<BookEntity> ratingGreaterThanOrEqualTo(BigDecimal minRating) {
        return (root, query, cb) -> {
            if (minRating == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
        };
    }
}
