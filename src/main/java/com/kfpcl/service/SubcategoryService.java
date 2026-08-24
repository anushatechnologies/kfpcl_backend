package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.SubcategoryCreateDto;
import com.kfpcl.dto.SubcategoryResponseDto;
import com.kfpcl.dto.SubcategoryUpdateDto;

import java.util.List;

public interface SubcategoryService {

    PageResponseDto<SubcategoryResponseDto> getAllSubcategories(String categoryId, String search, String status, int page, int size, String sortBy, String sortDir);

    List<SubcategoryResponseDto> getSubcategoriesByCategoryId(String categoryId);

    SubcategoryResponseDto getSubcategoryById(String subcategoryId);

    SubcategoryResponseDto createSubcategory(SubcategoryCreateDto dto);

    SubcategoryResponseDto updateSubcategory(String subcategoryId, SubcategoryUpdateDto dto);

    void deleteSubcategory(String subcategoryId);
}
