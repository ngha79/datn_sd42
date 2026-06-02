package com.base.controller;

import com.base.dto.request.brand.CreateBrandRequest;
import com.base.dto.request.brand.UpdateBrandRequest;
import com.base.dto.response.ApiResponse;
import com.base.entity.Brand;
import com.base.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BrandController {
    private final BrandService brandService;

    @GetMapping("/brand")
    public ResponseEntity<ApiResponse<Page<Brand>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brandId") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success(brandService.getCategories(pageable)));
    }

    @GetMapping("/brand{id}")
    public ResponseEntity<ApiResponse<Brand>> getBrand(@PathVariable Long id) {
        Brand brand = brandService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(brand));
    }

    @PostMapping("/admin/brand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Brand>> createBrand(@RequestBody CreateBrandRequest brand) {
        return ResponseEntity.ok((ApiResponse.success(brandService.save(brand))));
    }

    @PutMapping("/admin/brand/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Brand>> updateBrand(@PathVariable Long id, @RequestBody UpdateBrandRequest brand) {
        return ResponseEntity.ok((ApiResponse.success(brandService.update(id, brand))));
    }

    @DeleteMapping("/admin/brand/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createBrand(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.ok().build();
    }
}
