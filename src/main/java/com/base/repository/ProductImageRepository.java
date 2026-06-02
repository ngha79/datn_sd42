package com.base.repository;

import com.base.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByVariant_VariantId(Long variantId);
    Optional<ProductImage> findByVariant_VariantIdAndIsThumbnail(Long variantId, boolean isThumbnail);
}
