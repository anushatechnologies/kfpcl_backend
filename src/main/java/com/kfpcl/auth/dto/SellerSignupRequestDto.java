package com.kfpcl.auth.dto;

import com.kfpcl.common.validation.ValidGstin;
import com.kfpcl.common.validation.ValidPan;
import com.kfpcl.common.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSignupRequestDto {

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "GSTIN is required")
    @ValidGstin
    private String gstNumber;

    @NotBlank(message = "PAN is required")
    @ValidPan
    private String panNumber;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "KYC document URL is required")
    private String kycDocUrl;

    @NotBlank(message = "PAN document URL is required")
    private String panDocUrl;
}
