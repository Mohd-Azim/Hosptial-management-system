package com.hospital.hms.config;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.support.HmsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces {@link RequiresMvcRole} on MVC controller methods (HTML).
 */
@Component
public class MvcRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        RequiresMvcRole ann = AnnotationUtils.findAnnotation(hm.getMethod(), RequiresMvcRole.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(hm.getBeanType(), RequiresMvcRole.class);
        }
        if (ann == null) {
            return true;
        }
        UserPrincipal p = (UserPrincipal) request.getAttribute(HmsConstants.REQUEST_USER);
        if (p == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (p.isAdmin()) {
            return true;
        }
        Set<RoleCode> allowed = new HashSet<>(Arrays.asList(ann.value()));
        boolean ok = allowed.stream().anyMatch(r -> p.roles().contains(r));
        if (!ok) {
            response.sendRedirect(request.getContextPath() + "/app/dashboard?error=forbidden");
            return false;
        }
        return true;
    }
}
