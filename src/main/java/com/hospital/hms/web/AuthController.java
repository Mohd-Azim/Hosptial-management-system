package com.hospital.hms.web;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.User;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.service.AuthService;
import com.hospital.hms.web.dto.LoginRequest;
import com.hospital.hms.web.dto.RegisterPatientRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterPatientRequest req, HttpSession session) {
        User u = authService.registerPatient(req);
        authService.bindSession(session, u);
        return ResponseEntity.ok(Map.of(
                "userId", u.getId(),
                "username", u.getUsername(),
                "message", "Registered and logged in"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req, HttpSession session) {
        User u = authService.login(req.username(), req.password());
        authService.bindSession(session, u);
        return ResponseEntity.ok(Map.of(
                "userId", u.getId(),
                "username", u.getUsername(),
                "message", "Logged in"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        UserPrincipal p = WebRequests.principal(request);
        User u = userRepository.findById(p.userId()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "userId", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFullName(),
                "email", u.getEmail(),
                "roles", p.roles().stream().map(Enum::name).collect(Collectors.toSet())));
    }
}
