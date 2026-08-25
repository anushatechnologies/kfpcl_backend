package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ConversationResponseDto>>> listConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<ConversationResponseDto> conversations = conversationService.getConversations(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(conversations, "Conversations retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponseDto>> createConversation(
            @Valid @RequestBody ConversationCreateDto dto,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("authenticatedUser");
        ConversationResponseDto conversation = conversationService.createConversation(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversation, "Conversation created successfully"));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponseDto>>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int limit) {

        List<MessageResponseDto> messages = conversationService.getMessages(conversationId, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(messages, "Messages retrieved successfully"));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody MessageCreateDto dto,
            HttpServletRequest request) {

        String senderId = (String) request.getAttribute("authenticatedUser");
        MessageResponseDto message = conversationService.sendMessage(conversationId, dto, senderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, "Message sent successfully"));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String conversationId,
            HttpServletRequest request) {

        String readerId = (String) request.getAttribute("authenticatedUser");
        conversationService.markConversationAsRead(conversationId, readerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Conversation marked as read"));
    }

    @PostMapping(value = "/{conversationId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MessageAttachmentDto>> uploadAttachment(
            @PathVariable String conversationId,
            @RequestParam("file") MultipartFile file) {

        MessageAttachmentDto attachment = conversationService.uploadAttachment(conversationId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attachment, "Attachment uploaded successfully"));
    }
}
