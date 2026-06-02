package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.PharmacyService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.PharmacyBillFromLinesRequest;
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
@RequestMapping("/api/pharmacy")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @RequiresRole({RoleCode.PHARMACY, RoleCode.ADMIN})
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> pending() {
        var list = pharmacyService.pending().stream().map(f -> Map.<String, Object>of(
                "fulfillmentId", f.getId(),
                "prescriptionLineId", f.getPrescriptionLine().getId(),
                "medicine", f.getPrescriptionLine().getMedicineName(),
                "unitPrice", f.getPrescriptionLine().getUnitPrice(),
                "patientUserId", f.getPrescriptionLine().getPrescription().getVisit().getAppointment().getPatient().getId()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.PHARMACY, RoleCode.ADMIN})
    @PostMapping("/bills/from-fulfillments")
    public ResponseEntity<Map<String, Object>> createBill(@RequestBody PharmacyBillFromLinesRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        var bill = pharmacyService.createBillForFulfillments(actor, body.fulfillmentIds());
        return ResponseEntity.ok(Map.of("billId", bill.getId(), "total", bill.getTotalAmount()));
    }

    @RequiresRole({RoleCode.PHARMACY, RoleCode.ADMIN})
    @PostMapping("/fulfillments/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable Long id, HttpServletRequest request) {
        pharmacyService.declineFulfillment(WebRequests.principal(request), id);
        return ResponseEntity.ok().build();
    }

    @RequiresRole({RoleCode.PHARMACY, RoleCode.ADMIN})
    @PostMapping("/bills/{billId}/dispense")
    public ResponseEntity<Void> dispense(@PathVariable Long billId, HttpServletRequest request) {
        pharmacyService.dispenseBill(WebRequests.principal(request), billId);
        return ResponseEntity.ok().build();
    }
}
