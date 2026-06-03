package com.base.repository;

import com.base.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductIdAndDeletedFalse(Long productId);

    Page<Product> findByDeletedFalse(Pageable pageable);

    boolean existsByCategory_CategoryId(Long categoryId);

    boolean existsByBrand_BrandId(Long brandId);

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

    Page<Product> findAllByDeletedFalse(Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCaseAndDeletedFalse(
            String keyword, Pageable pageable);



    Page<Product> findByStatusAndDeletedFalse(Product.ProductStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.deleted = false
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:brandId IS NULL OR p.brand.id = :brandId)
          AND (:minPrice IS NULL OR p.basePrice >= :minPrice)
          AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)
          AND (:status IS NULL OR p.status = :status)
          AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") Product.ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByProductNameAndDeletedFalse(String productName);

    Page<Product> findAllByDeletedTrue(Pageable pageable);

    List<Product> findTop10ByDeletedFalseOrderByAverageRatingDesc();
}
