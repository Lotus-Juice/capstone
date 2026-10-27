package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CategoryDTOs;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.CategoryMapper;
import com.bookworm.infra.persistence.entity.CategoryEntity;
import com.bookworm.infra.persistence.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDTOs.CategoryNodeDTO> getCategoryTree() {
        log.info("Fetching complete category taxonomy tree");
        List<CategoryEntity> rootCategories = categoryRepository.findByParentCategoryIsNullOrderByDisplayOrderAsc();
        return categoryMapper.toNodeDTOList(rootCategories);
    }

    @Transactional(readOnly = true)
    public CategoryDTOs.CategoryDetailResponse getCategoryDetails(String categoryIdOrSlug) {
        log.info("Fetching category details for identifier: {}", categoryIdOrSlug);

        CategoryEntity category;
        try {
            UUID id = UUID.fromString(categoryIdOrSlug);
            category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        } catch (IllegalArgumentException e) {
            category = categoryRepository.findBySlug(categoryIdOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryIdOrSlug));
        }

        return categoryMapper.toDetailResponse(category);
    }
}
