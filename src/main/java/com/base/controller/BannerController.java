package com.base.controller;

import com.base.dto.request.banner.BannerCreateRequest;
import com.base.dto.request.banner.BannerUpdateRequest;
import com.base.dto.response.ApiResponse;
import com.base.dto.response.banner.BannerResponse;
import com.base.entity.Banner;
import com.base.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BannerController {
    private final BannerService bannerService;

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<Page<BannerResponse>>> getAllBanners(
            @RequestParam(defaultValue = "HOME_TOP") Banner.BannerPosition position,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, sort);
        Pageable pageable = PageRequest.of(0, 10, sortBy);

        return ResponseEntity.ok(ApiResponse.success(bannerService.getBanners(position, isActive, pageable)));
    }

    @GetMapping("/banners/{bannerId}")
    public ResponseEntity<ApiResponse<BannerResponse>> getBanner(@PathVariable Long bannerId) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.getBanner(bannerId)));
    }

    @PostMapping(
            value = "/admin/banners",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(
            @ModelAttribute BannerCreateRequest bannerCreateRequest,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.createBanner(bannerCreateRequest, file)));
    }

    @PutMapping(
            value = "/admin/banners/{bannerId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(
            @PathVariable Long bannerId,
            @ModelAttribute BannerUpdateRequest bannerUpdateRequest,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.updateBanner(bannerId, bannerUpdateRequest, file)));
    }

    @DeleteMapping("/admin/banners/{bannerId}")
    public ResponseEntity<ApiResponse<BannerResponse>> deleteBanner(@PathVariable Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/banners/{bannerId}")
    public ResponseEntity<ApiResponse<Void>> updateStatusBanner(@PathVariable Long bannerId) {
        bannerService.updateStatusBanner(bannerId);
        return ResponseEntity.ok().build();
    }
}
