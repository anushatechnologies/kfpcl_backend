package com.kfpcl.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * Extracts the user email from SecurityContext or 'X-User-Email' request header.
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }

        // Fallback for header-based identity in non-JWT / direct request mode
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String headerEmail = request.getHeader("X-User-Email");
                if (headerEmail != null && !headerEmail.isBlank()) {
                    return headerEmail.trim();
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
