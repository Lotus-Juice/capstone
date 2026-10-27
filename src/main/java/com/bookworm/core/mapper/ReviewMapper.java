package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.ReviewDTOs;
import com.bookworm.infra.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "reviewId", source = "id")
    @Mapping(target = "userName", expression = "java(entity.getUser() != null ? entity.getUser().getFirstName() + \" \" + entity.getUser().getLastName() : \"Anonymous\")")
    ReviewDTOs.ReviewDTO toDTO(ReviewEntity entity);

    List<ReviewDTOs.ReviewDTO> toDTOList(List<ReviewEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "verifiedPurchase", constant = "true")
    @Mapping(target = "moderationStatus", constant = "APPROVED")
    ReviewEntity toEntity(ReviewDTOs.SubmitReviewRequest request);
}
