package com.kfpcl.session.service;

import com.kfpcl.common.exception.InvalidSessionException;
import com.kfpcl.common.exception.SessionExpiredException;
import com.kfpcl.session.dto.SessionDto;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.repository.UserSessionRepository;
import com.kfpcl.user.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.session.timeout-hours:8}")
    private int sessionTimeoutHours;

    public SessionServiceImpl(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public UserSession createSession(String userId, Role role, String ipAddress, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(sessionTimeoutHours);

        String sessionId = generateSecureSessionId();

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(role)
                .createdAt(now)
                .expiresAt(expiresAt)
                .lastAccessedAt(now)
                .isActive(true)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public UserSession validateAndGetSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new InvalidSessionException("Session ID cannot be empty");
        }

        UserSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new InvalidSessionException("Session not found or invalid"));

        if (!session.isActive()) {
            throw new InvalidSessionException("Session is no longer active. Please authenticate again.");
        }

        if (session.isExpired()) {
            session.setActive(false);
            sessionRepository.save(session);
            throw new SessionExpiredException("Your session has expired. Please authenticate again.");
        }

        session.setLastAccessedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    @Override
    @Transactional
    public void invalidateAllUserSessions(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            sessionRepository.deactivateAllByUserId(userId);
        }
    }

    @Override
    public SessionDto toDto(UserSession session) {
        if (session == null) return null;
        return SessionDto.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .role(session.getRole())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .isActive(session.isActive())
                .build();
    }

    private String generateSecureSessionId() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "sess_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
