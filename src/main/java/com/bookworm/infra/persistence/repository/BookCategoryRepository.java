package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.BookCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategoryEntity, UUID>, JpaSpecificationExecutor<BookCategoryEntity> {

    List<BookCategoryEntity> findByBookId(UUID bookId);

    List<BookCategoryEntity> findByCategoryId(UUID categoryId);

    void deleteByBookIdAndCategoryId(UUID bookId, UUID categoryId);
}
