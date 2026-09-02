package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDto {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private List<String> permissions;
    private String status;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
