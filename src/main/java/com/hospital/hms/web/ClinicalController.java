package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.ClinicalService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.CheckInRequest;
import com.hospital.hms.web.dto.CompleteVisitRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinical")
public class ClinicalController {

    private final ClinicalService clinicalService;

    public ClinicalController(ClinicalService clinicalService) {
        this.clinicalService = clinicalService;
    }

    @RequiresRole({RoleCode.DOCTOR, RoleCode.ASSISTANT, RoleCode.ADMIN})
    @GetMapping("/appointments/today")
    public ResponseEntity<List<Map<String, Object>>> today(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        var list = clinicalService.todayConfirmed(d).stream()
                .map(a -> Map.<String, Object>of(
                        "appointmentId", a.getId(),
                        "patientUserId", a.getPatient().getId(),
                        "scheduledAt", a.getScheduledAt().toString(),
                        "notes", a.getNotes() != null ? a.getNotes() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.DOCTOR, RoleCode.ASSISTANT, RoleCode.ADMIN})
    @PostMapping("/appointments/{id}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Long id,
            @RequestBody CheckInRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var visit = clinicalService.checkIn(id, body.doctorUserId(), body.assistantUserId(), actor);
        return ResponseEntity.ok(Map.of("visitId", visit.getId(), "status", visit.getStatus().name()));
    }

    @RequiresRole({RoleCode.DOCTOR, RoleCode.ASSISTANT, RoleCode.ADMIN})
    @PostMapping("/appointments/{id}/complete")
    public ResponseEntity<Map<String, Object>> complete(@PathVariable Long id,
            @RequestBody CompleteVisitRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var rx = clinicalService.completeVisit(id, body.doctorUserId(), body.assistantUserId(),
                body.pharmacyOptIn(), body.instructions(),
                body.lines() != null ? body.lines() : List.of(), actor);
        return ResponseEntity.ok(Map.of("prescriptionId", rx.getId(), "printedAt",
                rx.getPrintedAt() != null ? rx.getPrintedAt().toString() : ""));
    }
}
