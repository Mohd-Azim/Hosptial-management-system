package com.hospital.hms.auth;

import com.hospital.hms.domain.User;
import com.hospital.hms.domain.UserRoleAssignment;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.UserRoleAssignmentRepository;
import com.hospital.hms.support.HmsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SessionPrincipalService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public SessionPrincipalService(UserRepository userRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    /**
     * Resolves logged-in user from session and attaches {@link UserPrincipal} to the request.
     *
     * @return empty if no session / invalid user / inactive account
     */
    public Optional<UserPrincipal> attachPrincipal(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object raw = session.getAttribute(HmsConstants.SESSION_USER_ID);
        if (!(raw instanceof Long userId)) {
            return Optional.empty();
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive() || user.isSuspended() || user.getRevokedAt() != null) {
            return Optional.empty();
        }
        Set<RoleCode> roles = loadRoles(userId);
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);
        request.setAttribute(HmsConstants.REQUEST_USER, principal);
        return Optional.of(principal);
    }

    private Set<RoleCode> loadRoles(Long userId) {
        List<UserRoleAssignment> links = userRoleAssignmentRepository.findActiveWithRolesByUserId(userId);
        Set<RoleCode> set = new HashSet<>();
        for (UserRoleAssignment link : links) {
            try {
                set.add(RoleCode.valueOf(link.getRole().getName()));
            } catch (IllegalArgumentException ignored) {
                // skip
            }
        }
        return Set.copyOf(set);
    }
}
