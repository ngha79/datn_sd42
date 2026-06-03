package com.base.repository;

import com.base.dto.response.product.TopWishlistProductProjection;
import com.base.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUser_UserId(Long userId, Pageable pageable);

    Optional<Wishlist> findByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    boolean existsByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    void deleteByUser_UserIdAndVariant_VariantId(Long userId, Long variantId);

    long countByUserUserId(Long userId);

    void deleteAllByUser_UserId(Long userId);

    @Query("""
    SELECT w
    FROM Wishlist w
    WHERE w.user.userId = :userId
    AND (
        :keyword IS NULL
        OR LOWER(w.variant.product.productName)
        LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    Page<Wishlist> findWishlistByUserAndKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT
        pv.product.productId,
        pv.product.productName,
        COUNT(w)
    FROM Wishlist w
    JOIN w.variant pv
    GROUP BY
        pv.product.productId,
        pv.product.productName
    ORDER BY COUNT(w) DESC
""")
    List<TopWishlistProductProjection> getTopWishlistProducts();
}
