package com.kfpcl.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieHelper {

    @Value("${app.session.cookie-name:KFPCL_SESSION_ID}")
    private String cookieName;

    @Value("${app.session.timeout-hours:8}")
    private int timeoutHours;

    @Value("${app.session.cookie-secure:false}")
    private boolean secure;

    @Value("${app.session.same-site:Lax}")
    private String sameSite;

    public void attachSessionCookie(HttpServletResponse response, String sessionId) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, sessionId)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofHours(timeoutHours))
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearSessionCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getCookieName() {
        return cookieName;
    }
}
