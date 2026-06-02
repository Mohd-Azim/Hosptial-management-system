package com.hospital.hms.service;

import com.hospital.hms.domain.User;
import com.hospital.hms.domain.UserRoleAssignment;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.UserRoleAssignmentRepository;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final AuthService authService;

    public AdminUserService(UserRepository userRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            AuthService authService) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void suspend(Long userId, boolean value) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setSuspended(value);
        u.setUpdatedAt(Instant.now());
        userRepository.save(u);
    }

    @Transactional
    public void revokeAccess(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setRevokedAt(Instant.now());
        u.setActive(false);
        u.setUpdatedAt(Instant.now());
        userRepository.save(u);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Instant now = Instant.now();
        u.setDeletedAt(now);
        u.setActive(false);
        u.setUpdatedAt(now);
        userRepository.save(u);
        List<UserRoleAssignment> links = userRoleAssignmentRepository.findByUser_Id(userId);
        for (UserRoleAssignment link : links) {
            link.setDeletedAt(now);
            userRoleAssignmentRepository.save(link);
        }
    }

    @Transactional
    public void assignRole(Long userId, RoleCode roleCode) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        authService.assignRole(u, roleCode);
    }
}
