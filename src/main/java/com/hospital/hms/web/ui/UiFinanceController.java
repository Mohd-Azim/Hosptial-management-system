package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.MiscExpenseService;
import com.hospital.hms.support.HmsConstants;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/app/finance")
@RequiresMvcRole({RoleCode.HR, RoleCode.ADMIN})
public class UiFinanceController {

    private final MiscExpenseService miscExpenseService;

    public UiFinanceController(MiscExpenseService miscExpenseService) {
        this.miscExpenseService = miscExpenseService;
    }

    @GetMapping("/misc")
    public String misc(@RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Model model) {
        model.addAttribute("expenses", miscExpenseService.list(principal, from, to));
        return "app/finance/misc";
    }
}
