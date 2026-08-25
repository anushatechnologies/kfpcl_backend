package com.kfpcl.service;

import com.kfpcl.dto.*;

public interface SupportTicketService {

    SupportTicketResponseDto createTicket(SupportTicketCreateDto dto, String userId);

    PageResponseDto<SupportTicketResponseDto> getAdminTickets(String status, String priority, int page, int size, String sortBy, String sortDir);

    SupportTicketResponseDto getTicketById(String ticketId);

    TicketReplyResponseDto replyToTicket(String ticketId, TicketReplyCreateDto dto, String senderId, String senderRole);

    SupportTicketResponseDto updateTicketStatus(String ticketId, TicketStatusUpdateDto dto);

    SupportTicketResponseDto updateTicketPriority(String ticketId, TicketPriorityUpdateDto dto);
}
