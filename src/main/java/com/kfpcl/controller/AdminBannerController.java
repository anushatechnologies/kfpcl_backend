package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.BannerDto;
import com.kfpcl.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/banners", "/api/v1/banners"})
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerDto>>> getBanners() {
        List<BannerDto> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ApiResponse.success(banners, "Banners retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BannerDto>>> getActiveBanners() {
        List<BannerDto> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success(banners, "Active banners retrieved successfully"));
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<BannerDto>> getBanner(@PathVariable String bannerId) {
        BannerDto banner = bannerService.getBannerById(bannerId);
        return ResponseEntity.ok(ApiResponse.success(banner, "Banner details retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerDto>> createBanner(@RequestBody BannerDto dto) {
        BannerDto created = bannerService.createBanner(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Banner created successfully"));
    }

    @RequestMapping(value = "/{bannerId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<BannerDto>> updateBanner(
            @PathVariable String bannerId,
            @RequestBody BannerDto dto) {
        BannerDto updated = bannerService.updateBanner(bannerId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Banner updated successfully"));
    }

    @RequestMapping(value = "/{bannerId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<BannerDto>> updateBannerStatus(
            @PathVariable String bannerId,
            @RequestBody Map<String, Object> body) {
        String status = body.get("status") != null ? body.get("status").toString() : null;
        Boolean active = null;
        if (body.containsKey("active")) {
            active = Boolean.valueOf(body.get("active").toString());
        } else if (body.containsKey("isActive")) {
            active = Boolean.valueOf(body.get("isActive").toString());
        }
        BannerDto updated = bannerService.updateBannerStatus(bannerId, status, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "Banner status updated successfully"));
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable String bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Banner deleted successfully"));
    }
}
