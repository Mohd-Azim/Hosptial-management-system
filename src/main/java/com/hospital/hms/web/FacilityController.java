package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.FacilityService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.AdmissionDeskRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.RECEPTION, RoleCode.NURSE})
    @GetMapping("/wards")
    public ResponseEntity<List<Map<String, Object>>> wards() {
        var list = facilityService.listWards().stream()
                .map(w -> Map.<String, Object>of(
                        "wardId", w.getId(),
                        "name", w.getName(),
                        "bedType", w.getBedType()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.RECEPTION, RoleCode.NURSE})
    @GetMapping("/wards/{wardId}/beds/available")
    public ResponseEntity<List<Map<String, Object>>> beds(@PathVariable Long wardId) {
        var list = facilityService.availableBeds(wardId).stream()
                .map(b -> Map.<String, Object>of("bedId", b.getId(), "code", b.getBedCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.RECEPTION, RoleCode.NURSE})
    @PostMapping("/admissions")
    public ResponseEntity<Map<String, Object>> admit(@RequestBody AdmissionDeskRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var adm = facilityService.admit(actor, body.patientUserId(), body.bedId(), body.admissionType(),
                body.offlinePaidImmediate());
        return ResponseEntity.ok(Map.of("admissionId", adm.getId(), "bedId", adm.getBed().getId()));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.RECEPTION, RoleCode.NURSE, RoleCode.DOCTOR})
    @PostMapping("/admissions/{id}/discharge")
    public ResponseEntity<Map<String, Object>> discharge(@PathVariable Long id, HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var adm = facilityService.discharge(actor, id);
        return ResponseEntity.ok(Map.of("admissionId", adm.getId(), "dischargedAt",
                adm.getDischargedAt() != null ? adm.getDischargedAt().toString() : ""));
    }
}
