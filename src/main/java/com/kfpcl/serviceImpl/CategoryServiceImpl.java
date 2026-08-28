package com.kfpcl.serviceImpl;

import com.kfpcl.dto.CategoryCreateDto;
import com.kfpcl.dto.CategoryResponseDto;
import com.kfpcl.dto.CategoryUpdateDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Subcategory;
import com.kfpcl.exception.*;
import com.kfpcl.repository.*;
import com.kfpcl.service.CategoryService;
import com.kfpcl.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ReviewRepository reviewRepository;
    private final ImageUtils imageUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CategoryResponseDto> getAllCategories(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categoryPage;

        if (StringUtils.hasText(search) && StringUtils.hasText(status)) {
            Category.Status catStatus = parseStatus(status);
            categoryPage = categoryRepository.findByNameContainingIgnoreCaseAndStatus(search.trim(), catStatus, pageable);
        } else if (StringUtils.hasText(search)) {
            categoryPage = categoryRepository.findByNameContainingIgnoreCaseAndStatusNot(search.trim(), Category.Status.ARCHIVED, pageable);
        } else if (StringUtils.hasText(status)) {
            Category.Status catStatus = parseStatus(status);
            categoryPage = categoryRepository.findByStatus(catStatus, pageable);
        } else {
            categoryPage = categoryRepository.findByStatusNot(Category.Status.ARCHIVED, pageable);
        }

        List<CategoryResponseDto> dtoList = categoryPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(categoryPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        return mapToDto(category);
    }

    @Override
    public CategoryResponseDto createCategory(CategoryCreateDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new DuplicateResourceException("Category", "name", dto.getName());
        }

        String categoryId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "cat_" + slugify(dto.getName());

        if (categoryRepository.existsById(categoryId)) {
            categoryId = categoryId + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        Category.Status status = Category.Status.ACTIVE;
        if (dto.getIsActive() != null && !dto.getIsActive()) {
            status = Category.Status.INACTIVE;
        }
        if (StringUtils.hasText(dto.getStatus())) {
            status = parseStatus(dto.getStatus());
        }

        Category category = Category.builder()
                .id(categoryId)
                .name(dto.getName().trim())
                .imageUrl(imageUtils.processBase64Image(dto.getImageUrl()))
                .description(dto.getDescription())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1)
                .discount(dto.getDiscount() != null ? dto.getDiscount() : 0.0)
                .status(status)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(String categoryId, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        if (StringUtils.hasText(dto.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), categoryId)) {
                throw new DuplicateResourceException("Category", "name", dto.getName());
            }
            category.setName(dto.getName().trim());
        }

        if (dto.getImageUrl() != null) {
            category.setImageUrl(imageUtils.processBase64Image(dto.getImageUrl()));
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getDisplayOrder() != null) {
            category.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getDiscount() != null) {
            category.setDiscount(dto.getDiscount());
        }
        if (dto.getIsActive() != null) {
            category.setStatus(dto.getIsActive() ? Category.Status.ACTIVE : Category.Status.INACTIVE);
        }
        if (StringUtils.hasText(dto.getStatus())) {
            category.setStatus(parseStatus(dto.getStatus()));
        }

        Category updated = categoryRepository.save(category);
        return mapToDto(updated);
    }

    @Override
    public void deleteCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // 1. Find and physically delete all products linked to this category
        List<Product> products = productRepository.findByCategoryId(categoryId);
        for (Product product : products) {
            inventoryRepository.findByProductId(product.getId()).ifPresent(inv -> {
                inventoryLogRepository.deleteByInventoryId(inv.getId());
                inventoryRepository.delete(inv);
            });
            inventoryLogRepository.deleteByProductId(product.getId());
            reviewRepository.deleteByProductId(product.getId());
            productRepository.delete(product);
        }

        // 2. Physically delete all subcategories linked to this category
        List<Subcategory> subcategories = subcategoryRepository.findByCategoryId(categoryId);
        if (!subcategories.isEmpty()) {
            subcategoryRepository.deleteAll(subcategories);
        }

        // 3. Physically delete the category row from database
        categoryRepository.delete(category);
    }

    private Category.Status parseStatus(String statusStr) {
        try {
            return Category.Status.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Category.Status.ACTIVE;
        }
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private CategoryResponseDto mapToDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .discount(category.getDiscount())
                .status(category.getStatus() != null ? category.getStatus().name() : Category.Status.ACTIVE.name())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
