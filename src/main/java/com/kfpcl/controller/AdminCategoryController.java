package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.CategoryCreateDto;
import com.kfpcl.dto.CategoryResponseDto;
import com.kfpcl.dto.CategoryUpdateDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalog/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<CategoryResponseDto>>> listCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        PageResponseDto<CategoryResponseDto> categories = categoryService.getAllCategories(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CategoryCreateDto dto) {

        CategoryResponseDto created = categoryService.createCategory(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Category created successfully"));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategory(
            @PathVariable String categoryId) {

        CategoryResponseDto category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(category, "Category details retrieved successfully"));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable String categoryId,
            @RequestBody CategoryUpdateDto dto) {

        CategoryResponseDto updated = categoryService.updateCategory(categoryId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Category updated successfully"));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable String categoryId) {

        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted/archived successfully"));
    }
}
