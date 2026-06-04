package com.base.repository;

import com.base.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_CartIdAndVariant_VariantId(Long cartId, Long variantId);

    @Modifying
    void deleteByCart_CartId(Long cartId);

    boolean existsByCart_CartIdAndVariant_VariantId(Long cartCartId, Long variantVariantId);
}
