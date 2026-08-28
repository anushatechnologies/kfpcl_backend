package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.SubcategoryCreateDto;
import com.kfpcl.dto.SubcategoryResponseDto;
import com.kfpcl.dto.SubcategoryUpdateDto;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Subcategory;
import com.kfpcl.exception.*;
import com.kfpcl.repository.*;
import com.kfpcl.service.SubcategoryService;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubcategoryServiceImpl implements SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ReviewRepository reviewRepository;
    private final ImageUtils imageUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SubcategoryResponseDto> getAllSubcategories(String categoryId, String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String resolvedCatId = null;
        if (StringUtils.hasText(categoryId)) {
            String trimmed = categoryId.trim();
            resolvedCatId = categoryRepository.findById(trimmed)
                    .or(() -> categoryRepository.findByNameIgnoreCase(trimmed))
                    .map(Category::getId)
                    .orElse(trimmed);
        }

        Page<Subcategory> subcategoryPage;

        if (StringUtils.hasText(resolvedCatId) && StringUtils.hasText(status)) {
            Subcategory.Status subStatus = parseStatus(status);
            subcategoryPage = subcategoryRepository.findByCategoryIdAndStatus(resolvedCatId, subStatus, pageable);
        } else if (StringUtils.hasText(resolvedCatId)) {
            subcategoryPage = subcategoryRepository.findByCategoryIdAndStatusNot(resolvedCatId, Subcategory.Status.ARCHIVED, pageable);
        } else if (StringUtils.hasText(status)) {
            Subcategory.Status subStatus = parseStatus(status);
            subcategoryPage = subcategoryRepository.findByStatus(subStatus, pageable);
        } else {
            subcategoryPage = subcategoryRepository.findByStatusNot(Subcategory.Status.ARCHIVED, pageable);
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        List<SubcategoryResponseDto> dtoList = subcategoryPage.getContent().stream()
                .map(sub -> mapToDto(sub, categoryNames.get(sub.getCategoryId())))
                .collect(Collectors.toList());

        return PageResponseDto.from(subcategoryPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubcategoryResponseDto> getSubcategoriesByCategoryId(String categoryId) {
        String catId = categoryId != null ? categoryId.trim() : "";
        Category category = categoryRepository.findById(catId)
                .or(() -> categoryRepository.findByNameIgnoreCase(catId))
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        String resolvedCatId = category.getId();
        List<Subcategory> subcategories = subcategoryRepository.findByCategoryIdAndStatus(resolvedCatId, Subcategory.Status.ACTIVE);
        if (subcategories.isEmpty()) {
            subcategories = subcategoryRepository.findByCategoryId(resolvedCatId).stream()
                    .filter(s -> s.getStatus() != Subcategory.Status.ARCHIVED)
                    .collect(Collectors.toList());
        }

        return subcategories.stream()
                .map(sub -> mapToDto(sub, category.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubcategoryResponseDto getSubcategoryById(String subcategoryId) {
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", subcategoryId));

        String categoryName = categoryRepository.findById(subcategory.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");

        return mapToDto(subcategory, categoryName);
    }

    @Override
    public SubcategoryResponseDto createSubcategory(SubcategoryCreateDto dto) {
        String requestedCat = dto.getCategoryId().trim();
        Category category = categoryRepository.findById(requestedCat)
                .or(() -> categoryRepository.findByNameIgnoreCase(requestedCat))
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", dto.getCategoryId()));

        String categoryId = category.getId();

        if (!category.isActive()) {
            throw new BusinessValidationException("Cannot create subcategory under an inactive or archived category: " + category.getName());
        }

        if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCase(categoryId, dto.getName().trim())) {
            throw new DuplicateResourceException(String.format("Subcategory '%s' already exists in category '%s'", dto.getName(), category.getName()));
        }

        String subcategoryId = StringUtils.hasText(dto.getId())
                ? dto.getId().trim()
                : "sub_" + slugify(dto.getName());

        if (subcategoryRepository.existsById(subcategoryId)) {
            subcategoryId = subcategoryId + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        Subcategory.Status status = Subcategory.Status.ACTIVE;
        if (dto.getIsActive() != null && !dto.getIsActive()) {
            status = Subcategory.Status.INACTIVE;
        }
        if (StringUtils.hasText(dto.getStatus())) {
            status = parseStatus(dto.getStatus());
        }

        Subcategory subcategory = Subcategory.builder()
                .id(subcategoryId)
                .categoryId(categoryId)
                .name(dto.getName().trim())
                .imageUrl(imageUtils.processBase64Image(dto.getImageUrl()))
                .description(dto.getDescription())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1)
                .discount(dto.getDiscount() != null ? dto.getDiscount() : 0.0)
                .status(status)
                .build();

        Subcategory saved = subcategoryRepository.save(subcategory);
        return mapToDto(saved, category.getName());
    }

    @Override
    public SubcategoryResponseDto updateSubcategory(String subcategoryId, SubcategoryUpdateDto dto) {
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", subcategoryId));

        String categoryId = StringUtils.hasText(dto.getCategoryId()) ? dto.getCategoryId().trim() : subcategory.getCategoryId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        if (StringUtils.hasText(dto.getName())) {
            if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCaseAndIdNot(categoryId, dto.getName().trim(), subcategoryId)) {
                throw new DuplicateResourceException(String.format("Subcategory '%s' already exists in category '%s'", dto.getName(), category.getName()));
            }
            subcategory.setName(dto.getName().trim());
        }

        subcategory.setCategoryId(categoryId);

        if (dto.getImageUrl() != null) {
            subcategory.setImageUrl(imageUtils.processBase64Image(dto.getImageUrl()));
        }
        if (dto.getDescription() != null) {
            subcategory.setDescription(dto.getDescription());
        }
        if (dto.getDisplayOrder() != null) {
            subcategory.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getDiscount() != null) {
            subcategory.setDiscount(dto.getDiscount());
        }
        if (dto.getIsActive() != null) {
            subcategory.setStatus(dto.getIsActive() ? Subcategory.Status.ACTIVE : Subcategory.Status.INACTIVE);
        }
        if (StringUtils.hasText(dto.getStatus())) {
            subcategory.setStatus(parseStatus(dto.getStatus()));
        }

        Subcategory updated = subcategoryRepository.save(subcategory);
        return mapToDto(updated, category.getName());
    }

    @Override
    public void deleteSubcategory(String subcategoryId) {
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategoryId", subcategoryId));

        // 1. Find and physically delete all products linked to this subcategory
        List<Product> products = productRepository.findBySubcategoryId(subcategoryId);
        for (Product product : products) {
            inventoryRepository.findByProductId(product.getId()).ifPresent(inv -> {
                inventoryLogRepository.deleteByInventoryId(inv.getId());
                inventoryRepository.delete(inv);
            });
            inventoryLogRepository.deleteByProductId(product.getId());
            reviewRepository.deleteByProductId(product.getId());
            productRepository.delete(product);
        }

        // 2. Physically delete the subcategory row from database
        subcategoryRepository.delete(subcategory);
    }

    private Subcategory.Status parseStatus(String statusStr) {
        try {
            return Subcategory.Status.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Subcategory.Status.ACTIVE;
        }
    }

    private String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private SubcategoryResponseDto mapToDto(Subcategory subcategory, String categoryName) {
        return SubcategoryResponseDto.builder()
                .id(subcategory.getId())
                .categoryId(subcategory.getCategoryId())
                .categoryName(categoryName)
                .name(subcategory.getName())
                .imageUrl(subcategory.getImageUrl())
                .description(subcategory.getDescription())
                .displayOrder(subcategory.getDisplayOrder())
                .discount(subcategory.getDiscount())
                .status(subcategory.getStatus() != null ? subcategory.getStatus().name() : Subcategory.Status.ACTIVE.name())
                .isActive(subcategory.isActive())
                .createdAt(subcategory.getCreatedAt())
                .updatedAt(subcategory.getUpdatedAt())
                .build();
    }
}
