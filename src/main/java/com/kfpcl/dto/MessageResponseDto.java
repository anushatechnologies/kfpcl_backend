package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private List<MessageAttachmentDto> attachments;
    private LocalDateTime createdAt;
}
