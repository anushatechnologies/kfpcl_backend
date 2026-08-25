package com.kfpcl.serviceImpl;

import com.kfpcl.dto.AuditLogResponseDto;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.entity.AuditLog;
import com.kfpcl.repository.AuditLogRepository;
import com.kfpcl.service.AuditLogService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(String actorId, String actorRole, String action, String entityType, String entityId, String previousValue, String newValue, String ipAddress, String deviceInfo) {
        try {
            AuditLog log = AuditLog.builder()
                    .id("audit_" + UUID.randomUUID().toString().substring(0, 8))
                    .actorId(StringUtils.hasText(actorId) ? actorId : "admin")
                    .actorRole(StringUtils.hasText(actorRole) ? actorRole : "ROLE_ADMIN")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .previousValue(previousValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Logging shouldn't block primary transactions
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AuditLogResponseDto> getAuditLogs(String entityType, String action, String from, String to, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(entityType)) {
                predicates.add(cb.equal(cb.upper(root.get("entityType")), entityType.trim().toUpperCase()));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(cb.upper(root.get("action")), action.trim().toUpperCase()));
            }
            if (StringUtils.hasText(from)) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(from);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
                } catch (Exception ignored) {}
            }
            if (StringUtils.hasText(to)) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(to);
                    predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
                } catch (Exception ignored) {}
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);
        List<AuditLogResponseDto> dtoList = logPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(logPage, dtoList);
    }

    private AuditLogResponseDto mapToDto(AuditLog log) {
        return AuditLogResponseDto.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .actorRole(log.getActorRole())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .previousValue(log.getPreviousValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .deviceInfo(log.getDeviceInfo())
                .timestamp(log.getTimestamp())
                .build();
    }
}
