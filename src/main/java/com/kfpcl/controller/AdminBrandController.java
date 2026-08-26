package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.BrandCreateDto;
import com.kfpcl.dto.BrandResponseDto;
import com.kfpcl.dto.BrandUpdateDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalog/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<BrandResponseDto>>> listBrands(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        PageResponseDto<BrandResponseDto> brands = brandService.getBrands(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(brands, "Brands retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponseDto>> createBrand(
            @Valid @RequestBody BrandCreateDto dto) {

        BrandResponseDto created = brandService.createBrand(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Brand created successfully"));
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<ApiResponse<BrandResponseDto>> getBrand(
            @PathVariable String brandId) {

        BrandResponseDto brand = brandService.getBrandById(brandId);
        return ResponseEntity.ok(ApiResponse.success(brand, "Brand retrieved successfully"));
    }

    @RequestMapping(value = "/{brandId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<BrandResponseDto>> updateBrand(
            @PathVariable String brandId,
            @RequestBody BrandUpdateDto dto) {

        BrandResponseDto updated = brandService.updateBrand(brandId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Brand updated successfully"));
    }

    @DeleteMapping("/{brandId}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(
            @PathVariable String brandId) {

        brandService.deleteBrand(brandId);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand deleted/archived successfully"));
    }
}
