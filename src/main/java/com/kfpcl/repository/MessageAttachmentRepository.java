package com.kfpcl.repository;

import com.kfpcl.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {

    List<MessageAttachment> findByMessageId(String messageId);
}
