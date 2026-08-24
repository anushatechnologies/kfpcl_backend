package com.kfpcl.service;

import com.kfpcl.dto.CategoryCreateDto;
import com.kfpcl.dto.CategoryResponseDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("cat_dairy")
                .name("Dairy, Bread & Eggs")
                .imageUrl("https://cdn/dairy.jpg")
                .description("Fresh dairy products")
                .displayOrder(1)
                .discount(5.0)
                .status(Category.Status.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Create Category - Success")
    void testCreateCategory_Success() {
        CategoryCreateDto dto = CategoryCreateDto.builder()
                .name("Dairy, Bread & Eggs")
                .imageUrl("https://cdn/dairy.jpg")
                .description("Fresh dairy products")
                .displayOrder(1)
                .discount(5.0)
                .isActive(true)
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Dairy, Bread & Eggs")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponseDto response = categoryService.createCategory(dto);

        assertNotNull(response);
        assertEquals("Dairy, Bread & Eggs", response.getName());
        assertEquals("cat_dairy", response.getId());
        assertTrue(response.getIsActive());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Create Category - Duplicate Name Throws 409 Conflict")
    void testCreateCategory_DuplicateName() {
        CategoryCreateDto dto = CategoryCreateDto.builder()
                .name("Dairy, Bread & Eggs")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Dairy, Bread & Eggs")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(dto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Get Category - Not Found Throws 404")
    void testGetCategory_NotFound() {
        when(categoryRepository.findById("invalid_id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById("invalid_id"));
    }
}
