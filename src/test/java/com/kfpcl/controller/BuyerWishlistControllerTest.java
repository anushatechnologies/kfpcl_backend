package com.kfpcl.controller;

import com.kfpcl.dto.ProductResponse;
import com.kfpcl.dto.WishlistItemResponse;
import com.kfpcl.service.WishlistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BuyerWishlistController.class)
class BuyerWishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @Test
    @DisplayName("POST /api/v1/buyer/wishlist/{productId} - 201 Created")
    void testAddToWishlist_Success() throws Exception {
        WishlistItemResponse item = WishlistItemResponse.builder()
                .id("w1")
                .product(ProductResponse.builder().id("prod_1").title("Amul Milk").price(BigDecimal.valueOf(28.0)).build())
                .addedAt(LocalDateTime.now())
                .build();

        Mockito.when(wishlistService.addToWishlist("prod_1")).thenReturn(item);

        mockMvc.perform(post("/api/v1/buyer/wishlist/prod_1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.product.id").value("prod_1"));
    }

    @Test
    @DisplayName("GET /api/v1/buyer/wishlist - 200 OK")
    void testGetWishlist_Success() throws Exception {
        WishlistItemResponse item = WishlistItemResponse.builder()
                .id("w1")
                .product(ProductResponse.builder().id("prod_1").title("Amul Milk").build())
                .build();

        Mockito.when(wishlistService.getBuyerWishlist()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/buyer/wishlist")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("w1"));
    }

    @Test
    @DisplayName("DELETE /api/v1/buyer/wishlist/{productId} - 200 OK")
    void testRemoveFromWishlist_Success() throws Exception {
        Mockito.doNothing().when(wishlistService).removeFromWishlist("prod_1");

        mockMvc.perform(delete("/api/v1/buyer/wishlist/prod_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
