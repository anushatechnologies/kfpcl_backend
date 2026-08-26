package com.kfpcl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.OrderCreateDto;
import com.kfpcl.dto.OrderItemDto;
import com.kfpcl.dto.OrderStatusUpdateDto;
import com.kfpcl.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateOrderAndTaxCalculation() throws Exception {
        OrderItemDto item = OrderItemDto.builder()
                .productId("prod_1")
                .productName("Organic Wheat Bulk")
                .quantity(100)
                .unitPrice(new BigDecimal("340.00"))
                .build();

        OrderCreateDto dto = OrderCreateDto.builder()
                .items(Collections.singletonList(item))
                .shippingAddress("Plot 42, MIDC Industrial Area, Pune, Maharashtra")
                .paymentMethod("bank")
                .shippingCost(new BigDecimal("150.00"))
                .sellerId("seller_123")
                .build();

        mockMvc.perform(post("/api/v1/buyer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderNumber", startsWith("ORD-")))
                .andExpect(jsonPath("$.subtotal", is(34000.0)))
                .andExpect(jsonPath("$.tax", is(6120.0)))
                .andExpect(jsonPath("$.shippingCost", is(150.0)))
                .andExpect(jsonPath("$.grandTotal", is(40270.0)))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    public void testIdempotencyKeyEnforcement() throws Exception {
        String idempotencyKey = "IDEM-" + UUID.randomUUID().toString();

        OrderItemDto item = OrderItemDto.builder()
                .productId("prod_2")
                .productName("Fertilizer Grade A")
                .quantity(10)
                .unitPrice(new BigDecimal("500.00"))
                .build();

        OrderCreateDto dto = OrderCreateDto.builder()
                .items(Collections.singletonList(item))
                .shippingAddress("Delhi Warehousing Hub")
                .paymentMethod("bank")
                .shippingCost(new BigDecimal("100.00"))
                .sellerId("seller_123")
                .build();

        MvcResult firstResult = mockMvc.perform(post("/api/v1/buyer/orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String firstResponseBody = firstResult.getResponse().getContentAsString();

        MvcResult secondResult = mockMvc.perform(post("/api/v1/buyer/orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String secondResponseBody = secondResult.getResponse().getContentAsString();
        assertEquals(firstResponseBody, secondResponseBody);
    }

    @Test
    public void testOrderStateMachineAndInvalidTransition422() throws Exception {
        OrderItemDto item = OrderItemDto.builder()
                .productId("prod_3")
                .quantity(5)
                .unitPrice(new BigDecimal("1000.00"))
                .build();

        OrderCreateDto dto = OrderCreateDto.builder()
                .items(Collections.singletonList(item))
                .shippingAddress("Nagpur Hub")
                .sellerId("seller_123")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/buyer/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(content).get("id").asLong();

        mockMvc.perform(put("/api/v1/seller/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderStatusUpdateDto.builder().status(OrderStatus.PROCESSING).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PROCESSING")));

        mockMvc.perform(put("/api/v1/seller/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderStatusUpdateDto.builder().status(OrderStatus.PACKED).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PACKED")));

        mockMvc.perform(put("/api/v1/seller/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderStatusUpdateDto.builder()
                                .status(OrderStatus.SHIPPED)
                                .trackingNo("VRL-89210344")
                                .shippingCarrier("VRL Logistics Express")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")))
                .andExpect(jsonPath("$.trackingNo", is("VRL-89210344")));

        mockMvc.perform(put("/api/v1/seller/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderStatusUpdateDto.builder().status(OrderStatus.PROCESSING).build())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Unprocessable Entity")));
    }

    @Test
    public void testFileUploadEndpoints() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "kyc_doc.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Sample KYC PDF Content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl", startsWith("/uploads/")))
                .andExpect(jsonPath("$.fileType", is("application/pdf")));
    }

    @Test
    public void testChatAndNotificationTokenEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/chats/seller_123/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Is bulk discount available?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Is bulk discount available?")));

        mockMvc.perform(get("/api/v1/chats/seller_123/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(post("/api/v1/notifications/register-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"fcm_token_123\",\"deviceType\":\"FCM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("fcm_token_123")));
    }

    @Test
    public void testSellerAnalyticsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/seller/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue", notNullValue()))
                .andExpect(jsonPath("$.totalOrders", notNullValue()));
    }
}
