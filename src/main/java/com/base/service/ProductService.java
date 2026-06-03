package com.base.service;

import com.base.dto.request.product.ProductRequest;
import com.base.dto.response.product.ProductResponse;
import com.base.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);
    ProductResponse getById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    void restoreProduct(Long id);

    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> filterProducts(Long categoryId, Long brandId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         Product.ProductStatus status, String keyword,
                                         Pageable pageable);

    ProductResponse changeStatus(Long id, Product.ProductStatus status);

    void bulkDelete(List<Long> ids);
    void bulkUpdateStatus(List<Long> ids, Product.ProductStatus status);

    void updateRating(Long productId, BigDecimal newRating);

    List<ProductResponse> getTopRated(int limit);
    Page<ProductResponse> getDeleted(Pageable pageable);
}
