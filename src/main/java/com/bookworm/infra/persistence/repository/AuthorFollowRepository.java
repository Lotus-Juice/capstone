package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.AuthorFollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorFollowRepository extends JpaRepository<AuthorFollowEntity, UUID>, JpaSpecificationExecutor<AuthorFollowEntity> {

    Optional<AuthorFollowEntity> findByUserIdAndAuthorId(UUID userId, UUID authorId);

    boolean existsByUserIdAndAuthorId(UUID userId, UUID authorId);

    void deleteByUserIdAndAuthorId(UUID userId, UUID authorId);
}
