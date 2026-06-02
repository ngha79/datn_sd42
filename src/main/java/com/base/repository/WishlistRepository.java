package com.base.repository;

import com.base.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUser_UserId(Long userId, Pageable pageable);

    Optional<Wishlist> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    boolean existsByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    void deleteByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);
}
