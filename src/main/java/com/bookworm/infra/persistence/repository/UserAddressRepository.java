package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, UUID>, JpaSpecificationExecutor<UserAddressEntity> {

    List<UserAddressEntity> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    Optional<UserAddressEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserAddressEntity> findByUserIdAndIsDefaultTrue(UUID userId);

    long countByUserId(UUID userId);

    @Modifying
    @Query("UPDATE UserAddressEntity a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultAddressForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserAddressEntity a SET a.isDefault = true WHERE a.id = :addressId AND a.user.id = :userId")
    void setDefaultAddress(@Param("addressId") UUID addressId, @Param("userId") UUID userId);
}
