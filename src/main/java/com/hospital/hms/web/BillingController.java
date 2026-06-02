package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.BillingService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.PayBillRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @RequiresRole({RoleCode.PATIENT, RoleCode.RECEPTION, RoleCode.PHARMACY, RoleCode.CANTEEN_STAFF,
            RoleCode.ADMIN, RoleCode.NURSE})
    @PostMapping("/bills/{billId}/pay")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable Long billId,
            @RequestBody PayBillRequest body,
            HttpServletRequest request) {
        var principal = WebRequests.principal(request);
        var payment = billingService.payBill(principal, billId, body.mode(), body.amount(),
                body.externalRef(), body.receivedByUserId());
        return ResponseEntity.ok(Map.of("paymentId", payment.getId(), "billId", billId));
    }
}
