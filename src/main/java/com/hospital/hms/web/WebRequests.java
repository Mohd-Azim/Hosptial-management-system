package com.hospital.hms.web;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.web.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public final class WebRequests {

    private WebRequests() {
    }

    public static UserPrincipal principal(HttpServletRequest request) {
        UserPrincipal p = (UserPrincipal) request.getAttribute(HmsConstants.REQUEST_USER);
        if (p == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return p;
    }
}
