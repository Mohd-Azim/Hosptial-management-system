package com.hospital.hms.web.ui;

import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.FacilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app/facility")
@RequiresMvcRole({RoleCode.RECEPTION, RoleCode.NURSE})
public class UiFacilityController {

    private final FacilityService facilityService;

    public UiFacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping("/wards")
    public String wards(Model model) {
        model.addAttribute("wards", facilityService.listWards());
        return "app/facility/wards";
    }
}
