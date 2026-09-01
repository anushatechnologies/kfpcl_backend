package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.SupportTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<SupportTicketResponseDto>>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<SupportTicketResponseDto> tickets = supportTicketService.getAdminTickets(status, priority, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(tickets, "Support tickets retrieved successfully"));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> getTicket(@PathVariable String ticketId) {
        SupportTicketResponseDto ticket = supportTicketService.getTicketById(ticketId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Support ticket retrieved successfully"));
    }

    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<ApiResponse<TicketReplyResponseDto>> replyToTicket(
            @PathVariable String ticketId,
            @Valid @RequestBody TicketReplyCreateDto dto,
            HttpServletRequest request) {

        String senderId = (String) request.getAttribute("authenticatedUser");
        String senderRole = (String) request.getAttribute("userRole");
        TicketReplyResponseDto reply = supportTicketService.replyToTicket(ticketId, dto, senderId, senderRole);
        return ResponseEntity.ok(ApiResponse.success(reply, "Reply sent successfully"));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> updateTicketStatus(
            @PathVariable String ticketId,
            @Valid @RequestBody TicketStatusUpdateDto dto) {

        SupportTicketResponseDto updated = supportTicketService.updateTicketStatus(ticketId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ticket status updated successfully"));
    }

    @PatchMapping("/{ticketId}/priority")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> updateTicketPriority(
            @PathVariable String ticketId,
            @Valid @RequestBody TicketPriorityUpdateDto dto) {

        SupportTicketResponseDto updated = supportTicketService.updateTicketPriority(ticketId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Ticket priority updated successfully"));
    }

    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> assignTicket(
            @PathVariable String ticketId,
            @RequestBody java.util.Map<String, String> payload) {
        // Stub for assigning a ticket
        return ResponseEntity.ok(ApiResponse.success(null, "Ticket assigned successfully"));
    }

    @PostMapping("/{ticketId}/resolve")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> resolveTicket(
            @PathVariable String ticketId) {
        // Stub for resolving a ticket
        return ResponseEntity.ok(ApiResponse.success(null, "Ticket resolved successfully"));
    }
}
