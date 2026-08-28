package com.kfpcl.service;

import com.kfpcl.dto.ProductCreateDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.entity.Category;
import com.kfpcl.entity.Inventory;
import com.kfpcl.entity.InventoryLog;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Subcategory;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.repository.*;
import com.kfpcl.serviceImpl.ProductServiceImpl;
import com.kfpcl.util.ImageUtils;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SubcategoryRepository subcategoryRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryLogRepository inventoryLogRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ImageUtils imageUtils;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Subcategory subcategory;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("cat_dairy")
                .name("Dairy, Bread & Eggs")
                .status(Category.Status.ACTIVE)
                .build();

        subcategory = Subcategory.builder()
                .id("sub_milk")
                .categoryId("cat_dairy")
                .name("Milk")
                .status(Subcategory.Status.ACTIVE)
                .build();

        product = Product.builder()
                .id("prod_amul_milk")
                .productName("Amul Taaza Milk")
                .categoryId("cat_dairy")
                .subcategoryId("sub_milk")
                .brand("Amul")
                .price(30.0)
                .mrp(32.0)
                .quantity(500.0)
                .unit("ml")
                .stockQuantity(50)
                .sku("AML-MILK-500")
                .discount(6.25)
                .status(Product.Status.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Create Product - Success with Server-side Discount & Inventory Initialization")
    void testCreateProduct_Success() {
        ProductCreateDto dto = ProductCreateDto.builder()
                .productName("Amul Taaza Milk")
                .categoryId("cat_dairy")
                .subcategoryId("sub_milk")
                .brand("Amul")
                .description("Fresh toned milk")
                .price(30.0)
                .mrp(32.0)
                .quantity(500.0)
                .unit("ml")
                .stockQuantity(50)
                .sku("AML-MILK-500")
                .status("ACTIVE")
                .build();

        when(categoryRepository.findById("cat_dairy")).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById("sub_milk")).thenReturn(Optional.of(subcategory));
        when(productRepository.existsBySku("AML-MILK-500")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDto response = productService.createProduct(dto);

        assertNotNull(response);
        assertEquals("Amul Taaza Milk", response.getProductName());
        assertEquals("AML-MILK-500", response.getSku());
        assertEquals(30.0, response.getPrice());
        assertEquals(32.0, response.getMrp());
        assertEquals(6.25, response.getDiscount()); // ((32-30)/32)*100

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(inventoryLogRepository, times(1)).save(any(InventoryLog.class));
    }

    @Test
    @DisplayName("Create Product - Price > MRP Throws BusinessValidationException")
    void testCreateProduct_PriceGreaterThanMrp() {
        ProductCreateDto dto = ProductCreateDto.builder()
                .productName("Amul Taaza Milk")
                .categoryId("cat_dairy")
                .subcategoryId("sub_milk")
                .price(40.0) // Invalid: price > mrp
                .mrp(32.0)
                .sku("AML-MILK-500")
                .build();

        when(categoryRepository.findById("cat_dairy")).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById("sub_milk")).thenReturn(Optional.of(subcategory));
        when(productRepository.existsBySku("AML-MILK-500")).thenReturn(false);

        assertThrows(BusinessValidationException.class, () -> productService.createProduct(dto));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Duplicate SKU Throws DuplicateResourceException")
    void testCreateProduct_DuplicateSku() {
        ProductCreateDto dto = ProductCreateDto.builder()
                .productName("Amul Taaza Milk")
                .categoryId("cat_dairy")
                .subcategoryId("sub_milk")
                .price(30.0)
                .mrp(32.0)
                .sku("AML-MILK-500")
                .build();

        when(categoryRepository.findById("cat_dairy")).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById("sub_milk")).thenReturn(Optional.of(subcategory));
        when(productRepository.existsBySku("AML-MILK-500")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(dto));
    }

    @Test
    @DisplayName("Create Product - Subcategory Mismatch with Category Throws BusinessValidationException")
    void testCreateProduct_SubcategoryMismatch() {
        Subcategory otherSub = Subcategory.builder()
                .id("sub_bread")
                .categoryId("cat_bakery") // different category
                .name("Bread")
                .status(Subcategory.Status.ACTIVE)
                .build();

        ProductCreateDto dto = ProductCreateDto.builder()
                .productName("Amul Taaza Milk")
                .categoryId("cat_dairy")
                .subcategoryId("sub_bread")
                .price(30.0)
                .mrp(32.0)
                .sku("AML-MILK-500")
                .build();

        when(categoryRepository.findById("cat_dairy")).thenReturn(Optional.of(category));
        when(subcategoryRepository.findById("sub_bread")).thenReturn(Optional.of(otherSub));

        assertThrows(BusinessValidationException.class, () -> productService.createProduct(dto));
    }

    @Test
    @DisplayName("Delete Product - Physically Deletes Product Row and Associated Inventory")
    void testDeleteProduct_PhysicallyRemovesRow() {
        Inventory inventory = Inventory.builder()
                .id("inv_123")
                .productId("prod_amul_milk")
                .sku("AML-MILK-500")
                .build();

        when(productRepository.findById("prod_amul_milk")).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId("prod_amul_milk")).thenReturn(Optional.of(inventory));

        productService.deleteProduct("prod_amul_milk");

        // Verifies real delete method is called on repository, NOT a status update save
        verify(inventoryLogRepository, times(1)).deleteByInventoryId("inv_123");
        verify(inventoryRepository, times(1)).delete(inventory);
        verify(reviewRepository, times(1)).deleteByProductId("prod_amul_milk");
        verify(productRepository, times(1)).delete(product);
        verify(productRepository, never()).save(any(Product.class));
    }
}
