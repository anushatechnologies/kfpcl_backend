package com.kfpcl.service;

import com.kfpcl.dto.request.CategoryRequest;
import com.kfpcl.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    /**
     * Retrieves all active categories (flat list or top-level with subcategories) for public catalog.
     */
    List<CategoryResponse> getActiveCategories();

    /**
     * Retrieves hierarchical tree of active categories (top-level parents with their children).
     */
    List<CategoryResponse> getActiveCategoryTree();

    /**
     * Retrieves a single active category by ID.
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Retrieves a single category by slug.
     */
    CategoryResponse getCategoryBySlug(String slug);

    /**
     * Retrieves all categories (including inactive ones) for management.
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Creates a new category.
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Updates an existing category.
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Deactivates or deletes a category.
     */
    void deleteCategory(Long id);

    /**
     * Toggles active status of a category.
     */
    CategoryResponse toggleCategoryStatus(Long id);
}
