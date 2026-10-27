package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<CategoryEntity> findByParentCategoryIsNullOrderByDisplayOrderAsc();

    List<CategoryEntity> findByParentCategoryIdOrderByDisplayOrderAsc(UUID parentId);

    @Query("""
        SELECT COUNT(bc) FROM BookCategoryEntity bc
        WHERE bc.category.id = :categoryId
    """)
    long countBooksByCategoryId(@Param("categoryId") UUID categoryId);
}
