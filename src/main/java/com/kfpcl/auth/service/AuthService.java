package com.kfpcl.auth.service;

import com.kfpcl.auth.dto.AuthResponseDto;
import com.kfpcl.auth.dto.BuyerSignupRequestDto;
import com.kfpcl.auth.dto.CheckPhoneRequestDto;
import com.kfpcl.auth.dto.CheckPhoneResponseDto;
import com.kfpcl.auth.dto.SellerSignupRequestDto;
import com.kfpcl.auth.dto.SendOtpRequestDto;
import com.kfpcl.auth.dto.VerifyOtpRequestDto;
import com.kfpcl.session.entity.UserSession;
import org.springframework.data.util.Pair;

public interface AuthService {

    CheckPhoneResponseDto checkBuyerPhone(CheckPhoneRequestDto request);

    void sendBuyerOtp(SendOtpRequestDto request);

    Pair<AuthResponseDto, UserSession> verifyBuyerOtp(VerifyOtpRequestDto request, String ipAddress, String userAgent);

    Pair<AuthResponseDto, UserSession> signupBuyer(BuyerSignupRequestDto request, String ipAddress, String userAgent);

    CheckPhoneResponseDto checkSupplierPhone(CheckPhoneRequestDto request);

    void sendSupplierOtp(SendOtpRequestDto request);

    Pair<AuthResponseDto, UserSession> verifySupplierOtp(VerifyOtpRequestDto request, String ipAddress, String userAgent);

    Pair<AuthResponseDto, UserSession> signupSupplier(SellerSignupRequestDto request, String ipAddress, String userAgent);

    void logout(String sessionId);
}
