package com.hospital.hms.service;

import com.hospital.hms.domain.PatientProfile;
import com.hospital.hms.domain.Role;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.UserRoleAssignment;
import com.hospital.hms.domain.UserRoleAssignmentId;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.PatientProfileRepository;
import com.hospital.hms.repo.RoleRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.repo.UserRoleAssignmentRepository;
import com.hospital.hms.support.PasswordHasher;
import com.hospital.hms.web.dto.RegisterPatientRequest;
import com.hospital.hms.web.error.ApiException;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PatientProfileRepository patientProfileRepository;

    public AuthService(UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PatientProfileRepository patientProfileRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    @Transactional
    public User registerPatient(RegisterPatientRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username taken");
        }
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setPhone(req.phone());
        u.setPasswordHash(PasswordHasher.hash(req.password()));
        u.setActive(true);
        u.setSuspended(false);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        userRepository.save(u);

        assignRole(u, RoleCode.PATIENT);

        PatientProfile profile = new PatientProfile();
        profile.setUser(u);
        profile.setMrn(generateMrn(u.getId()));
        profile.setBloodGroup(req.bloodGroup());
        patientProfileRepository.save(profile);

        return u;
    }

    private String generateMrn(Long userId) {
        return "MRN-IN-" + userId + "-" + (System.currentTimeMillis() % 100000L);
    }

    @Transactional
    public void assignRole(User user, RoleCode roleCode) {
        Role role = roleRepository.findByName(roleCode.name())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Role not seeded: " + roleCode));
        Optional<UserRoleAssignment> existing = userRoleAssignmentRepository
                .findById_UserIdAndId_RoleId(user.getId(), role.getId());
        if (existing.isPresent()) {
            UserRoleAssignment link = existing.get();
            if (link.getDeletedAt() != null) {
                link.setDeletedAt(null);
                link.setCreatedAt(Instant.now());
                userRoleAssignmentRepository.save(link);
            }
            return;
        }
        UserRoleAssignment link = new UserRoleAssignment();
        link.setId(new UserRoleAssignmentId(user.getId(), role.getId()));
        link.setUser(user);
        link.setRole(role);
        link.setCreatedAt(Instant.now());
        userRoleAssignmentRepository.save(link);
    }

    @Transactional(readOnly = true)
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!user.isActive() || user.isSuspended() || user.getRevokedAt() != null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account not permitted to login");
        }
        return user;
    }

    public void bindSession(HttpSession session, User user) {
        session.setAttribute(com.hospital.hms.support.HmsConstants.SESSION_USER_ID, user.getId());
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}
