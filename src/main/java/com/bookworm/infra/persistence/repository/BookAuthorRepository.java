package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.BookAuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthorEntity, UUID>, JpaSpecificationExecutor<BookAuthorEntity> {

    List<BookAuthorEntity> findByBookIdOrderByAuthorOrderAsc(UUID bookId);

    List<BookAuthorEntity> findByAuthorId(UUID authorId);

    void deleteByBookIdAndAuthorId(UUID bookId, UUID authorId);
}
