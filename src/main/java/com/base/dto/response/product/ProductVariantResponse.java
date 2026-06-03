package com.base.dto.response.product;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long variantId;
    private String sku;
    private String barcode;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer lowStockThreshold;
    private BigDecimal weight;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
}