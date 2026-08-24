package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.SubcategoryCreateDto;
import com.kfpcl.dto.SubcategoryResponseDto;
import com.kfpcl.dto.SubcategoryUpdateDto;
import com.kfpcl.service.SubcategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
public class AdminSubcategoryController {

    private final SubcategoryService subcategoryService;

    @GetMapping("/subcategories")
    public ResponseEntity<ApiResponse<PageResponseDto<SubcategoryResponseDto>>> listSubcategories(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        PageResponseDto<SubcategoryResponseDto> subcategories = subcategoryService.getAllSubcategories(categoryId, search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(subcategories, "Subcategories retrieved successfully"));
    }

    @PostMapping("/subcategories")
    public ResponseEntity<ApiResponse<SubcategoryResponseDto>> createSubcategory(
            @Valid @RequestBody SubcategoryCreateDto dto) {

        SubcategoryResponseDto created = subcategoryService.createSubcategory(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Subcategory created successfully"));
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<ApiResponse<List<SubcategoryResponseDto>>> listCategorySubcategories(
            @PathVariable String categoryId) {

        List<SubcategoryResponseDto> subcategories = subcategoryService.getSubcategoriesByCategoryId(categoryId);
        return ResponseEntity.ok(ApiResponse.success(subcategories, "Category subcategories retrieved successfully"));
    }

    @GetMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<ApiResponse<SubcategoryResponseDto>> getSubcategory(
            @PathVariable String subcategoryId) {

        SubcategoryResponseDto subcategory = subcategoryService.getSubcategoryById(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success(subcategory, "Subcategory details retrieved successfully"));
    }

    @PatchMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<ApiResponse<SubcategoryResponseDto>> updateSubcategory(
            @PathVariable String subcategoryId,
            @RequestBody SubcategoryUpdateDto dto) {

        SubcategoryResponseDto updated = subcategoryService.updateSubcategory(subcategoryId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Subcategory updated successfully"));
    }

    @DeleteMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubcategory(
            @PathVariable String subcategoryId) {

        subcategoryService.deleteSubcategory(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subcategory deleted/archived successfully"));
    }
}
