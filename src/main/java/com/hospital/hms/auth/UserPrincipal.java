package com.hospital.hms.auth;

import com.hospital.hms.domain.enums.RoleCode;
import java.util.Set;

public record UserPrincipal(Long userId, String username, Set<RoleCode> roles) {

    public boolean hasAny(RoleCode... codes) {
        for (RoleCode c : codes) {
            if (roles.contains(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return roles.contains(RoleCode.ADMIN);
    }
}
