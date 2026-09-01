package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponseDto {

    private String userId;
    private String name;
    private String email;
    private String role;
    private List<String> permissions;
    private String accessToken;
    private String refreshToken;
}
