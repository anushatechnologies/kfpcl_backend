package com.kfpcl.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MockAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String authHeader = request.getHeader("Authorization");
        
        // If the user sends an Authorization header, use it
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            request.setAttribute("authenticatedUser", token);
            if (token.toLowerCase().contains("admin")) {
                request.setAttribute("userRole", "ROLE_ADMIN");
            } else if (token.toLowerCase().contains("seller")) {
                request.setAttribute("userRole", "ROLE_SUPPLIER");
            } else {
                request.setAttribute("userRole", "ROLE_BUYER");
            }
            return true;
        }
        
        // NO JWT / NO HEADER SCENARIO: Automatically assign roles based on the URL path
        String path = request.getRequestURI();
        if (path.contains("/admin/")) {
            request.setAttribute("authenticatedUser", "user_admin_default");
            request.setAttribute("userRole", "ROLE_ADMIN");
        } else if (path.contains("/seller/")) {
            request.setAttribute("authenticatedUser", "seller-123");
            request.setAttribute("userRole", "ROLE_SUPPLIER");
        } else if (path.contains("/buyer/")) {
            request.setAttribute("authenticatedUser", "buyer-999");
            request.setAttribute("userRole", "ROLE_BUYER");
        } else {
            // Default fallback
            request.setAttribute("authenticatedUser", "guest-user");
            request.setAttribute("userRole", "ROLE_USER");
        }
        
        return true;
    }
}
