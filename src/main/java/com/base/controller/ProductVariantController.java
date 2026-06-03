package com.base.controller;

import com.base.dto.request.product.ProductVariantRequest;
import com.base.dto.response.product.ProductImageResponse;
import com.base.dto.response.product.ProductVariantResponse;
import com.base.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping("/admin/products/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> addVariant(
            @PathVariable Long productId,
            @RequestBody @Valid ProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(variantService.addVariant(productId, request));
    }

    @PutMapping("/admin/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long variantId,
            @RequestBody @Valid ProductVariantRequest request) {
        return ResponseEntity.ok(variantService.updateVariant(variantId, request));
    }

    @DeleteMapping("/admin/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<ProductVariantResponse>> getVariants(
            @PathVariable Long productId) {
        return ResponseEntity.ok(variantService.getVariantsByProduct(productId));
    }

    @PostMapping(value = "/admin/variants/{variantId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable Long variantId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isThumbnail) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(variantService.addImage(variantId, file, isThumbnail));
    }

    @DeleteMapping("/admin/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        variantService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/images/{imageId}/thumbnail")
    public ResponseEntity<ProductImageResponse> setThumbnail(@PathVariable Long imageId) {
        return ResponseEntity.ok(variantService.setThumbnail(imageId));
    }
}
