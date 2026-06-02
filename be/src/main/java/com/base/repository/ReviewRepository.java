package com.base.repository;

import com.base.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_ProductIdAndStatus(Long productId, Review.ReviewStatus status, Pageable pageable);

    boolean existsByOrderDetail_OrderDetailId(Long orderDetailId);

    Optional<Review> findByUser_UserIdAndOrderDetail_OrderDetailId(Long userId, Long orderDetailId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Long countApprovedByProductId(@Param("productId") Long productId);

    Page<Review> findByUser_UserId(Long userId, Pageable pageable);
}
