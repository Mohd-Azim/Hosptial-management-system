package com.hospital.hms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hms.auth.SessionPrincipalService;
import com.hospital.hms.web.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final SessionPrincipalService sessionPrincipalService;
    private final ObjectMapper objectMapper;

    public SessionAuthInterceptor(SessionPrincipalService sessionPrincipalService, ObjectMapper objectMapper) {
        this.sessionPrincipalService = sessionPrincipalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (sessionPrincipalService.attachPrincipal(request).isPresent()) {
            return true;
        }
        return write(response, 401, "Not logged in");
    }

    private boolean write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(Instant.now(), message, status == 401 ? "UNAUTHORIZED" : "FORBIDDEN");
        objectMapper.writeValue(response.getWriter(), body);
        return false;
    }
}
