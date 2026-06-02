package com.hospital.hms.web;

import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.PayrollService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.PayrollRunCreateRequest;
import com.hospital.hms.web.dto.StaffSalaryAssignmentRequest;
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
@RequestMapping("/api/hr/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @GetMapping("/runs")
    public ResponseEntity<List<Map<String, Object>>> runs(HttpServletRequest request) {
        var list = payrollService.listRuns(WebRequests.principal(request)).stream()
                .map(r -> Map.<String, Object>of(
                        "runId", r.getId(),
                        "year", r.getPeriodYear(),
                        "month", r.getPeriodMonth(),
                        "status", r.getStatus().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @PostMapping("/runs")
    public ResponseEntity<Map<String, Object>> createRun(@RequestBody PayrollRunCreateRequest body,
            HttpServletRequest request) {
        var r = payrollService.createDraftRun(WebRequests.principal(request), body.year(), body.month());
        return ResponseEntity.ok(Map.of("runId", r.getId(), "status", r.getStatus().name()));
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @PostMapping("/runs/{runId}/finalize")
    public ResponseEntity<Map<String, Object>> finalize(@PathVariable Long runId, HttpServletRequest request) {
        var r = payrollService.finalizeRun(WebRequests.principal(request), runId);
        return ResponseEntity.ok(Map.of("runId", r.getId(), "status", r.getStatus().name()));
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @GetMapping("/runs/{runId}/lines")
    public ResponseEntity<List<Map<String, Object>>> lines(@PathVariable Long runId, HttpServletRequest request) {
        var list = payrollService.linesForRun(WebRequests.principal(request), runId).stream()
                .map(l -> Map.<String, Object>of(
                        "staffUserId", l.getStaff().getId(),
                        "componentCode", l.getPayrollComponent().getCode(),
                        "componentKind", l.getPayrollComponent().getComponentKind().name(),
                        "amount", l.getAmount()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @GetMapping("/components")
    public ResponseEntity<List<Map<String, Object>>> components(HttpServletRequest request) {
        var list = payrollService.listComponents(WebRequests.principal(request)).stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "code", c.getCode(),
                        "name", c.getName(),
                        "kind", c.getComponentKind().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @PostMapping("/assignments")
    public ResponseEntity<Map<String, Object>> assign(@RequestBody StaffSalaryAssignmentRequest body,
            HttpServletRequest request) {
        var s = payrollService.assignComponent(WebRequests.principal(request), body);
        return ResponseEntity.ok(Map.of("assignmentId", s.getId()));
    }

    @RequiresRole({RoleCode.HR, RoleCode.ADMIN})
    @GetMapping("/assignments/staff/{staffUserId}")
    public ResponseEntity<List<Map<String, Object>>> staffAssignments(@PathVariable Long staffUserId,
            HttpServletRequest request) {
        var list = payrollService.listAssignmentsForStaff(WebRequests.principal(request), staffUserId).stream()
                .map(a -> Map.<String, Object>of(
                        "assignmentId", a.getId(),
                        "componentId", a.getPayrollComponent().getId(),
                        "componentCode", a.getPayrollComponent().getCode(),
                        "amountMonthly", a.getAmountMonthly(),
                        "effectiveFrom", a.getEffectiveFrom().toString(),
                        "effectiveTo", a.getEffectiveTo() != null ? a.getEffectiveTo().toString() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
