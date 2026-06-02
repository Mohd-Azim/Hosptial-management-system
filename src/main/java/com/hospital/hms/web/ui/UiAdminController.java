package com.hospital.hms.web.ui;

import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.UserRepository;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/admin")
@RequiresMvcRole(RoleCode.ADMIN)
public class UiAdminController {

    private final UserRepository userRepository;

    public UiAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll().stream()
                .map(u -> new UserRow(u.getId(), u.getUsername(), u.getFullName(), u.isActive(), u.isSuspended()))
                .collect(Collectors.toList()));
        return "app/admin/users";
    }

    public record UserRow(Long id, String username, String fullName, boolean active, boolean suspended) {
    }
}
