package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.WasteComplianceService;
import com.hospital.hms.support.HmsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/compliance")
@RequiresMvcRole({RoleCode.ADMIN, RoleCode.NURSE})
public class UiComplianceController {

    private final WasteComplianceService wasteComplianceService;

    public UiComplianceController(WasteComplianceService wasteComplianceService) {
        this.wasteComplianceService = wasteComplianceService;
    }

    @GetMapping("/waste")
    public String waste(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal, Model model) {
        model.addAttribute("logs", wasteComplianceService.list(principal));
        return "app/compliance/waste";
    }
}
