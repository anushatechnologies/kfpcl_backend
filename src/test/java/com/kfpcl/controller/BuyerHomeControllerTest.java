package com.kfpcl.controller;

import com.kfpcl.dto.BuyerHomeResponse;
import com.kfpcl.dto.BuyerSummaryResponse;
import com.kfpcl.service.BuyerHomeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuyerHomeController.class)
class BuyerHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuyerHomeService buyerHomeService;

    @Test
    @DisplayName("GET /api/v1/buyer/home - 200 OK")
    void testGetBuyerHome_Success() throws Exception {
        BuyerHomeResponse homeResponse = BuyerHomeResponse.builder()
                .buyer(BuyerSummaryResponse.builder().buyerId("buyer_1").companyName("KFPCL Supermarkets").build())
                .wishlistCount(3)
                .activeRfqsCount(2)
                .totalOrdersCount(5)
                .build();

        Mockito.when(buyerHomeService.getBuyerHomeDashboard()).thenReturn(homeResponse);

        mockMvc.perform(get("/api/v1/buyer/home")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.buyer.buyerId").value("buyer_1"))
                .andExpect(jsonPath("$.data.wishlistCount").value(3))
                .andExpect(jsonPath("$.data.activeRfqsCount").value(2));
    }
}
