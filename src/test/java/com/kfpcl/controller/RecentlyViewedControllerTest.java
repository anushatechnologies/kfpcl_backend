package com.kfpcl.controller;

import com.kfpcl.service.RecentlyViewedService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecentlyViewedController.class)
class RecentlyViewedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecentlyViewedService recentlyViewedService;

    @Test
    @DisplayName("POST /api/v1/buyer/products/{productId}/view - 200 OK")
    void testRecordProductView_Success() throws Exception {
        Mockito.doNothing().when(recentlyViewedService).recordProductView("prod_1");

        mockMvc.perform(post("/api/v1/buyer/products/prod_1/view")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
