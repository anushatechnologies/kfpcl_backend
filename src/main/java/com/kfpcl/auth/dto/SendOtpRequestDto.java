package com.kfpcl.auth.dto;

import com.kfpcl.common.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequestDto {

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phone;
}
