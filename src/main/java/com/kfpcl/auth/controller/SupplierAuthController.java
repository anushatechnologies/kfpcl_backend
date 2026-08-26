package com.kfpcl.auth.controller;

import com.kfpcl.auth.dto.AuthResponseDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.CheckPhoneResponseDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
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

@Tag(name = "Supplier Authentication", description = "Endpoints for Supplier/Seller mobile phone check, OTP login, and signup with KYC")
@RestController
@RequestMapping("/api/v1/auth/supplier")
public class SupplierAuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    public SupplierAuthController(AuthService authService, CookieHelper cookieHelper) {
        this.authService = authService;
        this.cookieHelper = cookieHelper;
    }

    @Operation(summary = "Check Supplier Phone Number", description = "Checks whether the supplier mobile phone is already registered")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Phone number checked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid phone format")
    })
    @PostMapping("/check-phone")
    public ResponseEntity<ApiResponse<CheckPhoneResponseDto>> checkPhone(@Valid @RequestBody CheckPhoneRequestDto request) {
        CheckPhoneResponseDto responseDto = authService.checkSupplierPhone(request);
        return ResponseEntity.ok(ApiResponse.success("Phone number checked successfully", responseDto));
    }

    @Operation(summary = "Send Supplier OTP", description = "Dispatches a 6-digit verification OTP to the supplier's phone")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid phone or rate limit exceeded")
    })
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {
        authService.sendSupplierOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @Operation(summary = "Verify Supplier OTP", description = "Verifies the 6-digit OTP and establishes an authenticated server-side session. Note: Does NOT automatically approve KYC.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier authenticated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request,
                                                                  HttpServletRequest httpRequest,
                                                                  HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Pair<AuthResponseDto, UserSession> authResult = authService.verifySupplierOtp(request, ipAddress, userAgent);

        // Attach secure HttpOnly session cookie
        cookieHelper.attachSessionCookie(httpResponse, authResult.getSecond().getSessionId());

        return ResponseEntity.ok(ApiResponse.success("Supplier authenticated successfully", authResult.getFirst()));
    }

    @Operation(summary = "Supplier Signup", description = "Registers supplier with GSTIN, PAN, KYC documents, and establishes an authenticated seller session (pending KYC verification)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Supplier registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error (invalid GSTIN, PAN, etc.)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Phone number already registered")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponseDto>> signup(@Valid @RequestBody SellerSignupRequestDto request,
                                                               HttpServletRequest httpRequest,
                                                               HttpServletResponse httpResponse) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Pair<AuthResponseDto, UserSession> authResult = authService.signupSupplier(request, ipAddress, userAgent);

        // Attach secure HttpOnly session cookie
        cookieHelper.attachSessionCookie(httpResponse, authResult.getSecond().getSessionId());

        return new ResponseEntity<>(ApiResponse.success("Supplier registered successfully", authResult.getFirst()), HttpStatus.CREATED);
    }
}
