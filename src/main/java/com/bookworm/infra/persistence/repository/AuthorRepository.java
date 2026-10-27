package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID>, JpaSpecificationExecutor<AuthorEntity> {

    Optional<AuthorEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<AuthorEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        SELECT a FROM AuthorEntity a
        JOIN a.followers f
        WHERE f.user.id = :userId
        ORDER BY f.createdAt DESC
    """)
    List<AuthorEntity> findFollowedAuthorsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(f) FROM AuthorFollowEntity f WHERE f.author.id = :authorId")
    long countFollowersByAuthorId(@Param("authorId") UUID authorId);
}
