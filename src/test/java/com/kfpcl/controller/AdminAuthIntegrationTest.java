package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.AdminLoginDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Admin Login - Success with default admin without tokens")
    void testAdminLogin_Success() throws Exception {
        AdminLoginDto loginDto = AdminLoginDto.builder()
                .email("admin@kfpcl.com")
                .password("admin123")
                .build();

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@kfpcl.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Admin Login - Invalid Credentials Returns 401 Unauthorized")
    void testAdminLogin_InvalidCredentials() throws Exception {
        AdminLoginDto loginDto = AdminLoginDto.builder()
                .email("admin@kfpcl.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin Logout Flow")
    void testAdminLogout_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Admin Get Me Profile without tokens")
    void testGetAdminProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("admin@kfpcl.com"));
    }
}
