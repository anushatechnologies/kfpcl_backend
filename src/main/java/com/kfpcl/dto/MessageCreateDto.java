package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateDto {

    @NotBlank(message = "Message content is required")
    private String content;

    private List<MessageAttachmentDto> attachments;
}
