package com.kfpcl.controller;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.QuotationCompareResponse;
import com.kfpcl.dto.QuotationResponse;
import com.kfpcl.service.QuotationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuyerQuotationController.class)
class BuyerQuotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuotationService quotationService;

    @Test
    @DisplayName("GET /api/v1/buyer/rfqs/{rfqId}/quotations - 200 OK")
    void testGetQuotations_Success() throws Exception {
        QuotationResponse q = QuotationResponse.builder()
                .id("quot_1")
                .rfqId("rfq_1")
                .quotedPrice(BigDecimal.valueOf(26.00))
                .status("PENDING")
                .build();

        Mockito.when(quotationService.getQuotationsForRfq("rfq_1")).thenReturn(List.of(q));

        mockMvc.perform(get("/api/v1/buyer/rfqs/rfq_1/quotations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("quot_1"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/rfqs/{rfqId}/quotations/compare - 200 OK")
    void testCompareQuotations_Success() throws Exception {
        QuotationCompareResponse compare = QuotationCompareResponse.builder()
                .rfqId("rfq_1")
                .productTitle("Amul Milk")
                .lowestQuotedPrice(BigDecimal.valueOf(26.00))
                .build();

        Mockito.when(quotationService.compareQuotations("rfq_1")).thenReturn(compare);

        mockMvc.perform(get("/api/v1/buyer/rfqs/rfq_1/quotations/compare")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rfqId").value("rfq_1"))
                .andExpect(jsonPath("$.data.lowestQuotedPrice").value(26.00));
    }

    @Test
    @DisplayName("POST /api/v1/buyer/rfqs/{rfqId}/quotations/{quotationId}/accept - 200 OK")
    void testAcceptQuotation_Success() throws Exception {
        BuyerOrderResponse order = BuyerOrderResponse.builder()
                .id("ord_1")
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(13000.00))
                .build();

        Mockito.when(quotationService.acceptQuotation("rfq_1", "quot_1")).thenReturn(order);

        mockMvc.perform(post("/api/v1/buyer/rfqs/rfq_1/quotations/quot_1/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ord_1"))
                .andExpect(jsonPath("$.data.status").value("PLACED"));
    }

    @Test
    @DisplayName("POST /api/v1/buyer/rfqs/{rfqId}/quotations/{quotationId}/reject - 200 OK")
    void testRejectQuotation_Success() throws Exception {
        QuotationResponse q = QuotationResponse.builder()
                .id("quot_1")
                .status("REJECTED")
                .build();

        Mockito.when(quotationService.rejectQuotation("rfq_1", "quot_1")).thenReturn(q);

        mockMvc.perform(post("/api/v1/buyer/rfqs/rfq_1/quotations/quot_1/reject")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
