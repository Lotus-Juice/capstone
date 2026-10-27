package com.bookworm.infra.persistence.repository;

import com.bookworm.BaseIntegrationTest;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookVariantRepository JPA Integration Tests (PostgreSQL Testcontainers)")
@Transactional
class BookVariantRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookVariantRepository bookVariantRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Atomic Stock Deduction: Should deduct stock if available and return updated row count 1")
    void shouldDeductStockWhenAvailable() {
        BookEntity book = bookRepository.save(BookEntity.builder()
            .title("Stock Test Book")
            .slug("stock-test-book-" + UUID.randomUUID())
            .synopsis("Inventory test synopsis.")
            .coverImageUrl("https://images.example.com/cover.jpg")
            .build());

        BookVariantEntity variant = bookVariantRepository.save(BookVariantEntity.builder()
            .book(book)
            .formatType(BookVariantEntity.FormatType.PAPERBACK)
            .isbn("ISBN-" + UUID.randomUUID())
            .sku("SKU-STOCK-" + UUID.randomUUID())
            .listPrice(new BigDecimal("500.00"))
            .sellingPrice(new BigDecimal("400.00"))
            .stockQuantity(10)
            .isActive(true)
            .build());

        int updatedRows = bookVariantRepository.deductStockIfAvailable(variant.getId(), 3);
        assertThat(updatedRows).isEqualTo(1);

        BookVariantEntity reloaded = bookVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Atomic Stock Deduction: Should fail with 0 rows updated when requested quantity exceeds available stock")
    void shouldFailStockDeductionWhenInsufficientStock() {
        BookEntity book = bookRepository.save(BookEntity.builder()
            .title("Low Stock Book")
            .slug("low-stock-" + UUID.randomUUID())
            .synopsis("Low stock synopsis.")
            .coverImageUrl("https://images.example.com/low-stock.jpg")
            .build());

        BookVariantEntity variant = bookVariantRepository.save(BookVariantEntity.builder()
            .book(book)
            .formatType(BookVariantEntity.FormatType.HARDCOVER)
            .isbn("ISBN-LOW-" + UUID.randomUUID())
            .sku("SKU-LOW-" + UUID.randomUUID())
            .listPrice(new BigDecimal("800.00"))
            .sellingPrice(new BigDecimal("700.00"))
            .stockQuantity(2)
            .isActive(true)
            .build());

        int updatedRows = bookVariantRepository.deductStockIfAvailable(variant.getId(), 5); // Requesting 5, only 2 available
        assertThat(updatedRows).isEqualTo(0);

        BookVariantEntity reloaded = bookVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(2); // Unmodified
    }

    @Test
    @DisplayName("Stock Restoration: Should restore stock on order cancellation")
    void shouldRestoreStockOnCancellation() {
        BookEntity book = bookRepository.save(BookEntity.builder()
            .title("Restore Stock Book")
            .slug("restore-stock-" + UUID.randomUUID())
            .synopsis("Restoration synopsis.")
            .coverImageUrl("https://images.example.com/restore.jpg")
            .build());

        BookVariantEntity variant = bookVariantRepository.save(BookVariantEntity.builder()
            .book(book)
            .formatType(BookVariantEntity.FormatType.PAPERBACK)
            .isbn("ISBN-RESTORE-" + UUID.randomUUID())
            .sku("SKU-RESTORE-" + UUID.randomUUID())
            .listPrice(new BigDecimal("600.00"))
            .sellingPrice(new BigDecimal("500.00"))
            .stockQuantity(5)
            .isActive(true)
            .build());

        int updatedRows = bookVariantRepository.restoreStock(variant.getId(), 4);
        assertThat(updatedRows).isEqualTo(1);

        BookVariantEntity reloaded = bookVariantRepository.findById(variant.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(9);
    }
}
