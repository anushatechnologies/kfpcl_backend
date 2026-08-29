package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.*;
import com.kfpcl.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Test
    @DisplayName("Catalog Image Upload - Success without token (201)")
    void testUploadCatalogImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "milk.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/catalog/images")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.fileUrl").exists());
    }



    @Test
    @DisplayName("Full Catalog & Product Workflow without any tokens (Positive & Negative Cases)")
    void testFullCatalogFlow() throws Exception {
        // 1. Create Category (201 Created without token)
        CategoryCreateDto categoryDto = CategoryCreateDto.builder()
                .id("cat_dairy_test")
                .name("Dairy and Eggs Test")
                .imageUrl("https://cdn/dairy.jpg")
                .description("Fresh dairy products")
                .displayOrder(1)
                .discount(5.0)
                .isActive(true)
                .build();

        String categoryResponse = mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        String generatedCategoryId = objectMapper.readTree(categoryResponse).get("data").get("id").asText();

        // 2. Duplicate Category Name -> 409 Conflict (without token)
        mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Create Subcategory (201 Created without token)
        SubcategoryCreateDto subcategoryDto = SubcategoryCreateDto.builder()
                .id("sub_milk_test")
                .categoryId(generatedCategoryId)
                .name("Organic Milk Test")
                .imageUrl("https://cdn/milk.jpg")
                .displayOrder(1)
                .isActive(true)
                .build();

        String subcategoryResponse = mockMvc.perform(post("/api/v1/admin/catalog/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subcategoryDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String generatedSubcategoryId = objectMapper.readTree(subcategoryResponse).get("data").get("id").asText();

        // 4. Create Product (201 Created without token) with server-side discount
        ProductCreateDto productDto = ProductCreateDto.builder()
                .sku("KFP-MILK-1000-TEST")
                .productName("Farm Fresh Organic Milk 1L")
                .categoryId(generatedCategoryId)
                .subcategoryId(generatedSubcategoryId)
                .brand("KFPCL Organic")
                .description("Pure fresh cow milk")
                .price(40.0)
                .mrp(50.0)
                .quantity(1000.0)
                .unit("ml")
                .stockQuantity(100)
                .sku("KFP-MILK-1000-TEST")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/v1/admin/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value("KFP-MILK-1000-TEST"))
                .andExpect(jsonPath("$.data.discount").value(20.0)); // ((50-40)/50)*100 = 20%

        // 5. Invalid Price (price > MRP) -> 422 Unprocessable Entity
        ProductCreateDto invalidPriceProduct = ProductCreateDto.builder()
                .productName("Invalid Price Milk")
                .categoryId(generatedCategoryId)
                .subcategoryId(generatedSubcategoryId)
                .price(60.0) // Invalid
                .mrp(50.0)
                .sku("KFP-INVALID-PRICE")
                .build();

        mockMvc.perform(post("/api/v1/admin/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPriceProduct)))
                .andExpect(status().isUnprocessableEntity());

        // 6. Public Buyer Product Listing (200 OK without token)
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "Organic")
                        .param("categoryId", generatedCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 7. Get Admin Inventory List (200 OK without token)
        mockMvc.perform(get("/api/v1/admin/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. Physically Delete Product (200 OK) -> Verifies physical removal from DB
        com.kfpcl.entity.Product createdProduct = productRepository.findBySku("KFP-MILK-1000-TEST").orElseThrow();
        String createdProdId = createdProduct.getId();

        mockMvc.perform(delete("/api/v1/admin/catalog/products/" + createdProdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(productRepository.existsById(createdProdId), "Product must be physically deleted from database");
        assertFalse(inventoryRepository.findByProductId(createdProdId).isPresent(), "Inventory must be physically deleted from database");

        // Verify GET by ID returns 404
        mockMvc.perform(get("/api/v1/catalog/products/" + createdProdId))
                .andExpect(status().isNotFound());

        // 9. Physically Delete Category (200 OK) -> Verifies cascading physical deletion of category and subcategory
        mockMvc.perform(delete("/api/v1/admin/catalog/categories/" + generatedCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(categoryRepository.existsById(generatedCategoryId), "Category must be physically deleted from database");
        assertFalse(subcategoryRepository.existsById(generatedSubcategoryId), "Subcategory must be physically deleted from database");

        // Verify GET Category by ID returns 404
        mockMvc.perform(get("/api/v1/catalog/categories/" + generatedCategoryId))
                .andExpect(status().isNotFound());
    }
}
