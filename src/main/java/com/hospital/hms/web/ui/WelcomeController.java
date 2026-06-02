package com.hospital.hms.web.ui;

import com.hospital.hms.support.HmsConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session != null && session.getAttribute(HmsConstants.SESSION_USER_ID) != null) {
            return "redirect:/app/dashboard";
        }
        // Show landing page for unauthenticated users
        return "landing";
    }
}
