package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.BookDTOs;
import com.bookworm.infra.persistence.entity.BookCategoryEntity;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuthorMapper.class, PublisherMapper.class})
public interface BookMapper {

    @Mapping(target = "author", expression = "java(extractPrimaryAuthor(entity))")
    @Mapping(target = "publisher", source = "publisher.name")
    @Mapping(target = "primaryCategory", expression = "java(extractPrimaryCategory(entity))")
    @Mapping(target = "availableFormats", expression = "java(extractAvailableFormats(entity))")
    @Mapping(target = "startingPrice", expression = "java(calculateStartingPrice(entity))")
    @Mapping(target = "currency", constant = "INR")
    @Mapping(target = "deliveryEstimate", constant = "Delivery by Mon, 21 Jul")
    BookDTOs.BookSummaryDTO toSummaryDTO(BookEntity entity);

    List<BookDTOs.BookSummaryDTO> toSummaryDTOList(List<BookEntity> entities);

    @Mapping(target = "author", expression = "java(extractAuthorDTO(entity))")
    @Mapping(target = "categories", expression = "java(toBookCategoryDTOList(entity.getBookCategories()))")
    @Mapping(target = "variants", source = "variants")
    @Mapping(target = "isWishlisted", constant = "false")
    BookDTOs.BookDetailResponse toDetailResponse(BookEntity entity);

    @Mapping(target = "variantId", source = "id")
    @Mapping(target = "inStock", expression = "java(variant.getStockQuantity() != null && variant.getStockQuantity() > 0)")
    BookDTOs.BookVariantDTO toVariantDTO(BookVariantEntity variant);

    List<BookDTOs.BookVariantDTO> toVariantDTOList(List<BookVariantEntity> variants);

    default BookDTOs.AuthorSummaryDTO extractPrimaryAuthor(BookEntity entity) {
        if (entity.getBookAuthors() == null || entity.getBookAuthors().isEmpty()) {
            return null;
        }
        var author = entity.getBookAuthors().get(0).getAuthor();
        return author == null ? null : BookDTOs.AuthorSummaryDTO.builder()
            .id(author.getId())
            .name(author.getName())
            .slug(author.getSlug())
            .build();
    }

    default com.bookworm.api.v1.dto.AuthorDTOs.AuthorDTO extractAuthorDTO(BookEntity entity) {
        if (entity.getBookAuthors() == null || entity.getBookAuthors().isEmpty()) {
            return null;
        }
        var author = entity.getBookAuthors().get(0).getAuthor();
        if (author == null) return null;
        return com.bookworm.api.v1.dto.AuthorDTOs.AuthorDTO.builder()
            .id(author.getId())
            .name(author.getName())
            .slug(author.getSlug())
            .biography(author.getBiography())
            .profileImageUrl(author.getProfileImageUrl())
            .build();
    }

    default String extractPrimaryCategory(BookEntity entity) {
        if (entity.getBookCategories() == null || entity.getBookCategories().isEmpty()) {
            return null;
        }
        return entity.getBookCategories().stream()
            .filter(BookCategoryEntity::isPrimary)
            .findFirst()
            .map(bc -> bc.getCategory().getName())
            .orElseGet(() -> entity.getBookCategories().get(0).getCategory().getName());
    }

    default List<String> extractAvailableFormats(BookEntity entity) {
        if (entity.getVariants() == null || entity.getVariants().isEmpty()) {
            return Collections.emptyList();
        }
        return entity.getVariants().stream()
            .filter(BookVariantEntity::isActive)
            .map(BookVariantEntity::getFormatType)
            .distinct()
            .toList();
    }

    default BigDecimal calculateStartingPrice(BookEntity entity) {
        if (entity.getVariants() == null || entity.getVariants().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return entity.getVariants().stream()
            .filter(BookVariantEntity::isActive)
            .map(BookVariantEntity::getSellingPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    default List<BookDTOs.BookCategoryDTO> toBookCategoryDTOList(List<BookCategoryEntity> categories) {
        if (categories == null) return Collections.emptyList();
        return categories.stream()
            .map(bc -> BookDTOs.BookCategoryDTO.builder()
                .id(bc.getCategory().getId())
                .name(bc.getCategory().getName())
                .slug(bc.getCategory().getSlug())
                .isPrimary(bc.isPrimary())
                .build())
            .toList();
    }
}
