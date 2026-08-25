package com.kfpcl.service;

import com.kfpcl.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ConversationService {

    PageResponseDto<ConversationResponseDto> getConversations(String userId, int page, int size);

    ConversationResponseDto createConversation(ConversationCreateDto dto, String currentUserId);

    List<MessageResponseDto> getMessages(String conversationId, String cursor, int limit);

    MessageResponseDto sendMessage(String conversationId, MessageCreateDto dto, String senderId);

    void markConversationAsRead(String conversationId, String readerId);

    MessageAttachmentDto uploadAttachment(String conversationId, MultipartFile file);
}
