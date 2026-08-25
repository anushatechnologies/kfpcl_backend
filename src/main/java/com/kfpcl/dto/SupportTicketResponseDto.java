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
public class SupportTicketResponseDto {

    private String id;
    private String ticketNumber;
    private String userId;
    private String userName;
    private String userEmail;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String assignedTo;
    private List<TicketReplyResponseDto> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
