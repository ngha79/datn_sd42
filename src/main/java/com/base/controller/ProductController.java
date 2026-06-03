package com.base.controller;

import com.base.dto.request.product.ProductRequest;
import com.base.dto.response.product.ProductResponse;
import com.base.entity.Product;
import com.base.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    // ───── CRUD ─────

    @PostMapping("/admin/products")
    public ResponseEntity<ProductResponse> create(
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/products/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        productService.restoreProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ───── Query & Filter ─────

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/products/filter")
    public ResponseEntity<Page<ProductResponse>> filter(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Product.ProductStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.filterProducts(
                categoryId, brandId, minPrice, maxPrice, status, keyword, pageable));
    }

    @GetMapping("/products/top-rated")
    public ResponseEntity<List<ProductResponse>> getTopRated(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getTopRated(limit));
    }

    @GetMapping("/admin/products/deleted")
    public ResponseEntity<Page<ProductResponse>> getDeleted(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getDeleted(pageable));
    }

    // ───── Trạng thái ─────

    @PatchMapping("/admin/products/{id}/status")
    public ResponseEntity<ProductResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam Product.ProductStatus status) {
        return ResponseEntity.ok(productService.changeStatus(id, status));
    }

    // ───── Bulk ─────

    @DeleteMapping("/admin/products/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<Long> ids) {
        productService.bulkDelete(ids);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/products/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatus(
            @RequestBody List<Long> ids,
            @RequestParam Product.ProductStatus status) {
        productService.bulkUpdateStatus(ids, status);
        return ResponseEntity.noContent().build();
    }

    // ───── Rating ─────

    @PatchMapping("/admin/products/{id}/rating")
    public ResponseEntity<Void> updateRating(
            @PathVariable Long id,
            @RequestParam @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal rating) {
        productService.updateRating(id, rating);
        return ResponseEntity.noContent().build();
    }
}