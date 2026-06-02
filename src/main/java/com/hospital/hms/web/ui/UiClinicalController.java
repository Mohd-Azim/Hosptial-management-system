package com.hospital.hms.web.ui;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.config.RequiresMvcRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.UserRoleAssignmentRepository;
import com.hospital.hms.repo.VisitRepository;
import com.hospital.hms.service.ClinicalService;
import com.hospital.hms.support.HmsConstants;
import com.hospital.hms.web.dto.PrescriptionLineRequest;
import com.hospital.hms.web.error.ApiException;
import com.hospital.hms.web.ui.dto.ClinicalTodayRow;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/clinical")
@RequiresMvcRole({RoleCode.DOCTOR, RoleCode.ASSISTANT})
public class UiClinicalController {

    private final ClinicalService clinicalService;
    private final VisitRepository visitRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public UiClinicalController(ClinicalService clinicalService, VisitRepository visitRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository) {
        this.clinicalService = clinicalService;
        this.visitRepository = visitRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    @GetMapping("/today")
    public String today(Model model) {
        LocalDate d = LocalDate.now();
        var list = clinicalService.todayConfirmed(d);
        List<ClinicalTodayRow> rows = list.stream()
                .map(a -> new ClinicalTodayRow(a, visitRepository.findByAppointment_Id(a.getId()).orElse(null)))
                .toList();
        model.addAttribute("rows", rows);
        model.addAttribute("day", d);
        model.addAttribute("doctors", userRoleAssignmentRepository.findActiveUsersByRoleName(RoleCode.DOCTOR.name()));
        model.addAttribute(
                "assistants", userRoleAssignmentRepository.findActiveUsersByRoleName(RoleCode.ASSISTANT.name()));
        return "app/clinical/today";
    }

    @PostMapping("/check-in")
    public String checkIn(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long appointmentId,
            @RequestParam(required = false) Long doctorUserId,
            @RequestParam(required = false) Long assistantUserId,
            RedirectAttributes ra) {
        try {
            long doc = principal.roles().contains(RoleCode.DOCTOR) ? principal.userId() : doctorUserId != null ? doctorUserId : 0L;
            if (doc <= 0) {
                ra.addFlashAttribute("flashError", "Doctor is required.");
                return "redirect:/app/clinical/today";
            }
            clinicalService.checkIn(appointmentId, doc, assistantUserId, principal);
            ra.addFlashAttribute("flashSuccess", "Patient checked in.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/clinical/today";
    }

    @PostMapping("/complete-visit")
    public String completeVisit(
            @RequestAttribute(HmsConstants.REQUEST_USER) UserPrincipal principal,
            @RequestParam Long appointmentId,
            @RequestParam(required = false) Long doctorUserId,
            @RequestParam(required = false) Long assistantUserId,
            @RequestParam(defaultValue = "false") boolean pharmacyOptIn,
            @RequestParam(required = false) String instructions,
            HttpServletRequest request,
            RedirectAttributes ra) {
        try {
            long doc = principal.roles().contains(RoleCode.DOCTOR) ? principal.userId() : doctorUserId != null ? doctorUserId : 0L;
            if (doc <= 0) {
                ra.addFlashAttribute("flashError", "Doctor is required.");
                return "redirect:/app/clinical/today";
            }
            List<PrescriptionLineRequest> lines = parsePrescriptionLines(request);
            if (lines.isEmpty()) {
                ra.addFlashAttribute("flashError", "Add at least one medicine line.");
                return "redirect:/app/clinical/today";
            }
            String instr = instructions != null ? instructions : "";
            clinicalService.completeVisit(appointmentId, doc, assistantUserId, pharmacyOptIn, instr, lines, principal);
            ra.addFlashAttribute("flashSuccess", "Visit completed and prescription saved.");
        } catch (ApiException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/app/clinical/today";
    }

    private static List<PrescriptionLineRequest> parsePrescriptionLines(HttpServletRequest request) {
        List<PrescriptionLineRequest> out = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String name = request.getParameter("med_" + i + "_name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String dosage = nullToEmpty(request.getParameter("med_" + i + "_dosage"));
            String freq = nullToEmpty(request.getParameter("med_" + i + "_freq"));
            int days = parseInt(request.getParameter("med_" + i + "_days"), 7);
            BigDecimal price = parseBd(request.getParameter("med_" + i + "_price"), BigDecimal.ZERO);
            out.add(new PrescriptionLineRequest(name.trim(), dosage, freq, days, price));
        }
        return out;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static BigDecimal parseBd(String s, BigDecimal def) {
        if (s == null || s.isBlank()) {
            return def;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
