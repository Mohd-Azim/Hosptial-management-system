package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.CanteenService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.CanteenPlaceOrderRequest;
import com.hospital.hms.web.dto.PayBillRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
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
@RequestMapping("/api/canteen")
public class CanteenController {

    private final CanteenService canteenService;

    public CanteenController(CanteenService canteenService) {
        this.canteenService = canteenService;
    }

    @RequiresRole({RoleCode.PATIENT, RoleCode.ADMIN, RoleCode.CANTEEN_STAFF, RoleCode.DOCTOR, RoleCode.NURSE})
    @GetMapping("/menu")
    public ResponseEntity<List<Map<String, Object>>> menu() {
        var list = canteenService.menu().stream()
                .map(i -> Map.<String, Object>of(
                        "id", i.getId(),
                        "name", i.getName(),
                        "price", i.getPrice(),
                        "available", i.isAvailable()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.PATIENT, RoleCode.ADMIN, RoleCode.CANTEEN_STAFF, RoleCode.DOCTOR, RoleCode.NURSE})
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> order(@RequestBody CanteenPlaceOrderRequest body,
            HttpServletRequest request) {
        var actor = WebRequests.principal(request);
        Map<Long, Integer> qty = new LinkedHashMap<>();
        if (body.lines() != null) {
            for (var line : body.lines()) {
                qty.put(line.menuItemId(), line.quantity());
            }
        }
        var order = canteenService.placeOrder(actor, qty);
        return ResponseEntity.ok(Map.of("orderId", order.getId(), "total", order.getTotalAmount(),
                "paymentStatus", order.getPaymentStatus()));
    }

    @RequiresRole({RoleCode.PATIENT, RoleCode.ADMIN, RoleCode.CANTEEN_STAFF, RoleCode.RECEPTION})
    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<Void> pay(@PathVariable Long id, @RequestBody PayBillRequest body,
            HttpServletRequest request) {
        var payer = WebRequests.principal(request);
        canteenService.payOrder(payer, id, body.mode(), body.externalRef(), body.receivedByUserId());
        return ResponseEntity.ok().build();
    }
}
