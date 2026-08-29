package com.kfpcl.service;

import com.kfpcl.dto.ImageUploadResponseDto;
import com.kfpcl.entity.MessageAttachment;
import com.kfpcl.repository.ConversationRepository;
import com.kfpcl.repository.MessageAttachmentRepository;
import com.kfpcl.repository.MessageRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.serviceImpl.ConversationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationAttachmentPersistenceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private MessageAttachmentRepository messageAttachmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ImageUploadService imageUploadService;
    @InjectMocks private ConversationServiceImpl conversationService;

    @Test
    void persistsReturnedS3UrlInAttachmentRecord() {
        when(conversationRepository.existsById("conv_123")).thenReturn(true);
        when(imageUploadService.uploadConversationAttachment(eq("conv_123"), any())).thenReturn(
                ImageUploadResponseDto.builder().fileName("a.jpg")
                        .fileUrl("https://kfpcl-backend-images-2026.s3.ap-south-1.amazonaws.com/conversations/conv_123/a.jpg")
                        .fileSize(12).contentType("image/jpeg").build());
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.uploadAttachment("conv_123", new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[12]));

        ArgumentCaptor<MessageAttachment> attachment = ArgumentCaptor.forClass(MessageAttachment.class);
        verify(messageAttachmentRepository).save(attachment.capture());
        assertEquals("https://kfpcl-backend-images-2026.s3.ap-south-1.amazonaws.com/conversations/conv_123/a.jpg",
                attachment.getValue().getFileUrl());
    }
}
