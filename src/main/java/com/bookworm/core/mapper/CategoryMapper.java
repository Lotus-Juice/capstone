package com.bookworm.core.mapper;

import com.bookworm.api.v1.dto.CategoryDTOs;
import com.bookworm.infra.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "subcategories", source = "subcategories")
    CategoryDTOs.CategoryNodeDTO toNodeDTO(CategoryEntity entity);

    List<CategoryDTOs.CategoryNodeDTO> toNodeDTOList(List<CategoryEntity> entities);

    @Mapping(target = "totalBooks", expression = "java(entity.getBookCategories() != null ? entity.getBookCategories().size() : 0)")
    CategoryDTOs.CategoryDetailResponse toDetailResponse(CategoryEntity entity);
}
