package com.base.service.impl;

import com.base.dto.response.product.TopWishlistProductResponse;
import com.base.dto.response.wishlist.WishlistResponse;
import com.base.entity.ProductImage;
import com.base.entity.ProductVariant;
import com.base.entity.User;
import com.base.entity.Wishlist;
import com.base.exception.ResourceAlreadyExistsException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.ProductVariantRepository;
import com.base.repository.UserRepository;
import com.base.repository.WishlistRepository;
import com.base.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    public WishlistResponse addToWishlist(Long userId, Long variantId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("ProductVariant", "id", variantId));

        if (wishlistRepository.existsByUser_UserIdAndVariant_VariantId(userId, variantId)) {
            throw new ResourceAlreadyExistsException("Product already exists in wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .variant(variant)
                .build();

        wishlist = wishlistRepository.save(wishlist);

        return convertToDto(wishlist);
    }

    @Override
    public void removeFromWishlist(Long userId, Long variantId) {

        if (!wishlistRepository.existsByUser_UserIdAndVariant_VariantId(userId, variantId)) {
            throw new ResourceNotFoundException(
                    "Wishlist", "variantId", variantId
            );
        }

        wishlistRepository.deleteByUser_UserIdAndVariant_VariantId(
                userId,
                variantId
        );
    }

    @Override
    public boolean toggleWishlist(Long userId, Long variantId) {

        boolean exists = wishlistRepository
                .existsByUser_UserIdAndVariant_VariantId(
                        userId,
                        variantId
                );

        if (exists) {

            wishlistRepository.deleteByUser_UserIdAndVariant_VariantId(
                    userId,
                    variantId
            );

            return false;
        }

        addToWishlist(userId, variantId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsInWishlist(Long userId, Long variantId) {

        return wishlistRepository
                .existsByUser_UserIdAndVariant_VariantId(
                        userId,
                        variantId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WishlistResponse> getUserWishlist(
            Long userId,
            String keyword,
            Pageable pageable
    ) {

        return wishlistRepository
                .findWishlistByUserAndKeyword(userId, keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countWishlist(Long userId) {
        return wishlistRepository.countByUserUserId(userId);
    }

    @Override
    public void clearWishlist(Long userId) {
        wishlistRepository.deleteAllByUser_UserId(userId);
    }

    public WishlistResponse convertToDto(Wishlist wishlist) {
        if (wishlist == null) return null;

        WishlistResponse response = new WishlistResponse();
        response.setWishlistId(wishlist.getWishlistId());
        response.setCreatedAt(wishlist.getCreatedAt());

        ProductVariant variant = wishlist.getVariant();
        if (variant != null) {
            response.setVariantId(variant.getVariantId());
            response.setColor(variant.getColor());
            response.setSize(variant.getSize());
            response.setPrice(variant.getPrice());

            if (variant.getProduct() != null) {
                response.setProductId(variant.getProduct().getProductId());
                response.setProductName(variant.getProduct().getProductName());
            }

            if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                String defaultImageUrl = variant.getImages().stream()
                        .filter(ProductImage::isThumbnail)
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(variant.getImages().get(0).getImageUrl());

                response.setImageUrl(defaultImageUrl);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopWishlistProductResponse> getTopWishlistProducts(
            Integer limit
    ) {

        return wishlistRepository
                .getTopWishlistProducts()
                .stream()
                .limit(limit)
                .map(item ->
                        TopWishlistProductResponse.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .wishlistCount(item.getWishlistCount())
                                .build()
                )
                .toList();
    }
}
