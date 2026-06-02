package com.hospital.hms.config;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.service.AuditService;
import com.hospital.hms.support.HmsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();
        if (!"POST".equals(method)
                && !"PUT".equals(method)
                && !"DELETE".equals(method)
                && !"PATCH".equals(method)) {
            return;
        }
        String uri = request.getRequestURI();
        if (shouldSkip(uri)) {
            return;
        }
        Long userId = null;
        String username = null;
        Object raw = request.getAttribute(HmsConstants.REQUEST_USER);
        if (raw instanceof UserPrincipal p) {
            userId = p.userId();
            username = p.username();
        }
        String ip = clientIp(request);
        String detail = ex != null ? trunc(ex.getMessage(), 500) : null;
        auditService.recordHttpAction(
                userId, username, method, uri, response.getStatus(), ip, detail);
    }

    private static boolean shouldSkip(String uri) {
        if (uri.startsWith("/actuator")) {
            return true;
        }
        if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/register")) {
            return true;
        }
        if ("/login".equals(uri) || "/register".equals(uri)) {
            return true;
        }
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/error")) {
            return true;
        }
        return false;
    }

    private static String clientIp(HttpServletRequest request) {
        String x = request.getHeader("X-Forwarded-For");
        if (x != null && !x.isBlank()) {
            return x.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String trunc(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
