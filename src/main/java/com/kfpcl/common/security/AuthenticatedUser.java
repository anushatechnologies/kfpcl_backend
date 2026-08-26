package com.kfpcl.common.security;

import com.kfpcl.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {
    private String userId;
    private String phone;
    private Role role;
    private String sessionId;
    private boolean isVerified;
}
