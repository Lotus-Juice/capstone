package com.bookworm.infra.persistence.repository;

import com.bookworm.infra.persistence.entity.BookVariantEntity;
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
public interface BookVariantRepository extends JpaRepository<BookVariantEntity, UUID>, JpaSpecificationExecutor<BookVariantEntity> {

    Optional<BookVariantEntity> findByIsbn(String isbn);

    Optional<BookVariantEntity> findBySku(String sku);

    Optional<BookVariantEntity> findByBookIdAndFormatType(UUID bookId, String formatType);

    List<BookVariantEntity> findByBookIdAndIsActiveTrue(UUID bookId);

    @Modifying
    @Query("""
        UPDATE BookVariantEntity v
        SET v.stockQuantity = v.stockQuantity - :quantity
        WHERE v.id = :variantId AND v.stockQuantity >= :quantity
    """)
    int deductStockIfAvailable(@Param("variantId") UUID variantId, @Param("quantity") int quantity);

    @Modifying
    @Query("""
        UPDATE BookVariantEntity v
        SET v.stockQuantity = v.stockQuantity + :quantity
        WHERE v.id = :variantId
    """)
    void restoreStock(@Param("variantId") UUID variantId, @Param("quantity") int quantity);
}
