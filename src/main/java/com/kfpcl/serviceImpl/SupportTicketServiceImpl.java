package com.kfpcl.serviceImpl;

import com.kfpcl.dto.*;
import com.kfpcl.entity.SupportTicket;
import com.kfpcl.entity.TicketReply;
import com.kfpcl.entity.User;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.SupportTicketRepository;
import com.kfpcl.repository.TicketReplyRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.AuditLogService;
import com.kfpcl.service.SupportTicketService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public SupportTicketResponseDto createTicket(SupportTicketCreateDto dto, String userId) {
        String actualUserId = StringUtils.hasText(userId) ? userId : "user_guest";
        User user = userRepository.findById(actualUserId).orElse(null);

        SupportTicket.Priority priority = SupportTicket.Priority.MEDIUM;
        if (StringUtils.hasText(dto.getPriority())) {
            try {
                priority = SupportTicket.Priority.valueOf(dto.getPriority().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String ticketId = "tkt_" + UUID.randomUUID().toString().substring(0, 8);
        String ticketNumber = "KFP-TKT-" + (System.currentTimeMillis() % 1000000);

        SupportTicket ticket = SupportTicket.builder()
                .id(ticketId)
                .ticketNumber(ticketNumber)
                .userId(actualUserId)
                .userName(user != null ? user.getName() : "Guest User")
                .userEmail(user != null ? user.getEmail() : "user@kfpcl.com")
                .subject(dto.getSubject().trim())
                .description(dto.getDescription().trim())
                .category(dto.getCategory() != null ? dto.getCategory() : "General Inquiry")
                .priority(priority)
                .status(SupportTicket.Status.OPEN)
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SupportTicketResponseDto> getAdminTickets(String status, String priority, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<SupportTicket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    SupportTicket.Status tStatus = SupportTicket.Status.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), tStatus));
                } catch (IllegalArgumentException ignored) {}
            }

            if (StringUtils.hasText(priority)) {
                try {
                    SupportTicket.Priority tPriority = SupportTicket.Priority.valueOf(priority.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("priority"), tPriority));
                } catch (IllegalArgumentException ignored) {}
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SupportTicket> ticketPage = supportTicketRepository.findAll(spec, pageable);
        List<SupportTicketResponseDto> dtoList = ticketPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(ticketPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponseDto getTicketById(String ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketId", ticketId));
        return mapToDto(ticket);
    }

    @Override
    public TicketReplyResponseDto replyToTicket(String ticketId, TicketReplyCreateDto dto, String senderId, String senderRole) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketId", ticketId));

        String actualSenderId = StringUtils.hasText(senderId) ? senderId : "admin";
        String actualRole = StringUtils.hasText(senderRole) ? senderRole : "ROLE_ADMIN";

        User sender = userRepository.findById(actualSenderId).orElse(null);

        TicketReply reply = TicketReply.builder()
                .id("reply_" + UUID.randomUUID().toString().substring(0, 8))
                .ticketId(ticket.getId())
                .senderId(actualSenderId)
                .senderName(sender != null ? sender.getName() : "Support Agent")
                .senderRole(actualRole)
                .message(dto.getMessage().trim())
                .attachments(dto.getAttachments())
                .build();

        TicketReply saved = ticketReplyRepository.save(reply);

        if (ticket.getStatus() == SupportTicket.Status.OPEN) {
            ticket.setStatus(SupportTicket.Status.IN_PROGRESS);
            supportTicketRepository.save(ticket);
        }

        auditLogService.logAction(actualSenderId, actualRole, "REPLY_SUPPORT_TICKET", "SUPPORT_TICKET", ticketId, null, "REPLIED", null, null);

        return TicketReplyResponseDto.builder()
                .id(saved.getId())
                .ticketId(saved.getTicketId())
                .senderId(saved.getSenderId())
                .senderName(saved.getSenderName())
                .senderRole(saved.getSenderRole())
                .message(saved.getMessage())
                .attachments(saved.getAttachments())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public SupportTicketResponseDto updateTicketStatus(String ticketId, TicketStatusUpdateDto dto) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketId", ticketId));

        SupportTicket.Status newStatus;
        try {
            newStatus = SupportTicket.Status.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid ticket status: " + dto.getStatus() + ". Allowed: OPEN, IN_PROGRESS, RESOLVED");
        }

        String oldStatus = ticket.getStatus().name();
        ticket.setStatus(newStatus);
        SupportTicket saved = supportTicketRepository.save(ticket);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_TICKET_STATUS", "SUPPORT_TICKET", ticketId, oldStatus, newStatus.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public SupportTicketResponseDto updateTicketPriority(String ticketId, TicketPriorityUpdateDto dto) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketId", ticketId));

        SupportTicket.Priority newPriority;
        try {
            newPriority = SupportTicket.Priority.valueOf(dto.getPriority().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid ticket priority: " + dto.getPriority() + ". Allowed: LOW, MEDIUM, HIGH, URGENT");
        }

        String oldPriority = ticket.getPriority().name();
        ticket.setPriority(newPriority);
        SupportTicket saved = supportTicketRepository.save(ticket);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_TICKET_PRIORITY", "SUPPORT_TICKET", ticketId, oldPriority, newPriority.name(), null, null);

        return mapToDto(saved);
    }

    private SupportTicketResponseDto mapToDto(SupportTicket ticket) {
        List<TicketReplyResponseDto> replies = ticketReplyRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                .map(r -> TicketReplyResponseDto.builder()
                        .id(r.getId())
                        .ticketId(r.getTicketId())
                        .senderId(r.getSenderId())
                        .senderName(r.getSenderName())
                        .senderRole(r.getSenderRole())
                        .message(r.getMessage())
                        .attachments(r.getAttachments())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SupportTicketResponseDto.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .userId(ticket.getUserId())
                .userName(ticket.getUserName())
                .userEmail(ticket.getUserEmail())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority().name())
                .status(ticket.getStatus().name())
                .assignedTo(ticket.getAssignedTo())
                .replies(replies)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
