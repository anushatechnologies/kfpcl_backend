package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.CreateRfqRequest;
import com.kfpcl.dto.RfqResponse;
import com.kfpcl.service.RfqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuyerRfqController.class)
class BuyerRfqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RfqService rfqService;

    @Test
    @DisplayName("POST /api/v1/buyer/rfqs - 201 Created")
    void testCreateRfq_Success() throws Exception {
        CreateRfqRequest request = CreateRfqRequest.builder()
                .productId("prod_1")
                .productTitle("Amul Milk")
                .categoryId("cat_dairy")
                .quantity(100)
                .unit("piece")
                .targetPrice(BigDecimal.valueOf(28.00))
                .expectedDeliveryDate(LocalDate.now().plusDays(10))
                .description("Bulk supply")
                .build();

        RfqResponse response = RfqResponse.builder()
                .id("rfq_1")
                .productId("prod_1")
                .productTitle("Amul Milk")
                .quantity(100)
                .unit("piece")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(rfqService.createRfq(any(CreateRfqRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/buyer/rfqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rfq_1"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/rfqs - 200 OK")
    void testGetBuyerRfqs_Success() throws Exception {
        RfqResponse rfq = RfqResponse.builder()
                .id("rfq_1")
                .productTitle("Amul Milk")
                .status("OPEN")
                .build();

        Mockito.when(rfqService.getBuyerRfqs()).thenReturn(List.of(rfq));

        mockMvc.perform(get("/api/v1/buyer/rfqs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("rfq_1"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/rfqs/{rfqId} - 200 OK")
    void testGetRfqById_Success() throws Exception {
        RfqResponse rfq = RfqResponse.builder()
                .id("rfq_1")
                .productTitle("Amul Milk")
                .status("OPEN")
                .build();

        Mockito.when(rfqService.getBuyerRfqById("rfq_1")).thenReturn(rfq);

        mockMvc.perform(get("/api/v1/buyer/rfqs/rfq_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("rfq_1"));
    }

    @Test
    @DisplayName("PATCH /api/v1/buyer/rfqs/{rfqId}/cancel - 200 OK")
    void testCancelRfq_Success() throws Exception {
        RfqResponse rfq = RfqResponse.builder()
                .id("rfq_1")
                .status("CANCELLED")
                .build();

        Mockito.when(rfqService.cancelRfq("rfq_1")).thenReturn(rfq);

        mockMvc.perform(patch("/api/v1/buyer/rfqs/rfq_1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
