package com.hospital.hms.web;

import com.hospital.hms.domain.enums.AttendanceDayStatus;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.service.HrService;
import com.hospital.hms.config.RequiresRole;
import com.hospital.hms.web.dto.AttendanceClockRequest;
import com.hospital.hms.web.dto.AttendanceHrUpdateRequest;
import com.hospital.hms.web.dto.LeaveApplyRequest;
import com.hospital.hms.web.dto.LeaveDecideRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr")
public class HrController {

    private final HrService hrService;

    public HrController(HrService hrService) {
        this.hrService = hrService;
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
            RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    @PostMapping("/attendance/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(
            @RequestBody(required = false) AttendanceClockRequest body,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            HttpServletRequest request) {
        AttendanceClockRequest b = body != null ? body : new AttendanceClockRequest(null, null, null, null);
        LocalDate d = b.day() != null ? b.day() : day;
        var log = hrService.checkIn(WebRequests.principal(request), d, b.shiftCode(), b.workLocation(), b.remarks());
        return ResponseEntity.ok(Map.of(
                "logId", log.getId(),
                "checkIn", log.getCheckIn() != null ? log.getCheckIn().toString() : ""));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
            RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    @PostMapping("/attendance/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            HttpServletRequest request) {
        var log = hrService.checkOut(WebRequests.principal(request), day);
        return ResponseEntity.ok(Map.of(
                "logId", log.getId(),
                "checkOut", log.getCheckOut() != null ? log.getCheckOut().toString() : ""));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
            RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    @GetMapping("/attendance/me")
    public ResponseEntity<List<Map<String, Object>>> myAttendance(HttpServletRequest request) {
        var list = hrService.myAttendance(WebRequests.principal(request)).stream()
                .map(this::toAttendanceMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.HR})
    @GetMapping("/attendance/day")
    public ResponseEntity<List<Map<String, Object>>> rosterDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        var list = hrService.rosterForDay(WebRequests.principal(request), date).stream()
                .map(this::toAttendanceMapWithStaff)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.HR})
    @GetMapping("/attendance/staff/{staffUserId}")
    public ResponseEntity<List<Map<String, Object>>> rosterStaff(
            @PathVariable Long staffUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        var list = hrService.rosterForStaff(WebRequests.principal(request), staffUserId, from, to).stream()
                .map(this::toAttendanceMapWithStaff)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.HR})
    @PatchMapping("/attendance/logs/{logId}")
    public ResponseEntity<Map<String, Object>> hrPatchLog(@PathVariable Long logId,
            @RequestBody AttendanceHrUpdateRequest body,
            HttpServletRequest request) {
        AttendanceDayStatus st = null;
        if (body.dayStatus() != null && !body.dayStatus().isBlank()) {
            st = AttendanceDayStatus.valueOf(body.dayStatus());
        }
        var log = hrService.hrUpdateDay(WebRequests.principal(request), logId, st,
                body.remarks(), body.shiftCode(), body.workLocation());
        return ResponseEntity.ok(toAttendanceMapWithStaff(log));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.HR})
    @PostMapping("/attendance/logs/{logId}/approve")
    public ResponseEntity<Map<String, Object>> approveLog(@PathVariable Long logId,
            HttpServletRequest request) {
        var log = hrService.approveAttendance(WebRequests.principal(request), logId);
        return ResponseEntity.ok(toAttendanceMapWithStaff(log));
    }

    private Map<String, Object> toAttendanceMap(com.hospital.hms.domain.AttendanceLog a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("logId", a.getId());
        m.put("logDate", a.getLogDate().toString());
        m.put("checkIn", a.getCheckIn() != null ? a.getCheckIn().toString() : "");
        m.put("checkOut", a.getCheckOut() != null ? a.getCheckOut().toString() : "");
        m.put("shiftCode", a.getShiftCode() != null ? a.getShiftCode() : "");
        m.put("workLocation", a.getWorkLocation() != null ? a.getWorkLocation() : "");
        m.put("remarks", a.getRemarks() != null ? a.getRemarks() : "");
        m.put("dayStatus", a.getDayStatus().name());
        m.put("approvedAt", a.getApprovedAt() != null ? a.getApprovedAt().toString() : "");
        return m;
    }

    private Map<String, Object> toAttendanceMapWithStaff(com.hospital.hms.domain.AttendanceLog a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("logId", a.getId());
        m.put("staffUserId", a.getStaff().getId());
        m.put("staffUsername", a.getStaff().getUsername());
        m.put("logDate", a.getLogDate().toString());
        m.put("checkIn", a.getCheckIn() != null ? a.getCheckIn().toString() : "");
        m.put("checkOut", a.getCheckOut() != null ? a.getCheckOut().toString() : "");
        m.put("shiftCode", a.getShiftCode() != null ? a.getShiftCode() : "");
        m.put("workLocation", a.getWorkLocation() != null ? a.getWorkLocation() : "");
        m.put("remarks", a.getRemarks() != null ? a.getRemarks() : "");
        m.put("dayStatus", a.getDayStatus().name());
        m.put("approvedAt", a.getApprovedAt() != null ? a.getApprovedAt().toString() : "");
        return m;
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
            RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    @PostMapping("/leave")
    public ResponseEntity<Map<String, Object>> applyLeave(@RequestBody LeaveApplyRequest body,
            HttpServletRequest request) {
        var lr = hrService.applyLeave(WebRequests.principal(request), body.startDate(), body.endDate(), body.reason());
        return ResponseEntity.ok(Map.of("leaveId", lr.getId(), "status", lr.getStatus().name()));
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
            RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT})
    @GetMapping("/leave/me")
    public ResponseEntity<List<Map<String, Object>>> myLeaves(HttpServletRequest request) {
        var list = hrService.myLeaves(WebRequests.principal(request)).stream()
                .map(l -> Map.<String, Object>of(
                        "leaveId", l.getId(),
                        "start", l.getStartDate().toString(),
                        "end", l.getEndDate().toString(),
                        "status", l.getStatus().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @RequiresRole({RoleCode.ADMIN, RoleCode.HR})
    @PostMapping("/leave/{id}/decide")
    public ResponseEntity<Map<String, Object>> decide(@PathVariable Long id,
            @RequestBody LeaveDecideRequest body,
            HttpServletRequest request) {
        var lr = hrService.decideLeave(WebRequests.principal(request), id, body.approve());
        return ResponseEntity.ok(Map.of("leaveId", lr.getId(), "status", lr.getStatus().name()));
    }
}
