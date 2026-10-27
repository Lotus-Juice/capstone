package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Page<UserEntity> findByRole(String role, Pageable pageable);

    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") UUID userId, @Param("loginTime") Instant loginTime);

    @Modifying
    @Query("UPDATE UserEntity u SET u.isDeleted = true WHERE u.id = :userId")
    void softDeleteById(@Param("userId") UUID userId);
}
