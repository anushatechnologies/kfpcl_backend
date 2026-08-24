package com.kfpcl.controller;

import com.kfpcl.dto.ProductResponse;
import com.kfpcl.service.RecommendationService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuyerRecommendationController.class)
class BuyerRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    @DisplayName("GET /api/v1/buyer/recommendations - 200 OK")
    void testGetRecommendations_Success() throws Exception {
        ProductResponse p = ProductResponse.builder()
                .id("prod_1")
                .title("Amul Milk")
                .price(BigDecimal.valueOf(28.00))
                .build();

        Mockito.when(recommendationService.getBuyerRecommendations()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/buyer/recommendations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("prod_1"));
    }
}
