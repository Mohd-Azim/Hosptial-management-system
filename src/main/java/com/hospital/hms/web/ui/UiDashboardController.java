package com.hospital.hms.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app")
public class UiDashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "app/dashboard";
    }
}
