package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Explicit Invalid Token Returns 401 Unauthorized")
    void testAdminEndpoint_InvalidToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inventory")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Explicit Unauthorized Role Token Returns 403 Forbidden")
    void testAdminEndpoint_UnauthorizedRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inventory")
                        .header("Authorization", "Bearer unauthorized_role"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
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

        mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("cat_dairy_test"));

        // 2. Duplicate Category Name -> 409 Conflict (without token)
        mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Create Subcategory (201 Created without token)
        SubcategoryCreateDto subcategoryDto = SubcategoryCreateDto.builder()
                .id("sub_milk_test")
                .categoryId("cat_dairy_test")
                .name("Organic Milk Test")
                .imageUrl("https://cdn/milk.jpg")
                .displayOrder(1)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/catalog/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subcategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("sub_milk_test"));

        // 4. Create Product (201 Created without token) with server-side discount
        ProductCreateDto productDto = ProductCreateDto.builder()
                .productName("Organic Fresh Milk Test")
                .categoryId("cat_dairy_test")
                .subcategoryId("sub_milk_test")
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
                .categoryId("cat_dairy_test")
                .subcategoryId("sub_milk_test")
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
                        .param("categoryId", "cat_dairy_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 7. Get Admin Inventory List (200 OK without token)
        mockMvc.perform(get("/api/v1/admin/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
