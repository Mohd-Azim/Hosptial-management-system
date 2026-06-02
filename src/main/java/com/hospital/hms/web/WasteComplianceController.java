package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.WasteComplianceService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.MedicalWasteLogRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compliance/waste")
public class WasteComplianceController {

    private final WasteComplianceService wasteComplianceService;

    public WasteComplianceController(WasteComplianceService wasteComplianceService) {
        this.wasteComplianceService = wasteComplianceService;
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.NURSE, RoleCode.PHARMACY, RoleCode.DOCTOR})
    @PostMapping
    public ResponseEntity<Map<String, Object>> log(@RequestBody MedicalWasteLogRequest body,
            HttpServletRequest request) {
        var row = wasteComplianceService.log(WebRequests.principal(request), body);
        return ResponseEntity.ok(Map.of("id", row.getId(), "recordedAt", row.getRecordedAt().toString()));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.NURSE})
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(HttpServletRequest request) {
        var list = wasteComplianceService.list(WebRequests.principal(request)).stream()
                .map(w -> Map.<String, Object>of(
                        "id", w.getId(),
                        "categoryCode", w.getCategoryCode(),
                        "quantity", w.getQuantityValue().toString(),
                        "unit", w.getQuantityUnit(),
                        "manifest", w.getCbwtfManifestNumber() != null ? w.getCbwtfManifestNumber() : "",
                        "recordedAt", w.getRecordedAt().toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
