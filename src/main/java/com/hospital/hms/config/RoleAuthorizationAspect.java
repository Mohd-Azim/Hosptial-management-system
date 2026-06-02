package com.hospital.hms.config;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.web.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RoleAuthorizationAspect {

    @Before("@annotation(requiresRole)")
    public void checkRoles(JoinPoint joinPoint, RequiresRole requiresRole) {
        RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
        UserPrincipal principal = (UserPrincipal) request.getAttribute(HmsConstants.REQUEST_USER);
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        if (principal.isAdmin()) {
            return;
        }
        boolean allowed = Arrays.stream(requiresRole.value()).anyMatch(principal.roles()::contains);
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }
}
