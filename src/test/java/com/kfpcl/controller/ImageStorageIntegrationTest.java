package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.*;
import com.kfpcl.entity.Category;
import com.kfpcl.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImageStorageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private software.amazon.awssdk.services.s3.S3Client s3Client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Upload Image -> Get S3 Key -> Create Category/Product -> Verify DB key storage")
    void testImageStorageFlow() throws Exception {
        // 1. Upload catalog image
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "milk.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some-content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/admin/catalog/images")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageKey").exists())
                .andExpect(jsonPath("$.data.fileUrl").exists())
                .andReturn();

        String uploadResponseStr = uploadResult.getResponse().getContentAsString();
        String imageKey = objectMapper.readTree(uploadResponseStr).get("data").get("imageKey").asText();
        String fileUrl = objectMapper.readTree(uploadResponseStr).get("data").get("fileUrl").asText();

        assertNotNull(imageKey);
        assertEquals(imageKey, fileUrl);

        // 2. Create Category using the S3 Key
        CategoryCreateDto categoryDto = CategoryCreateDto.builder()
                .id("cat_milk_storage_test")
                .name("Dairy Key Storage Test")
                .imageUrl(imageKey)
                .description("Testing image storage keys")
                .isActive(true)
                .build();

        MvcResult catCreateResult = mockMvc.perform(post("/api/v1/admin/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").value(imageKey))
                .andReturn();

        // 3. Verify key is saved in Category table in DB
        Category category = categoryRepository.findById("cat_milk_storage_test").orElseThrow();
        assertEquals(imageKey, category.getImageUrl());

        // 4. Verify GET Category returns matching key
        mockMvc.perform(get("/api/v1/catalog/categories/cat_milk_storage_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value(imageKey));
    }
}
