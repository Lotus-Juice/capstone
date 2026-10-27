package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherEntity, UUID>, JpaSpecificationExecutor<PublisherEntity> {

    Optional<PublisherEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<PublisherEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
