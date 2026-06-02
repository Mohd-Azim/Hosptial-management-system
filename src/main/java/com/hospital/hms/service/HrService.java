package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.AttendanceLog;
import com.hospital.hms.domain.LeaveRequest;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.AttendanceDayStatus;
import com.hospital.hms.domain.enums.LeaveStatus;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.AttendanceLogRepository;
import com.hospital.hms.repo.LeaveRequestRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HrService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public HrService(AttendanceLogRepository attendanceLogRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    private static boolean isStaffRole(UserPrincipal actor) {
        return actor.hasAny(RoleCode.ADMIN, RoleCode.DOCTOR, RoleCode.NURSE, RoleCode.RECEPTION,
                RoleCode.PHARMACY, RoleCode.ASSISTANT, RoleCode.CANTEEN_STAFF, RoleCode.HR, RoleCode.PROCUREMENT);
    }

    private static void assertHrOrAdmin(UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.HR, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "HR or admin only");
        }
    }

    @Transactional
    public AttendanceLog checkIn(UserPrincipal actor, LocalDate day, String shiftCode, String workLocation,
            String remarks) {
        User staff = userRepository.findById(actor.userId()).orElseThrow();
        if (!isStaffRole(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Staff attendance only");
        }
        LocalDate d = day != null ? day : LocalDate.now();
        AttendanceLog log = attendanceLogRepository.findByStaff_IdAndLogDate(actor.userId(), d)
                .orElseGet(() -> newAttendance(staff, d));
        if (log.getCheckIn() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Already checked in");
        }
        log.setCheckIn(Instant.now());
        if (shiftCode != null) {
            log.setShiftCode(shiftCode);
        }
        if (workLocation != null) {
            log.setWorkLocation(workLocation);
        }
        if (remarks != null) {
            log.setRemarks(remarks);
        }
        return attendanceLogRepository.save(log);
    }

    @Transactional
    public AttendanceLog checkOut(UserPrincipal actor, LocalDate day) {
        if (!isStaffRole(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Staff attendance only");
        }
        LocalDate d = day != null ? day : LocalDate.now();
        AttendanceLog log = attendanceLogRepository.findByStaff_IdAndLogDate(actor.userId(), d)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No check-in for day"));
        if (log.getCheckOut() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Already checked out");
        }
        log.setCheckOut(Instant.now());
        return attendanceLogRepository.save(log);
    }

    private AttendanceLog newAttendance(User staff, LocalDate d) {
        AttendanceLog log = new AttendanceLog();
        log.setStaff(staff);
        log.setLogDate(d);
        log.setDayStatus(AttendanceDayStatus.PRESENT);
        log.setCreatedAt(Instant.now());
        return log;
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> myAttendance(UserPrincipal actor) {
        if (!isStaffRole(actor)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Staff only");
        }
        return attendanceLogRepository.findByStaff_IdOrderByLogDateDesc(actor.userId());
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> rosterForDay(UserPrincipal actor, LocalDate date) {
        assertHrOrAdmin(actor);
        LocalDate d = date != null ? date : LocalDate.now();
        return attendanceLogRepository.findByLogDateOrderByStaff_IdAsc(d);
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> rosterForStaff(UserPrincipal actor, Long staffUserId, LocalDate from, LocalDate to) {
        assertHrOrAdmin(actor);
        if (from == null || to == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "from and to required");
        }
        return attendanceLogRepository.findByStaff_IdAndLogDateBetweenOrderByLogDateAsc(staffUserId, from, to);
    }

    @Transactional
    public AttendanceLog hrUpdateDay(UserPrincipal actor, Long logId, AttendanceDayStatus status,
            String remarks, String shiftCode, String workLocation) {
        assertHrOrAdmin(actor);
        AttendanceLog log = attendanceLogRepository.findById(logId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance log not found"));
        if (status != null) {
            log.setDayStatus(status);
        }
        if (remarks != null) {
            log.setRemarks(remarks);
        }
        if (shiftCode != null) {
            log.setShiftCode(shiftCode);
        }
        if (workLocation != null) {
            log.setWorkLocation(workLocation);
        }
        return attendanceLogRepository.save(log);
    }

    @Transactional
    public AttendanceLog approveAttendance(UserPrincipal actor, Long logId) {
        assertHrOrAdmin(actor);
        AttendanceLog log = attendanceLogRepository.findById(logId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance log not found"));
        User approver = userRepository.findById(actor.userId()).orElseThrow();
        log.setApprovedBy(approver);
        log.setApprovedAt(Instant.now());
        return attendanceLogRepository.save(log);
    }

    @Transactional
    public LeaveRequest applyLeave(UserPrincipal actor, LocalDate start, LocalDate end, String reason) {
        User staff = userRepository.findById(actor.userId()).orElseThrow();
        if (actor.roles().contains(RoleCode.PATIENT)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Patients do not apply internal leave");
        }
        LeaveRequest lr = new LeaveRequest();
        lr.setStaff(staff);
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setReason(reason);
        lr.setStatus(LeaveStatus.PENDING);
        lr.setCreatedAt(Instant.now());
        lr.setUpdatedAt(Instant.now());
        return leaveRequestRepository.save(lr);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> myLeaves(UserPrincipal actor) {
        return leaveRequestRepository.findByStaff_IdOrderByCreatedAtDesc(actor.userId());
    }

    @Transactional
    public LeaveRequest decideLeave(UserPrincipal actor, Long leaveId, boolean approve) {
        if (!actor.hasAny(RoleCode.ADMIN, RoleCode.HR)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin or HR only");
        }
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Already decided");
        }
        lr.setStatus(approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        lr.setDecidedBy(userRepository.findById(actor.userId()).orElseThrow());
        lr.setDecidedAt(Instant.now());
        lr.setUpdatedAt(Instant.now());
        return leaveRequestRepository.save(lr);
    }
}
