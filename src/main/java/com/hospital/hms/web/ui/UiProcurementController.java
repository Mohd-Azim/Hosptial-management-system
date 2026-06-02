package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.VendorService;
import com.hospital.hms.support.HmsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/procurement")
@RequiresMvcRole(RoleCode.PROCUREMENT)
public class UiProcurementController {

    private final VendorService vendorService;

    public UiProcurementController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping("/vendors")
    public String vendors(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal actor, Model model) {
        model.addAttribute("vendors", vendorService.list(actor));
        return "app/procurement/vendors";
    }
}
