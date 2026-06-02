package com.hospital.hms.web;

import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.PatientSearchService;
import com.hospital.hms.web.dto.PatientSearchHit;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class PatientSearchController {

    private final PatientSearchService patientSearchService;

    public PatientSearchController(PatientSearchService patientSearchService) {
        this.patientSearchService = patientSearchService;
    }

    /**
     * Prefix search with Redis → DB (bucket + index). Debounce on the client.
     */
    @RequiresRole({RoleCode.RECEPTION, RoleCode.PHARMACY, RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.ASSISTANT})
    @GetMapping("/patients")
    public List<PatientSearchHit> patients(
            @RequestParam("q") String q,
            @RequestParam(required = false) Integer limit) {
        return patientSearchService.searchPatients(q, limit);
    }
}
