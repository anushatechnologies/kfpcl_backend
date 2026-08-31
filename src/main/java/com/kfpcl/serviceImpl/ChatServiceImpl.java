package com.kfpcl.serviceImpl;

import com.kfpcl.dto.ChatMessageDto;
import com.kfpcl.entity.ChatMessage;
import com.kfpcl.entity.Conversation;
import com.kfpcl.repository.ChatMessageRepository;
import com.kfpcl.repository.ConversationRepository;
import com.kfpcl.service.ChatService;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(ConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findUserConversations(userId, Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessageHistory(String userId, String participantId) {
        Optional<Conversation> convOpt = conversationRepository.findBetweenUsers(userId, participantId);
        if (convOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return messageRepository.findByConversationIdOrderByTimestampAsc(convOpt.get().getId()).stream()
                .map(msg -> ChatMessageDto.builder()
                        .id(msg.getId())
                        .conversationId(msg.getConversationId())
                        .senderId(msg.getSenderId())
                        .receiverId(msg.getReceiverId())
                        .content(msg.getContent())
                        .timestamp(msg.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(String senderId, String receiverId, String content) {
        Conversation conversation = conversationRepository.findBetweenUsers(senderId, receiverId)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .id(UUID.randomUUID().toString())
                            .participantOneId(senderId)
                            .participantTwoId(receiverId)
                            .lastMessage(content)
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMsg = messageRepository.save(message);

        ChatMessageDto dto = ChatMessageDto.builder()
                .id(savedMsg.getId())
                .conversationId(savedMsg.getConversationId())
                .senderId(savedMsg.getSenderId())
                .receiverId(savedMsg.getReceiverId())
                .content(savedMsg.getContent())
                .timestamp(savedMsg.getTimestamp())
                .build();

        try {
            messagingTemplate.convertAndSend("/topic/chat/" + conversation.getId(), dto);
            messagingTemplate.convertAndSendToUser(receiverId, "/queue/messages", dto);
        } catch (Exception ex) {
            // Ignore
        }

        return dto;
    }
}
