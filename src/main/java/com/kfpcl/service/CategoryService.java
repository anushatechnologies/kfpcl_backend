package com.kfpcl.service;

import com.kfpcl.dto.CategoryCreateDto;
import com.kfpcl.dto.CategoryResponseDto;
import com.kfpcl.dto.CategoryUpdateDto;
import com.kfpcl.dto.PageResponseDto;

public interface CategoryService {

    PageResponseDto<CategoryResponseDto> getAllCategories(String search, String status, int page, int size, String sortBy, String sortDir);

    CategoryResponseDto getCategoryById(String categoryId);

    CategoryResponseDto createCategory(CategoryCreateDto dto);

    CategoryResponseDto updateCategory(String categoryId, CategoryUpdateDto dto);

    void deleteCategory(String categoryId);
}
