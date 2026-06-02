package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.HrService;
import com.hospital.hms.service.PayrollService;
import com.hospital.hms.support.HmsConstants;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/app/hr")
public class UiHrController {

    private final HrService hrService;
    private final PayrollService payrollService;

    public UiHrController(HrService hrService, PayrollService payrollService) {
        this.hrService = hrService;
        this.payrollService = payrollService;
    }

    @GetMapping("/me")
    @RequiresMvcRole({RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION, RoleCode.PHARMACY,
            RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    public String myAttendance(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal, Model model) {
        model.addAttribute("logs", hrService.myAttendance(principal));
        return "app/hr/me";
    }

    @GetMapping("/roster")
    @RequiresMvcRole({RoleCode.HR, RoleCode.ADMIN})
    public String rosterDay(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam(required = false) LocalDate date,
            Model model) {
        LocalDate d = date != null ? date : LocalDate.now();
        model.addAttribute("day", d);
        model.addAttribute("logs", hrService.rosterForDay(principal, d));
        return "app/hr/roster";
    }

    @GetMapping("/payroll")
    @RequiresMvcRole({RoleCode.HR, RoleCode.ADMIN})
    public String payroll(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal, Model model) {
        model.addAttribute("components", payrollService.listComponents(principal));
        model.addAttribute("runs", payrollService.listRuns(principal));
        return "app/hr/payroll";
    }
}
