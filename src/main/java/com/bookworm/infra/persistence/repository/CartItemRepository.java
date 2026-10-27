package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID>, JpaSpecificationExecutor<CartItemEntity> {

    Optional<CartItemEntity> findByCartIdAndVariantId(UUID cartId, UUID variantId);

    void deleteByCartId(UUID cartId);
}
