package com.base.repository;

import com.base.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);
    boolean existsBySku(String sku);

    List<ProductVariant> findByProduct_ProductId(Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId = :productId AND v.stockQuantity <= v.lowStockThreshold")
    List<ProductVariant> findLowStockByProductId(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= v.lowStockThreshold")
    List<ProductVariant> findAllLowStock();
}
