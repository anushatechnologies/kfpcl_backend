package com.kfpcl.controller;

import com.kfpcl.dto.ChatMessageDto;
import com.kfpcl.entity.Conversation;
import com.kfpcl.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "user_1") String userId) {
        List<Conversation> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{participantId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessageHistory(
            @PathVariable("participantId") String participantId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "user_1") String userId) {
        List<ChatMessageDto> messages = chatService.getMessageHistory(userId, participantId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{participantId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable("participantId") String participantId,
            @RequestBody ChatMessageDto request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "user_1") String senderId) {
        String content = request.getContent() != null ? request.getContent() : "";
        ChatMessageDto sent = chatService.sendMessage(senderId, participantId, content);
        return ResponseEntity.ok(sent);
    }
}

