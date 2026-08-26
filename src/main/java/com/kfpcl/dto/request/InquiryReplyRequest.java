package com.kfpcl.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryReplyRequest {

    @NotBlank(message = "Reply message cannot be blank")
    @Size(min = 2, max = 5000, message = "Reply message must be between 2 and 5000 characters")
    private String replyMessage;
}
