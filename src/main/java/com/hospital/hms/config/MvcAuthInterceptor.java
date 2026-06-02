package com.hospital.hms.config;

import com.hospital.hms.auth.SessionPrincipalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MvcAuthInterceptor implements HandlerInterceptor {

    private final SessionPrincipalService sessionPrincipalService;

    public MvcAuthInterceptor(SessionPrincipalService sessionPrincipalService) {
        this.sessionPrincipalService = sessionPrincipalService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (sessionPrincipalService.attachPrincipal(request).isPresent()) {
            return true;
        }
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            uri += "?" + query;
        }
        String redirectParam = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + redirectParam);
        return false;
    }
}
