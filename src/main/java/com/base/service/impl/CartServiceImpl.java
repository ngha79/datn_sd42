package com.base.service.impl;

import com.base.dto.request.cart.CartItemRequest;
import com.base.dto.response.cart.CartItemResponse;
import com.base.dto.response.cart.CartResponse;
import com.base.entity.*;
import com.base.exception.BadRequestException;
import com.base.exception.ForbiddenException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.CartItemRepository;
import com.base.repository.CartRepository;
import com.base.repository.ProductVariantRepository;
import com.base.repository.UserRepository;
import com.base.service.CartService;
import com.base.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final SecurityUtils securityUtils;

    @Override
    public CartResponse getMyCart() {
        Long userId = securityUtils.getCurrentUserId();
        return cartRepository.findByUser_UserId(userId).map(cart -> modelMapper.map(cart, CartResponse.class)).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng!"));
    }

    @Override
    public CartItemResponse addToCart(CartItemRequest cartItemRequest) {
        Long userId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ProductVariant variant = productVariantRepository
                .findByVariantIdAndProduct_StatusAndProduct_DeletedFalse(
                        cartItemRequest.getVariantId(),
                        Product.ProductStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không còn bán!"));

        // Dùng isAvailable() thay vì check thủ công
        if (!variant.isAvailable()) {
            throw new BadRequestException("Sản phẩm hiện không có sẵn!");
        }

        if (cartItemRequest.getQuantity() <= 0) {
            throw new BadRequestException("Số lượng phải lớn hơn 0");
        }

        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));

        CartItem cartItem = cartItemRepository
                .findByCart_CartIdAndVariant_VariantId(cart.getCartId(), variant.getVariantId())
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + cartItemRequest.getQuantity();

            // Dùng getAvailableQuantity() thay vì stockQuantity
            if (newQuantity > variant.getAvailableQuantity()) {
                throw new BadRequestException(
                        "Số lượng vượt quá tồn kho. Hiện còn " + variant.getAvailableQuantity()
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {
            if (cartItemRequest.getQuantity() > variant.getAvailableQuantity()) {
                throw new BadRequestException(
                        "Số lượng vượt quá tồn kho. Hiện còn " + variant.getAvailableQuantity()
                );
            }

            cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(cartItemRequest.getQuantity())
                    .build();
        }

        cartItem = cartItemRepository.save(cartItem);

        return toResponse(cartItem);
    }

    @Override
    public CartItemResponse updateQuantity(CartItemRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository
                .findByCart_CartIdAndVariant_VariantId(cart.getCartId(), request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        ProductVariant variant = cartItem.getVariant();

        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Dùng getAvailableQuantity()
        if (request.getQuantity() > variant.getAvailableQuantity()) {
            throw new BadRequestException("Only " + variant.getAvailableQuantity() + " items left");
        }

        cartItem.setQuantity(request.getQuantity());

        return toResponse(cartItemRepository.save(cartItem));
    }

    @Override
    public CartItemResponse increaseQuantity(Long cartItemId) {
        Long userId = securityUtils.getCurrentUserId();

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateOwnership(cartItem, userId);

        if (cartItem.getQuantity() + 1 > cartItem.getVariant().getAvailableQuantity()) {
            throw new BadRequestException("Not enough stock");
        }

        cartItem.setQuantity(cartItem.getQuantity() + 1);

        return toResponse(cartItemRepository.save(cartItem));
    }

    @Override
    public Optional<CartItemResponse> decreaseQuantity(Long cartItemId) {
        Long userId = securityUtils.getCurrentUserId();

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateOwnership(cartItem, userId);

        if (cartItem.getQuantity() <= 1) {
            cartItemRepository.delete(cartItem);
            return Optional.empty();
        }

        cartItem.setQuantity(cartItem.getQuantity() - 1);

        return Optional.of(toResponse(cartItemRepository.save(cartItem)));
    }

    @Override
    public void removeItem(Long cartItemId) {
        Long userId = securityUtils.getCurrentUserId();

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Kiểm tra ownership
        validateOwnership(cartItem, userId);

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void removeItems(List<Long> cartItemIds) {
        Long userId = securityUtils.getCurrentUserId();

        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> items = cartItemRepository.findAllById(cartItemIds);

        List<CartItem> ownedItems = items.stream()
                .filter(item -> item.getCart().getCartId().equals(cart.getCartId()))
                .toList();

        if (ownedItems.size() != cartItemIds.size()) {
            throw new BadRequestException("Một số item không thuộc giỏ hàng của bạn");
        }

        cartItemRepository.deleteAll(ownedItems);
    }

    @Override
    public void clearCart() {
        Long userId = securityUtils.getCurrentUserId();

        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCart_CartId(cart.getCartId());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countItems() {
        Long userId = securityUtils.getCurrentUserId();

        return cartRepository.findByUser_UserId(userId)
                .map(cart -> cart.getItems().stream()
                        .mapToLong(CartItem::getQuantity)
                        .sum())
                .orElse(0L);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsInCart(Long variantId) {
        Long userId = securityUtils.getCurrentUserId();

        return cartRepository.findByUser_UserId(userId)
                .map(cart -> cartItemRepository.existsByCart_CartIdAndVariant_VariantId(
                        cart.getCartId(), variantId))
                .orElse(false); // Trả false thay vì throw
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotal() {
        Long userId = securityUtils.getCurrentUserId();

        return cartRepository.findByUser_UserId(userId)
                .map(cart -> cart.getItems().stream()
                        .map(item -> item.getVariant().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .orElse(BigDecimal.ZERO);
    }

    // ==================== PRIVATE HELPERS ====================

    private void validateOwnership(CartItem cartItem, Long userId) {
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác item này");
        }
    }

    private CartItemResponse toResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getVariant();

        String imageUrl = null;
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            imageUrl = variant.getImages().stream()
                    .filter(ProductImage::isThumbnail)
                    .findFirst()
                    .orElse(variant.getImages().get(0))
                    .getImageUrl();
        }

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .variantId(variant.getVariantId())
                .productId(variant.getProduct().getProductId())
                .productName(variant.getProduct().getProductName())
                .imageUrl(imageUrl)
                .color(variant.getColor())
                .size(variant.getSize())
                .price(variant.getPrice())
                .quantity(cartItem.getQuantity())
                .stockQuantity(variant.getAvailableQuantity())
                .subtotal(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .build();
    }
}
