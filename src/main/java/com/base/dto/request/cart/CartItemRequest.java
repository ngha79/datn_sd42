package com.base.dto.request.cart;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long variantId;
    private Integer quantity;
}
