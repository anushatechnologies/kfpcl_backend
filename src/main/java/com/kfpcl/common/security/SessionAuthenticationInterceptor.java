package com.kfpcl.common.security;

import com.kfpcl.common.exception.ForbiddenException;
import com.kfpcl.common.exception.UnauthorizedException;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.service.SessionService;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.entity.User;
import com.kfpcl.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
@Component
public class SessionAuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ATTR = "authenticatedUser";
    public static final String SESSION_HEADER = "X-Session-Id";

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Value("${app.session.cookie-name:KFPCL_SESSION_ID}")
    private String sessionCookieName;

    public SessionAuthenticationInterceptor(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        String sessionId = extractSessionId(request);

        if (requireRole != null) {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new UnauthorizedException("Authentication required. Please provide a valid session cookie.");
            }

            UserSession session = sessionService.validateAndGetSession(sessionId);

            User user = userRepository.findByIdAndIsActiveTrue(session.getUserId())
                    .orElseThrow(() -> new UnauthorizedException("User account not found or is deactivated"));

            AuthenticatedUser authUser = AuthenticatedUser.builder()
                    .userId(user.getId())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .sessionId(session.getSessionId())
                    .isVerified(user.isVerified())
                    .build();

            UserContext.setCurrentUser(authUser);
            request.setAttribute(AUTHENTICATED_USER_ATTR, authUser);

            boolean roleAllowed = Arrays.stream(requireRole.value())
                    .anyMatch(role -> role == user.getRole());

            if (!roleAllowed) {
                throw new ForbiddenException(String.format(
                        "Access denied. User role '%s' does not have permission for this resource. Required role(s): %s",
                        user.getRole(), Arrays.toString(requireRole.value())
                ));
            }
        } else if (sessionId != null && !sessionId.trim().isEmpty()) {
            try {
                UserSession session = sessionService.validateAndGetSession(sessionId);
                userRepository.findByIdAndIsActiveTrue(session.getUserId()).ifPresent(user -> {
                    AuthenticatedUser authUser = AuthenticatedUser.builder()
                            .userId(user.getId())
                            .phone(user.getPhone())
                            .role(user.getRole())
                            .sessionId(session.getSessionId())
                            .isVerified(user.isVerified())
                            .build();
                    UserContext.setCurrentUser(authUser);
                    request.setAttribute(AUTHENTICATED_USER_ATTR, authUser);
                });
            } catch (Exception ignored) {
                // For unauthenticated routes with invalid session, ignore and proceed
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    public String extractSessionId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (sessionCookieName.equals(cookie.getName()) && cookie.getValue() != null) {
                    return cookie.getValue();
                }
            }
        }
        // Fallback for API clients / tools if header is provided
        return request.getHeader(SESSION_HEADER);
    }
}
