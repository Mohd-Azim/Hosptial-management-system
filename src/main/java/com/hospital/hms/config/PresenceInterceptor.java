package com.hospital.hms.config;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.presence.PresenceRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PresenceInterceptor implements HandlerInterceptor {

    private final PresenceRegistry presenceRegistry;

    public PresenceInterceptor(PresenceRegistry presenceRegistry) {
        this.presenceRegistry = presenceRegistry;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object raw = request.getAttribute(HmsConstants.REQUEST_USER);
        if (raw instanceof UserPrincipal p) {
            presenceRegistry.touch(p.userId());
        }
    }
}
