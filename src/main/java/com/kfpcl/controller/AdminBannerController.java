package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/banners")
public class AdminBannerController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBanners() {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "Banners retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBanner(@RequestBody Map<String, Object> dto) {
        dto.put("id", "banner-" + System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Banner created successfully"));
    }

    @PatchMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBanner(
            @PathVariable String bannerId, @RequestBody Map<String, Object> dto) {
        dto.put("id", bannerId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Banner updated successfully"));
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<String>> deleteBanner(@PathVariable String bannerId) {
        return ResponseEntity.ok(ApiResponse.success("Success", "Banner deleted successfully"));
    }

    @PatchMapping("/{bannerId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBannerStatus(
            @PathVariable String bannerId, @RequestBody Map<String, Object> dto) {
        dto.put("id", bannerId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Banner status updated successfully"));
    }
}
