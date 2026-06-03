package com.base.service;

import com.base.dto.request.product.ProductVariantRequest;
import com.base.dto.response.product.ProductImageResponse;
import com.base.dto.response.product.ProductVariantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponse addVariant(Long productId, ProductVariantRequest request);

    ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request);

    void deleteVariant(Long variantId);

    List<ProductVariantResponse> getVariantsByProduct(Long productId);

    ProductImageResponse addImage(Long variantId, MultipartFile file, boolean isThumbnail);

    void deleteImage(Long imageId);

    ProductImageResponse setThumbnail(Long imageId);
}