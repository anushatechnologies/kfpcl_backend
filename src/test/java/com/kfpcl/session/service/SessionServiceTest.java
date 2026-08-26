package com.kfpcl.session.service;

import com.kfpcl.common.exception.InvalidSessionException;
import com.kfpcl.common.exception.SessionExpiredException;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.session.repository.UserSessionRepository;
import com.kfpcl.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private UserSessionRepository sessionRepository;

    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionServiceImpl(sessionRepository);
        ReflectionTestUtils.setField(sessionService, "sessionTimeoutHours", 8);
    }

    @Test
    @DisplayName("Should create server session with cryptographically random session ID and 8-hour expiry")
    void testCreateSession_Success() {
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSession session = sessionService.createSession("usr_1001", Role.BUYER, "127.0.0.1", "JUnit-Agent");

        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertTrue(session.getSessionId().startsWith("sess_"));
        assertEquals("usr_1001", session.getUserId());
        assertEquals(Role.BUYER, session.getRole());
        assertTrue(session.isActive());
        assertNotNull(session.getExpiresAt());
        assertTrue(session.getExpiresAt().isAfter(LocalDateTime.now().plusHours(7)));
    }

    @Test
    @DisplayName("Should validate active session and touch lastAccessedAt")
    void testValidateAndGetSession_Success() {
        UserSession existingSession = UserSession.builder()
                .sessionId("sess_valid123")
                .userId("usr_1001")
                .role(Role.BUYER)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusHours(1))
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(7))
                .build();

        when(sessionRepository.findBySessionId("sess_valid123")).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSession validated = sessionService.validateAndGetSession("sess_valid123");

        assertNotNull(validated);
        assertEquals("usr_1001", validated.getUserId());
        assertTrue(validated.getLastAccessedAt().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    @DisplayName("Should throw SessionExpiredException when session has passed expiresAt")
    void testValidateAndGetSession_Expired() {
        UserSession expiredSession = UserSession.builder()
                .sessionId("sess_expired123")
                .userId("usr_1001")
                .role(Role.BUYER)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusHours(9))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(sessionRepository.findBySessionId("sess_expired123")).thenReturn(Optional.of(expiredSession));

        assertThrows(SessionExpiredException.class, () ->
                sessionService.validateAndGetSession("sess_expired123")
        );

        assertFalse(expiredSession.isActive());
        verify(sessionRepository).save(expiredSession);
    }

    @Test
    @DisplayName("Should throw InvalidSessionException when session is inactive")
    void testValidateAndGetSession_Inactive() {
        UserSession inactiveSession = UserSession.builder()
                .sessionId("sess_inactive123")
                .userId("usr_1001")
                .role(Role.BUYER)
                .isActive(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(7))
                .build();

        when(sessionRepository.findBySessionId("sess_inactive123")).thenReturn(Optional.of(inactiveSession));

        assertThrows(InvalidSessionException.class, () ->
                sessionService.validateAndGetSession("sess_inactive123")
        );
    }

    @Test
    @DisplayName("Should invalidate session on logout")
    void testInvalidateSession() {
        UserSession session = UserSession.builder()
                .sessionId("sess_logout123")
                .userId("usr_1001")
                .isActive(true)
                .build();

        when(sessionRepository.findBySessionId("sess_logout123")).thenReturn(Optional.of(session));

        sessionService.invalidateSession("sess_logout123");

        assertFalse(session.isActive());
        verify(sessionRepository).save(session);
    }
}
