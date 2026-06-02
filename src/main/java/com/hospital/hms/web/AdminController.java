package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.AdminUserService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.AssignRoleRequest;
import com.hospital.hms.web.dto.SuspendUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @RequiresRole(RoleCode.ADMIN)
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> users() {
        var list = adminUserService.listUsers().stream()
                .map(u -> Map.<String, Object>of(
                        "userId", u.getId(),
                        "username", u.getUsername(),
                        "fullName", u.getFullName(),
                        "active", u.isActive(),
                        "suspended", u.isSuspended(),
                        "revokedAt", u.getRevokedAt() != null ? u.getRevokedAt().toString() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole(RoleCode.ADMIN)
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long id, @RequestBody SuspendUserRequest body) {
        adminUserService.suspend(id, body.suspended());
        return ResponseEntity.ok().build();
    }

    @RequiresRole(RoleCode.ADMIN)
    @PostMapping("/users/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        adminUserService.revokeAccess(id);
        return ResponseEntity.ok().build();
    }

    @RequiresRole(RoleCode.ADMIN)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        adminUserService.softDeleteUser(id);
        return ResponseEntity.ok().build();
    }

    @RequiresRole(RoleCode.ADMIN)
    @PostMapping("/users/{id}/roles")
    public ResponseEntity<Void> assignRole(@PathVariable Long id, @RequestBody AssignRoleRequest body) {
        adminUserService.assignRole(id, body.role());
        return ResponseEntity.ok().build();
    }
}
