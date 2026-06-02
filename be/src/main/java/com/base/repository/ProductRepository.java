package com.base.repository;

import com.base.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductIdAndDeletedFalse(Long productId);

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByCategory_CategoryIdAndDeletedFalse(Long categoryId, Pageable pageable);

    Page<Product> findByBrand_BrandIdAndDeletedFalse(Long brandId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.deleted = false
          AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
          AND (:brandId IS NULL OR p.brand.brandId = :brandId)
          AND (:status IS NULL OR p.status = :status)
    """)
    Page<Product> search(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("brandId") Long brandId,
        @Param("status") Product.ProductStatus status,
        Pageable pageable
    );
}
