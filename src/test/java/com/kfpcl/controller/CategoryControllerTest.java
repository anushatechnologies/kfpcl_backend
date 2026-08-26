package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.request.CategoryRequest;
import com.kfpcl.dto.response.CategoryResponse;
import com.kfpcl.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /api/v1/categories - Should return active categories list")
    void getActiveCategories_ShouldReturn200() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Grains & Pulses")
                .slug("grains-pulses")
                .isActive(true)
                .build();

        when(categoryService.getActiveCategories()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Grains & Pulses"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should create category successfully")
    void createCategory_ShouldReturn201() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Spices & Condiments")
                .slug("spices-condiments")
                .description("Dry spices")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(2L)
                .name("Spices & Condiments")
                .slug("spices-condiments")
                .isActive(true)
                .build();

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Spices & Condiments"));
    }
}
