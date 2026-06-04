package com.base.dto.response.cart;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long cartItemId;

    private Long variantId;

    private Long productId;

    private String productName;

    private String brandName;

    private String categoryName;

    private String sku;

    private String imageUrl;

    private String color;

    private String size;

    private BigDecimal price;

    private Integer quantity;

    private Integer stockQuantity;

    private BigDecimal subtotal;
}