package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.SupportTicketCreateDto;
import com.kfpcl.dto.SupportTicketResponseDto;
import com.kfpcl.service.SupportTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportTicketService supportTicketService;

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<SupportTicketResponseDto>> createTicket(
            @Valid @RequestBody SupportTicketCreateDto dto,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("authenticatedUser");
        SupportTicketResponseDto created = supportTicketService.createTicket(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Support ticket created successfully"));
    }
}
