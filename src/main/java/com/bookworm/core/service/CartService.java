package com.bookworm.core.service;

import com.bookworm.api.v1.dto.CartDTOs;
import com.bookworm.core.common.exception.BusinessException;
import com.bookworm.core.common.exception.InsufficientStockException;
import com.bookworm.core.common.exception.ResourceNotFoundException;
import com.bookworm.core.mapper.CartMapper;
import com.bookworm.infra.persistence.entity.BookVariantEntity;
import com.bookworm.infra.persistence.entity.CartEntity;
import com.bookworm.infra.persistence.entity.CartItemEntity;
import com.bookworm.infra.persistence.entity.UserEntity;
import com.bookworm.infra.persistence.repository.BookVariantRepository;
import com.bookworm.infra.persistence.repository.CartItemRepository;
import com.bookworm.infra.persistence.repository.CartRepository;
import com.bookworm.infra.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookVariantRepository bookVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDTOs.CartDTO getActiveCart(UUID userId, String guestToken) {
        CartEntity cart = resolveOrCreateActiveCart(userId, guestToken);
        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTOs.CartDTO addItemToCart(UUID userId, String guestToken, CartDTOs.AddCartItemRequest request) {
        log.info("Adding item to cart - variant ID: {}, qty: {}", request.getVariantId(), request.getQuantity());

        CartEntity cart = resolveOrCreateActiveCart(userId, guestToken);
        BookVariantEntity variant = bookVariantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("BookVariant", request.getVariantId()));

        if (!variant.isActive()) {
            throw new BusinessException("VARIANT_INACTIVE", "This book format is currently unavailable for purchase.");
        }

        Optional<CartItemEntity> existingItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId());
        int targetQuantity = request.getQuantity();

        if (existingItem.isPresent()) {
            targetQuantity += existingItem.get().getQuantity();
        }

        if (targetQuantity > 20) {
            throw new BusinessException("MAX_CART_ITEM_LIMIT", "Cannot add more than 20 units of a single title.");
        }

        if (variant.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException(variant.getSku(), targetQuantity, variant.getStockQuantity());
        }

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(targetQuantity);
            item.setUnitPriceAtAddition(variant.getSellingPrice());
            cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                .cart(cart)
                .variant(variant)
                .quantity(targetQuantity)
                .unitPriceAtAddition(variant.getSellingPrice())
                .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTOs.CartDTO updateItemQuantity(UUID userId, String guestToken, UUID cartItemId, int quantity) {
        log.info("Updating cart item ID: {} to quantity: {}", cartItemId, quantity);

        CartEntity cart = resolveOrCreateActiveCart(userId, guestToken);
        CartItemEntity item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("CartItem", cartItemId);
        }

        if (item.getVariant().getStockQuantity() < quantity) {
            throw new InsufficientStockException(item.getVariant().getSku(), quantity, item.getVariant().getStockQuantity());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTOs.CartDTO removeItemFromCart(UUID userId, String guestToken, UUID cartItemId) {
        log.info("Removing cart item ID: {}", cartItemId);

        CartEntity cart = resolveOrCreateActiveCart(userId, guestToken);
        CartItemEntity item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (item.getCart().getId().equals(cart.getId())) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        }

        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTOs.CartDTO mergeGuestCart(UUID userId, String guestSessionToken) {
        log.info("Merging guest cart token: {} into user ID: {}", guestSessionToken, userId);

        Optional<CartEntity> guestCartOpt = cartRepository.findActiveCartByGuestTokenWithItems(guestSessionToken);
        CartEntity userCart = resolveOrCreateActiveCart(userId, null);

        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return cartMapper.toDTO(userCart);
        }

        CartEntity guestCart = guestCartOpt.get();
        for (CartItemEntity guestItem : guestCart.getItems()) {
            Optional<CartItemEntity> existingUserItem = cartItemRepository.findByCartIdAndVariantId(
                userCart.getId(),
                guestItem.getVariant().getId()
            );

            if (existingUserItem.isPresent()) {
                CartItemEntity userItem = existingUserItem.get();
                int combinedQty = Math.min(20, userItem.getQuantity() + guestItem.getQuantity());
                userItem.setQuantity(combinedQty);
                cartItemRepository.save(userItem);
            } else {
                CartItemEntity newItem = CartItemEntity.builder()
                    .cart(userCart)
                    .variant(guestItem.getVariant())
                    .quantity(guestItem.getQuantity())
                    .unitPriceAtAddition(guestItem.getUnitPriceAtAddition())
                    .build();
                userCart.getItems().add(newItem);
                cartItemRepository.save(newItem);
            }
        }

        guestCart.setStatus("MERGED");
        cartRepository.save(guestCart);

        return cartMapper.toDTO(userCart);
    }

    private CartEntity resolveOrCreateActiveCart(UUID userId, String guestToken) {
        if (userId != null) {
            return cartRepository.findActiveCartByUserIdWithItems(userId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    CartEntity newCart = CartEntity.builder()
                        .user(user)
                        .status("ACTIVE")
                        .expiresAt(Instant.now().plus(90, ChronoUnit.DAYS))
                        .build();
                    return cartRepository.save(newCart);
                });
        } else if (guestToken != null && !guestToken.isBlank()) {
            return cartRepository.findActiveCartByGuestTokenWithItems(guestToken)
                .orElseGet(() -> {
                    CartEntity newCart = CartEntity.builder()
                        .guestSessionToken(guestToken)
                        .status("ACTIVE")
                        .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                        .build();
                    return cartRepository.save(newCart);
                });
        } else {
            throw new BusinessException("MISSING_SESSION", "Either authenticated user session or guest session token is required");
        }
    }
}
