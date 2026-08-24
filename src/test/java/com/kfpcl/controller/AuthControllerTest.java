package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.LoginRequest;
import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.Role;
import com.kfpcl.entity.User;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.SupplierRepository;
import com.kfpcl.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BuyerRepository buyerRepository;

    @MockBean
    private SupplierRepository supplierRepository;

    @Test
    @DisplayName("POST /api/v1/auth/login - Success")
    void testLogin_Success() throws Exception {
        User user = User.builder()
                .id("usr_1")
                .email("buyer@kfpcl.com")
                .password("password123")
                .name("Rajesh Kumar")
                .role(Role.ROLE_BUYER)
                .build();

        Buyer buyer = Buyer.builder().id("buyer_1").user(user).build();

        Mockito.when(userRepository.findByEmail("buyer@kfpcl.com")).thenReturn(Optional.of(user));
        Mockito.when(buyerRepository.findByUserId("usr_1")).thenReturn(Optional.of(buyer));
        Mockito.when(supplierRepository.findByUserId("usr_1")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder()
                .email("buyer@kfpcl.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("usr_1"))
                .andExpect(jsonPath("$.data.buyerId").value("buyer_1"))
                .andExpect(jsonPath("$.data.email").value("buyer@kfpcl.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Invalid Password 401")
    void testLogin_InvalidPassword() throws Exception {
        User user = User.builder()
                .id("usr_1")
                .email("buyer@kfpcl.com")
                .password("password123")
                .build();

        Mockito.when(userRepository.findByEmail("buyer@kfpcl.com")).thenReturn(Optional.of(user));

        LoginRequest request = LoginRequest.builder()
                .email("buyer@kfpcl.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
