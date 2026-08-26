package com.kfpcl.user.controller;

import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.CookieHelper;
import com.kfpcl.common.security.RequireRole;
import com.kfpcl.common.security.SessionAuthenticationInterceptor;
import com.kfpcl.common.security.UserContext;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Account", description = "Endpoints for authenticated user account management")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final CookieHelper cookieHelper;
    private final SessionAuthenticationInterceptor sessionInterceptor;

    public UserController(UserService userService,
                          CookieHelper cookieHelper,
                          SessionAuthenticationInterceptor sessionInterceptor) {
        this.userService = userService;
        this.cookieHelper = cookieHelper;
        this.sessionInterceptor = sessionInterceptor;
    }

    @Operation(summary = "Delete Account", description = "Deactivates the user account, destroys all active server sessions, and clears authentication cookies")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired session")
    })
    @RequireRole({Role.BUYER, Role.SELLER, Role.ADMIN})
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        String userId = UserContext.getRequiredUserId();
        String sessionId = sessionInterceptor.extractSessionId(request);

        userService.deleteAccount(userId, sessionId);
        cookieHelper.clearSessionCookie(response);

        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
