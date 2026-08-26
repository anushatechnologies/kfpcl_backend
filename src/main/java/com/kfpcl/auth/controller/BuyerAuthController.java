package com.kfpcl.auth.controller;

import com.kfpcl.auth.dto.AuthResponseDto;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.CheckPhoneResponseDto;
import com.kfpcl.auth.dto.SendOtpRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.auth.service.AuthService;
import com.kfpcl.common.response.ApiResponse;
import com.kfpcl.common.security.CookieHelper;
import com.kfpcl.session.entity.UserSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Buyer Authentication", description = "Endpoints for Buyer mobile phone check, OTP login, and signup")
@RestController
@RequestMapping("/api/v1/auth/buyer")
public class BuyerAuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    public BuyerAuthController(AuthService authService, CookieHelper cookieHelper) {
        this.authService = authService;
        this.cookieHelper = cookieHelper;
    }

    @Operation(summary = "Check Buyer Phone Number", description = "Checks whether the buyer mobile phone is already registered")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Phone number checked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid phone format")
    })
    @PostMapping("/check-phone")
    public ResponseEntity<ApiResponse<CheckPhoneResponseDto>> checkPhone(@Valid @RequestBody CheckPhoneRequestDto request) {
        CheckPhoneResponseDto responseDto = authService.checkBuyerPhone(request);
        return ResponseEntity.ok(ApiResponse.success("Phone number checked successfully", responseDto));
    }

    @Operation(summary = "Send Buyer OTP", description = "Dispatches a 6-digit verification OTP to the buyer's phone")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid phone or rate limit exceeded")
    })
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {
        authService.sendBuyerOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @Operation(summary = "Verify Buyer OTP", description = "Verifies the 6-digit OTP and establishes an authenticated server-side session via HttpOnly cookie")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Buyer authenticated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request,
                                                                 HttpServletRequest httpRequest,
                                                                 HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Pair<AuthResponseDto, UserSession> authResult = authService.verifyBuyerOtp(request, ipAddress, userAgent);

        // Attach secure HttpOnly session cookie
        cookieHelper.attachSessionCookie(httpResponse, authResult.getSecond().getSessionId());

        return ResponseEntity.ok(ApiResponse.success("Buyer authenticated successfully", authResult.getFirst()));
    }

    @Operation(summary = "Buyer Signup", description = "Completes the buyer business registration and establishes an authenticated server-side session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Buyer registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Phone number already registered")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponseDto>> signup(@Valid @RequestBody BuyerSignupRequestDto request,
                                                               HttpServletRequest httpRequest,
                                                               HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Pair<AuthResponseDto, UserSession> authResult = authService.signupBuyer(request, ipAddress, userAgent);

        // Attach secure HttpOnly session cookie
        cookieHelper.attachSessionCookie(httpResponse, authResult.getSecond().getSessionId());

        return new ResponseEntity<>(ApiResponse.success("Buyer registered successfully", authResult.getFirst()), HttpStatus.CREATED);
    }
}
