package com.hospital.hms.web.ui;

import com.hospital.hms.service.AuthService;
import com.hospital.hms.web.dto.RegisterPatientRequest;
import com.hospital.hms.web.error.ApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UiRegisterController {

    private final AuthService authService;

    public UiRegisterController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bloodGroup,
            HttpSession session,
            Model model) {
        try {
            var req = new RegisterPatientRequest(username, email, password, fullName, phone, bloodGroup);
            var user = authService.registerPatient(req);
            authService.bindSession(session, user);
            return "redirect:/app/patient/home";
        } catch (ApiException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("fullName", fullName);
            return "auth/register";
        }
    }
}
