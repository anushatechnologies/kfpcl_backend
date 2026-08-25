package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateDto {

    @NotBlank(message = "Recipient userId is required")
    private String recipientId;

    private String subject;

    @NotBlank(message = "Initial message content is required")
    private String message;
}
