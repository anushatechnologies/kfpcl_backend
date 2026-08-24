package com.kfpcl.controller;

import com.kfpcl.dto.CategoryResponse;
import com.kfpcl.dto.PageResponse;
import com.kfpcl.dto.ProductDetailResponse;
import com.kfpcl.dto.ProductResponse;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.service.CatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @Test
    @DisplayName("GET /api/v1/catalog/categories - Success")
    void testGetCategories_Success() throws Exception {
        CategoryResponse cat1 = CategoryResponse.builder().id("cat_dairy").name("Dairy").status("ACTIVE").build();
        Mockito.when(catalogService.getCategories(true)).thenReturn(List.of(cat1));

        mockMvc.perform(get("/api/v1/catalog/categories?isActive=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("cat_dairy"))
                .andExpect(jsonPath("$.data[0].name").value("Dairy"));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/products?featured=true - Success")
    void testGetFeaturedProducts_Success() throws Exception {
        ProductResponse prod = ProductResponse.builder()
                .id("prod_1")
                .title("Amul Milk")
                .price(BigDecimal.valueOf(30.00))
                .featured(true)
                .build();
        Mockito.when(catalogService.getFeaturedProducts()).thenReturn(List.of(prod));

        mockMvc.perform(get("/api/v1/catalog/products?featured=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("prod_1"))
                .andExpect(jsonPath("$.data[0].featured").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/products - Filter & Pagination Success")
    void testGetProducts_Filtered_Success() throws Exception {
        ProductResponse prod = ProductResponse.builder()
                .id("prod_1")
                .title("Amul Milk")
                .price(BigDecimal.valueOf(30.00))
                .moq(10)
                .build();

        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(List.of(prod))
                .page(1)
                .limit(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        Mockito.when(catalogService.getProducts(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/catalog/products?search=milk&minPrice=20&maxPrice=50&page=1&limit=20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("prod_1"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/products/{productId} - Success")
    void testGetProductById_Success() throws Exception {
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id("prod_1")
                .title("Amul Milk")
                .price(BigDecimal.valueOf(30.00))
                .stockQuantity(100)
                .build();

        Mockito.when(catalogService.getProductById("prod_1")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/catalog/products/prod_1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("prod_1"))
                .andExpect(jsonPath("$.data.stockQuantity").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/products/{productId} - Not Found 404")
    void testGetProductById_NotFound() throws Exception {
        Mockito.when(catalogService.getProductById("prod_999"))
                .thenThrow(new ResourceNotFoundException("Product", "id", "prod_999"));

        mockMvc.perform(get("/api/v1/catalog/products/prod_999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
