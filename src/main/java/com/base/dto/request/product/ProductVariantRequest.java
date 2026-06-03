package com.base.dto.request.product;

import com.base.entity.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank
    private String sku;

    private String barcode;
    private String size;
    private String color;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private Integer stockQuantity = 0;
    private Integer reservedQuantity = 0;
    private Integer lowStockThreshold = 5;
    private BigDecimal weight;


}
