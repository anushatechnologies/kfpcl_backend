package com.kfpcl.service;

import com.kfpcl.dto.request.CategoryRequest;
import com.kfpcl.dto.response.CategoryResponse;
import com.kfpcl.entity.Category;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.serviceImpl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Grains & Pulses")
                .slug("grains-pulses")
                .description("Wholesale grains")
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Grains & Pulses")
                .slug("grains-pulses")
                .description("Wholesale grains")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Grains & Pulses")).thenReturn(false);
        when(categoryRepository.existsBySlug("grains-pulses")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Grains & Pulses");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when category name exists")
    void createCategory_DuplicateName() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Grains & Pulses")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Grains & Pulses")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve active categories successfully")
    void getActiveCategories_Success() {
        when(categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(category));

        List<CategoryResponse> list = categoryService.getActiveCategories();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Grains & Pulses");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found by id")
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
