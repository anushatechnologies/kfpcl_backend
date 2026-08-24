package com.kfpcl.controller;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.OrderTrackingResponse;
import com.kfpcl.service.OrderService;
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

@WebMvcTest(controllers = BuyerOrderController.class)
class BuyerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("GET /api/v1/buyer/orders - 200 OK")
    void testGetOrders_Success() throws Exception {
        BuyerOrderResponse order = BuyerOrderResponse.builder()
                .id("ord_1")
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(13000.00))
                .build();

        Mockito.when(orderService.getBuyerOrders("ALL")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/buyer/orders?status=ALL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("ord_1"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/orders/{orderId} - 200 OK")
    void testGetOrderById_Success() throws Exception {
        BuyerOrderResponse order = BuyerOrderResponse.builder()
                .id("ord_1")
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(13000.00))
                .build();

        Mockito.when(orderService.getBuyerOrderById("ord_1")).thenReturn(order);

        mockMvc.perform(get("/api/v1/buyer/orders/ord_1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ord_1"));
    }

    @Test
    @DisplayName("POST /api/v1/buyer/orders/{orderId}/cancel - 200 OK")
    void testCancelOrder_Success() throws Exception {
        BuyerOrderResponse order = BuyerOrderResponse.builder()
                .id("ord_1")
                .status("CANCELLED")
                .build();

        Mockito.when(orderService.cancelOrder("ord_1")).thenReturn(order);

        mockMvc.perform(post("/api/v1/buyer/orders/ord_1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/v1/buyer/orders/{orderId}/confirm-delivery - 200 OK")
    void testConfirmDelivery_Success() throws Exception {
        BuyerOrderResponse order = BuyerOrderResponse.builder()
                .id("ord_1")
                .status("DELIVERED")
                .build();

        Mockito.when(orderService.confirmDelivery("ord_1")).thenReturn(order);

        mockMvc.perform(post("/api/v1/buyer/orders/ord_1/confirm-delivery")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/orders/{orderId}/tracking - 200 OK")
    void testGetOrderTracking_Success() throws Exception {
        OrderTrackingResponse tracking = OrderTrackingResponse.builder()
                .orderId("ord_1")
                .status("SHIPPED")
                .trackingNumber("TRK123456")
                .courierPartner("BlueDart Logistics")
                .build();

        Mockito.when(orderService.getOrderTracking("ord_1")).thenReturn(tracking);

        mockMvc.perform(get("/api/v1/buyer/orders/ord_1/tracking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("ord_1"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK123456"));
    }
}
