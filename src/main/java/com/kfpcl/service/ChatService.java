package com.kfpcl.service;

import com.kfpcl.dto.ChatMessageDto;
import com.kfpcl.entity.Conversation;

import java.util.List;

public interface ChatService {
    List<Conversation> getUserConversations(String userId);

    List<ChatMessageDto> getMessageHistory(String userId, String participantId);

    ChatMessageDto sendMessage(String senderId, String receiverId, String content);
}
