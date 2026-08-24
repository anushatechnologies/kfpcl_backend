package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String userId;
    private String email;
    private String name;
    private String role;
    private String buyerId;
    private String supplierId;
    private String message;
}
