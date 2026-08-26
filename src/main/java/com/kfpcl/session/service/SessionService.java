package com.kfpcl.session.service;

import com.kfpcl.session.dto.SessionDto;
import com.kfpcl.session.entity.UserSession;
import com.kfpcl.user.entity.Role;

public interface SessionService {

    UserSession createSession(String userId, Role role, String ipAddress, String userAgent);

    UserSession validateAndGetSession(String sessionId);

    void invalidateSession(String sessionId);

    void invalidateAllUserSessions(String userId);

    SessionDto toDto(UserSession session);
}
