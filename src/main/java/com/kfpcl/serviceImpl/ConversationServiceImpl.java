package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.Conversation;
import com.kfpcl.entity.Message;
import com.kfpcl.entity.MessageAttachment;
import com.kfpcl.entity.User;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ConversationRepository;
import com.kfpcl.repository.MessageAttachmentRepository;
import com.kfpcl.repository.MessageRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.ConversationService;
import com.kfpcl.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ConversationResponseDto> getConversations(String userId, int page, int size) {
        String currentUserId = StringUtils.hasText(userId) ? userId : "user_admin_default";
        Pageable pageable = PageRequest.of(page, size);

        Page<Conversation> convPage = conversationRepository.findUserConversations(currentUserId, pageable);
        List<ConversationResponseDto> dtoList = convPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(convPage, dtoList);
    }

    @Override
    public ConversationResponseDto createConversation(ConversationCreateDto dto, String currentUserId) {
        String senderId = StringUtils.hasText(currentUserId) ? currentUserId : "user_admin_default";
        String recipientId = dto.getRecipientId().trim();

        User sender = userRepository.findById(senderId).orElse(null);
        User recipient = userRepository.findById(recipientId).orElse(null);

        Conversation conv = conversationRepository.findBetweenUsers(senderId, recipientId)
                .orElse(null);

        if (conv == null) {
            conv = Conversation.builder()
                    .id("conv_" + UUID.randomUUID().toString().substring(0, 8))
                    .participantOneId(senderId)
                    .participantOneName(sender != null ? sender.getName() : "Admin")
                    .participantTwoId(recipientId)
                    .participantTwoName(recipient != null ? recipient.getName() : "Recipient")
                    .subject(StringUtils.hasText(dto.getSubject()) ? dto.getSubject().trim() : "Direct Message")
                    .lastMessage(dto.getMessage())
                    .lastMessageAt(LocalDateTime.now())
                    .build();
            conv = conversationRepository.save(conv);
        } else {
            conv.setLastMessage(dto.getMessage());
            conv.setLastMessageAt(LocalDateTime.now());
            conv = conversationRepository.save(conv);
        }

        // Add initial message
        Message msg = Message.builder()
                .id("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .conversationId(conv.getId())
                .senderId(senderId)
                .senderName(sender != null ? sender.getName() : "Admin")
                .content(dto.getMessage().trim())
                .isRead(false)
                .build();
        messageRepository.save(msg);

        return mapToDto(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages(String conversationId, String cursor, int limit) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation", "conversationId", conversationId);
        }

        int pageSize = limit > 0 ? limit : 50;
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("createdAt").descending());
        Page<Message> msgPage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        return msgPage.getContent().stream()
                .map(this::mapMessageToDto)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponseDto sendMessage(String conversationId, MessageCreateDto dto, String senderId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "conversationId", conversationId));

        String actualSenderId = StringUtils.hasText(senderId) ? senderId : "user_admin_default";
        User sender = userRepository.findById(actualSenderId).orElse(null);

        Message message = Message.builder()
                .id("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .conversationId(conv.getId())
                .senderId(actualSenderId)
                .senderName(sender != null ? sender.getName() : "User")
                .content(dto.getContent().trim())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        if (dto.getAttachments() != null) {
            for (MessageAttachmentDto att : dto.getAttachments()) {
                MessageAttachment attachment = MessageAttachment.builder()
                        .id("att_" + UUID.randomUUID().toString().substring(0, 8))
                        .messageId(saved.getId())
                        .fileUrl(att.getFileUrl())
                        .fileName(att.getFileName())
                        .fileType(att.getFileType())
                        .fileSize(att.getFileSize())
                        .build();
                messageAttachmentRepository.save(attachment);
            }
        }

        conv.setLastMessage(dto.getContent().trim());
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        return mapMessageToDto(saved);
    }

    @Override
    public void markConversationAsRead(String conversationId, String readerId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation", "conversationId", conversationId);
        }
        String actualReader = StringUtils.hasText(readerId) ? readerId : "user_admin_default";
        messageRepository.markAsRead(conversationId, actualReader);
    }

    @Override
    public MessageAttachmentDto uploadAttachment(String conversationId, MultipartFile file) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation", "conversationId", conversationId);
        }

        ImageUploadResponseDto upload = imageUploadService.uploadConversationAttachment(conversationId, file);

        MessageAttachment attachment = MessageAttachment.builder()
                .id("att_" + UUID.randomUUID().toString().substring(0, 8))
                .messageId("msg_pending")
                .fileUrl(upload.getFileUrl())
                .fileName(upload.getFileName())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        MessageAttachment saved = messageAttachmentRepository.save(attachment);

        return MessageAttachmentDto.builder()
                .id(saved.getId())
                .fileUrl(saved.getFileUrl())
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .fileSize(saved.getFileSize())
                .build();
    }

    private ConversationResponseDto mapToDto(Conversation conv) {
        return ConversationResponseDto.builder()
                .id(conv.getId())
                .participantOneId(conv.getParticipantOneId())
                .participantOneName(conv.getParticipantOneName())
                .participantTwoId(conv.getParticipantTwoId())
                .participantTwoName(conv.getParticipantTwoName())
                .subject(conv.getSubject())
                .lastMessage(conv.getLastMessage())
                .lastMessageAt(conv.getLastMessageAt())
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }

    private MessageResponseDto mapMessageToDto(Message msg) {
        List<MessageAttachmentDto> attachments = messageAttachmentRepository.findByMessageId(msg.getId()).stream()
                .map(a -> MessageAttachmentDto.builder()
                        .id(a.getId())
                        .fileUrl(a.getFileUrl())
                        .fileName(a.getFileName())
                        .fileType(a.getFileType())
                        .fileSize(a.getFileSize())
                        .build())
                .collect(Collectors.toList());

        return MessageResponseDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .senderName(msg.getSenderName())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .attachments(attachments)
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
