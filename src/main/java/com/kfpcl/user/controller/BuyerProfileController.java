package com.kfpcl.user.controller;

import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.RequireRole;
import com.kfpcl.common.security.UserContext;
import com.kfpcl.user.dto.BuyerProfileResponseDto;
import com.kfpcl.user.dto.BuyerProfileUpdateDto;
import com.kfpcl.user.entity.Role;
import com.kfpcl.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Buyer Profile", description = "Endpoints for authenticated buyers to view and update company profile")
@RestController
@RequestMapping("/api/v1/buyer/profile")
public class BuyerProfileController {

    private final UserService userService;

    public BuyerProfileController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get Buyer Profile", description = "Retrieves the authenticated buyer profile using server-side session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Buyer profile fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (buyer only)")
    })
    @RequireRole(Role.BUYER)
    @GetMapping
    public ResponseEntity<ApiResponse<BuyerProfileResponseDto>> getProfile() {
        String userId = UserContext.getRequiredUserId();
        BuyerProfileResponseDto profile = userService.getBuyerProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Buyer profile fetched successfully", profile));
    }

    @Operation(summary = "Update Buyer Profile", description = "Updates permitted fields for the authenticated buyer company profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Buyer profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or expired session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Role mismatch (buyer only)")
    })
    @RequireRole(Role.BUYER)
    @PutMapping
    public ResponseEntity<ApiResponse<BuyerProfileResponseDto>> updateProfile(@Valid @RequestBody BuyerProfileUpdateDto request) {
        String userId = UserContext.getRequiredUserId();
        BuyerProfileResponseDto updatedProfile = userService.updateBuyerProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Buyer profile updated successfully", updatedProfile));
    }
}
