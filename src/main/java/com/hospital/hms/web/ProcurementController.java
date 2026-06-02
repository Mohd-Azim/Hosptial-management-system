package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.VendorService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.VendorUpsertRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procurement/vendors")
public class ProcurementController {

    private final VendorService vendorService;

    public ProcurementController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @RequiresRole({RoleCode.PROCUREMENT, RoleCode.ADMIN})
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(HttpServletRequest request) {
        var list = vendorService.list(WebRequests.principal(request)).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.PROCUREMENT, RoleCode.ADMIN})
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody VendorUpsertRequest body,
            HttpServletRequest request) {
        var v = vendorService.create(WebRequests.principal(request), body);
        return ResponseEntity.ok(toMap(v));
    }

    @RequiresRole({RoleCode.PROCUREMENT, RoleCode.ADMIN})
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody VendorUpsertRequest body,
            HttpServletRequest request) {
        var v = vendorService.update(WebRequests.principal(request), id, body);
        return ResponseEntity.ok(toMap(v));
    }

    @RequiresRole({RoleCode.PROCUREMENT, RoleCode.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        vendorService.softDelete(WebRequests.principal(request), id);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toMap(com.hospital.hms.domain.Vendor v) {
        return Map.of(
                "id", v.getId(),
                "partyType", v.getPartyType().name(),
                "legalName", v.getLegalName(),
                "tradeName", v.getTradeName() != null ? v.getTradeName() : "",
                "gstin", v.getGstin() != null ? v.getGstin() : "",
                "phone", v.getPhone() != null ? v.getPhone() : "",
                "active", v.isActive());
    }
}
