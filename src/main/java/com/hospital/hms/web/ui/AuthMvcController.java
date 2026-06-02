package com.hospital.hms.web.ui;

import com.hospital.hms.service.AuthService;
import com.hospital.hms.web.error.ApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthMvcController {

    private final AuthService authService;

    public AuthMvcController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String redirect,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Session expired or access denied. Please sign in again.");
        }
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {
        try {
            var user = authService.login(username, password);
            authService.bindSession(session, user);
            String target = redirect;
            if (target == null || target.isBlank() || !target.startsWith("/") || target.startsWith("//")) {
                target = "/app/dashboard";
            }
            return "redirect:" + target;
        } catch (ApiException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            if (redirect != null) {
                model.addAttribute("redirect", redirect);
            }
            return "auth/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
