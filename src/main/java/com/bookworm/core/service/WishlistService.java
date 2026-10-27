package com.bookworm.core.service;

import com.bookworm.api.v1.dto.WishlistDTOs;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.infra.persistence.entity.BookEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.entity.WishlistEntity;
import com.bookworm.infra.persistence.repository.BookRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import com.bookworm.infra.persistence.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public WishlistDTOs.WishlistResponse getWishlist(UUID userId, Pageable pageable) {
        log.info("Fetching wishlist for user ID: {}", userId);
        Page<WishlistEntity> page = wishlistRepository.findByUserIdWithBook(userId, pageable);

        List<WishlistDTOs.WishlistItemDTO> items = page.getContent().stream()
            .map(w -> {
                BookEntity book = w.getBook();
                String author = (book.getBookAuthors() != null && !book.getBookAuthors().isEmpty())
                    ? book.getBookAuthors().get(0).getAuthor().getName() : "";
                BigDecimal price = (book.getVariants() != null && !book.getVariants().isEmpty())
                    ? book.getVariants().get(0).getSellingPrice() : BigDecimal.ZERO;

                return WishlistDTOs.WishlistItemDTO.builder()
                    .wishlistId(w.getId())
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .authorName(author)
                    .coverImageUrl(book.getCoverImageUrl())
                    .sellingPrice(price)
                    .rating(book.getAverageRating())
                    .addedAt(w.getCreatedAt())
                    .build();
            })
            .toList();

        return WishlistDTOs.WishlistResponse.builder()
            .items(items)
            .totalItems(page.getTotalElements())
            .build();
    }

    @Transactional
    public WishlistDTOs.WishlistActionResponse addToWishlist(UUID userId, UUID bookId) {
        log.info("Adding book ID: {} to wishlist of user ID: {}", bookId, userId);

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            WishlistEntity wishlist = WishlistEntity.builder()
                .user(user)
                .book(book)
                .build();
            wishlistRepository.save(wishlist);
        }

        return WishlistDTOs.WishlistActionResponse.builder()
            .bookId(bookId)
            .inWishlist(true)
            .message("Book added to your wishlist.")
            .build();
    }

    @Transactional
    public void removeFromWishlist(UUID userId, UUID bookId) {
        log.info("Removing book ID: {} from wishlist of user ID: {}", bookId, userId);
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}
