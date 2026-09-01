package com.kfpcl.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class MockAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String authHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        // 1. If user sends Authorization Bearer header, parse user and role
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String rawToken = authHeader.substring(7).trim();
            String resolvedUser = extractUserFromToken(rawToken);
            request.setAttribute("authenticatedUser", resolvedUser);

            if (resolvedUser.toLowerCase().contains("admin") || rawToken.toLowerCase().contains("admin")) {
                request.setAttribute("userRole", "ROLE_ADMIN");
            } else if (resolvedUser.toLowerCase().contains("seller") || rawToken.toLowerCase().contains("seller")) {
                request.setAttribute("userRole", "ROLE_SUPPLIER");
            } else {
                request.setAttribute("userRole", "ROLE_BUYER");
            }
            return true;
        }

        // 2. Check X-User-Id or X-Buyer-Id header
        String customUserId = request.getHeader("X-User-Id");
        if (customUserId == null || customUserId.isBlank()) {
            customUserId = request.getHeader("X-Buyer-Id");
        }
        if (customUserId != null && !customUserId.isBlank()) {
            request.setAttribute("authenticatedUser", customUserId.trim());
            request.setAttribute("userRole", "ROLE_BUYER");
            return true;
        }

        // 3. Fallback based on URI path
        if (path.contains("/admin/")) {
            request.setAttribute("authenticatedUser", "user_admin_default");
            request.setAttribute("userRole", "ROLE_ADMIN");
        } else if (path.contains("/seller/")) {
            request.setAttribute("authenticatedUser", "seller-123");
            request.setAttribute("userRole", "ROLE_SUPPLIER");
        } else if (path.contains("/buyer/") || path.contains("/rfq") || path.contains("/cart")) {
            request.setAttribute("authenticatedUser", "buyer-999");
            request.setAttribute("userRole", "ROLE_BUYER");
        } else {
            request.setAttribute("authenticatedUser", "buyer-999");
            request.setAttribute("userRole", "ROLE_USER");
        }

        return true;
    }

    private String extractUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            return "buyer-999";
        }

        // If it's a 3-part JWT token (header.payload.signature)
        String[] parts = token.split("\\.");
        if (parts.length >= 2) {
            try {
                byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
                JsonNode payload = OBJECT_MAPPER.readTree(new String(decodedBytes, StandardCharsets.UTF_8));

                if (payload.hasNonNull("email")) {
                    return payload.get("email").asText();
                }
                if (payload.hasNonNull("userId")) {
                    return payload.get("userId").asText();
                }
                if (payload.hasNonNull("user_id")) {
                    return payload.get("user_id").asText();
                }
                if (payload.hasNonNull("sub")) {
                    return payload.get("sub").asText();
                }
                if (payload.hasNonNull("id")) {
                    return payload.get("id").asText();
                }
            } catch (Exception ex) {
                log.debug("Could not decode JWT payload: {}", ex.getMessage());
            }
        }

        return token;
    }
}
