package com.kfpcl.service;

import com.kfpcl.dto.CategoryResponse;
import com.kfpcl.dto.PageResponse;
import com.kfpcl.dto.ProductDetailResponse;
import com.kfpcl.dto.ProductResponse;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Supplier;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.CategoryRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.serviceImpl.CatalogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Category category;
    private Supplier supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("cat_dairy", "Dairy", Category.Status.ACTIVE);
        supplier = Supplier.builder()
                .id("supp_1")
                .companyName("Amul India")
                .gstVerified(true)
                .isVerified(true)
                .build();

        product = Product.builder()
                .id("prod_1")
                .title("Amul Taaza Milk")
                .description("Fresh toned milk")
                .category(category)
                .supplier(supplier)
                .price(BigDecimal.valueOf(28.00))
                .unit("piece")
                .moq(10)
                .stockQuantity(100)
                .featured(true)
                .status(Product.Status.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("getCategories(true) - Returns active categories")
    void testGetCategories_ActiveOnly() {
        when(categoryRepository.findByStatus(Category.Status.ACTIVE)).thenReturn(List.of(category));

        List<CategoryResponse> result = catalogService.getCategories(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cat_dairy", result.get(0).getId());
        assertEquals("ACTIVE", result.get(0).getStatus());
        verify(categoryRepository, times(1)).findByStatus(Category.Status.ACTIVE);
    }

    @Test
    @DisplayName("getFeaturedProducts() - Returns featured active products")
    void testGetFeaturedProducts() {
        when(productRepository.findByFeaturedTrueAndStatus(Product.Status.ACTIVE)).thenReturn(List.of(product));

        List<ProductResponse> result = catalogService.getFeaturedProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("prod_1", result.get(0).getId());
        assertTrue(result.get(0).getFeatured());
    }

    @Test
    @DisplayName("getProducts() - Dynamic Specification Pagination")
    void testGetProducts_Success() {
        PageImpl<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProductResponse> result = catalogService.getProducts(
                "milk", "cat_dairy", BigDecimal.valueOf(20), BigDecimal.valueOf(50), 20, true, true, "price_low", 1, 20, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("prod_1", result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("getProductById() - Success")
    void testGetProductById_Success() {
        when(productRepository.findById("prod_1")).thenReturn(Optional.of(product));

        ProductDetailResponse result = catalogService.getProductById("prod_1");

        assertNotNull(result);
        assertEquals("prod_1", result.getId());
        assertEquals("Amul Taaza Milk", result.getTitle());
        assertEquals("Amul India", result.getSupplier().getCompanyName());
    }

    @Test
    @DisplayName("getProductById() - Throws ResourceNotFoundException for invalid ID")
    void testGetProductById_NotFound() {
        when(productRepository.findById("prod_unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> catalogService.getProductById("prod_unknown"));
    }
}
