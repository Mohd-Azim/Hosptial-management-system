package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.MiscExpenseService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.MiscExpenseRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/misc-expenses")
public class FinanceMiscController {

    private final MiscExpenseService miscExpenseService;

    public FinanceMiscController(MiscExpenseService miscExpenseService) {
        this.miscExpenseService = miscExpenseService;
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody MiscExpenseRequest body,
            HttpServletRequest request) {
        var e = miscExpenseService.record(WebRequests.principal(request), body);
        return ResponseEntity.ok(Map.of(
                "id", e.getId(),
                "amount", e.getAmount(),
                "expenseDate", e.getExpenseDate().toString()));
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        var list = miscExpenseService.list(WebRequests.principal(request), from, to).stream()
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", e.getId());
                    row.put("category", e.getCategory());
                    row.put("amount", e.getAmount());
                    row.put("expenseDate", e.getExpenseDate().toString());
                    if (e.getVendor() != null) {
                        row.put("vendorId", e.getVendor().getId());
                    }
                    return row;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        miscExpenseService.softDelete(WebRequests.principal(request), id);
        return ResponseEntity.ok().build();
    }
}
