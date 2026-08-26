package com.kfpcl.controller;

import com.kfpcl.dto.ChatMessageDto;
import com.kfpcl.entity.Conversation;
import com.kfpcl.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<Conversation>> getConversations(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "user_1";
        List<Conversation> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{participantId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessageHistory(
            @PathVariable("participantId") String participantId,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "user_1";
        List<ChatMessageDto> messages = chatService.getMessageHistory(userId, participantId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{participantId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable("participantId") String participantId,
            @RequestBody ChatMessageDto request,
            Authentication authentication) {
        String senderId = authentication != null ? authentication.getName() : "user_1";
        String content = request.getContent() != null ? request.getContent() : "";
        ChatMessageDto sent = chatService.sendMessage(senderId, participantId, content);
        return ResponseEntity.ok(sent);
    }
}
