package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReplyResponseDto {

    private String id;
    private String ticketId;
    private String senderId;
    private String senderName;
    private String senderRole;
    private String message;
    private String attachments;
    private LocalDateTime createdAt;
}
