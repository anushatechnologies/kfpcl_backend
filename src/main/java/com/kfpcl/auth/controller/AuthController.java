package com.kfpcl.auth.controller;

import com.kfpcl.auth.service.AuthService;
import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.CookieHelper;
import com.kfpcl.common.security.SessionAuthenticationInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication & Session", description = "Endpoints for session invalidation and logout")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;
    private final SessionAuthenticationInterceptor sessionInterceptor;

    public AuthController(AuthService authService,
                          CookieHelper cookieHelper,
                          SessionAuthenticationInterceptor sessionInterceptor) {
        this.authService = authService;
        this.cookieHelper = cookieHelper;
        this.sessionInterceptor = sessionInterceptor;
    }

    @Operation(summary = "User Logout", description = "Invalidates the server-side session and clears the session cookie")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = sessionInterceptor.extractSessionId(request);
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            authService.logout(sessionId);
        }
        cookieHelper.clearSessionCookie(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
