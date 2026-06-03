package com.base.dto.response.product;

import com.base.entity.Product;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String categoryName;
    private String brandName;
    private Product.ProductStatus status;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
