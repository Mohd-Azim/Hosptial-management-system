package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.PayrollComponent;
import com.hospital.hms.domain.PayrollLine;
import com.hospital.hms.domain.PayrollRun;
import com.hospital.hms.domain.StaffSalaryAssignment;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.PayrollRunStatus;
import com.hospital.hms.domain.enums.RoleCode;
import com.hospital.hms.repo.PayrollComponentRepository;
import com.hospital.hms.repo.PayrollLineRepository;
import com.hospital.hms.repo.PayrollRunRepository;
import com.hospital.hms.repo.StaffSalaryAssignmentRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.web.dto.StaffSalaryAssignmentRequest;
import com.hospital.hms.web.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollLineRepository payrollLineRepository;
    private final StaffSalaryAssignmentRepository staffSalaryAssignmentRepository;
    private final PayrollComponentRepository payrollComponentRepository;
    private final UserRepository userRepository;

    public PayrollService(PayrollRunRepository payrollRunRepository,
            PayrollLineRepository payrollLineRepository,
            StaffSalaryAssignmentRepository staffSalaryAssignmentRepository,
            PayrollComponentRepository payrollComponentRepository,
            UserRepository userRepository) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollLineRepository = payrollLineRepository;
        this.staffSalaryAssignmentRepository = staffSalaryAssignmentRepository;
        this.payrollComponentRepository = payrollComponentRepository;
        this.userRepository = userRepository;
    }

    private static void assertHr(UserPrincipal actor) {
        if (!actor.hasAny(RoleCode.HR, RoleCode.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "HR or admin only");
        }
    }

    @Transactional(readOnly = true)
    public List<PayrollRun> listRuns(UserPrincipal actor) {
        assertHr(actor);
        return payrollRunRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StaffSalaryAssignment> listAssignmentsForStaff(UserPrincipal actor, Long staffUserId) {
        assertHr(actor);
        return staffSalaryAssignmentRepository.findByStaff_IdOrderByEffectiveFromDesc(staffUserId);
    }

    @Transactional
    public PayrollRun createDraftRun(UserPrincipal actor, int year, int month) {
        assertHr(actor);
        if (month < 1 || month > 12) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Month must be 1–12");
        }
        payrollRunRepository.findByPeriodYearAndPeriodMonth(year, month).ifPresent(r -> {
            throw new ApiException(HttpStatus.CONFLICT, "Payroll run already exists for period");
        });
        PayrollRun run = new PayrollRun();
        run.setPeriodYear(year);
        run.setPeriodMonth(month);
        run.setStatus(PayrollRunStatus.DRAFT);
        User creator = userRepository.findById(actor.userId()).orElseThrow();
        run.setCreatedBy(creator);
        run.setCreatedAt(Instant.now());
        run.setUpdatedAt(Instant.now());
        return payrollRunRepository.save(run);
    }

    @Transactional
    public StaffSalaryAssignment assignComponent(UserPrincipal actor, StaffSalaryAssignmentRequest req) {
        assertHr(actor);
        User staff = userRepository.findById(req.staffUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Staff user not found"));
        PayrollComponent comp = payrollComponentRepository.findById(req.payrollComponentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payroll component not found"));
        StaffSalaryAssignment s = new StaffSalaryAssignment();
        s.setStaff(staff);
        s.setPayrollComponent(comp);
        s.setAmountMonthly(req.amountMonthly());
        s.setEffectiveFrom(req.effectiveFrom());
        s.setEffectiveTo(req.effectiveTo());
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        return staffSalaryAssignmentRepository.save(s);
    }

    @Transactional
    public PayrollRun finalizeRun(UserPrincipal actor, Long runId) {
        assertHr(actor);
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payroll run not found"));
        if (run.getStatus() != PayrollRunStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "Run is not in DRAFT status");
        }
        if (payrollLineRepository.countByPayrollRun_Id(runId) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Run already has lines — regenerate not supported in MVP");
        }
        YearMonth ym = YearMonth.of(run.getPeriodYear(), run.getPeriodMonth());
        LocalDate periodStart = ym.atDay(1);
        LocalDate periodEnd = ym.atEndOfMonth();
        List<Long> staffIds = staffSalaryAssignmentRepository.findDistinctStaffIdsWithAssignmentOverlapping(
                periodStart, periodEnd);
        if (staffIds.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No salary assignments overlap this period");
        }
        Instant now = Instant.now();
        for (Long staffId : staffIds) {
            User staff = userRepository.findById(staffId).orElseThrow();
            List<StaffSalaryAssignment> assigns = staffSalaryAssignmentRepository.findActiveBetween(staffId,
                    periodStart, periodEnd);
            for (StaffSalaryAssignment a : assigns) {
                PayrollLine line = new PayrollLine();
                line.setPayrollRun(run);
                line.setStaff(staff);
                line.setPayrollComponent(a.getPayrollComponent());
                line.setAmount(a.getAmountMonthly());
                line.setCreatedAt(now);
                payrollLineRepository.save(line);
            }
        }
        run.setStatus(PayrollRunStatus.FINALIZED);
        run.setFinalizedAt(now);
        run.setUpdatedAt(now);
        return payrollRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public List<PayrollLine> linesForRun(UserPrincipal actor, Long runId) {
        assertHr(actor);
        return payrollLineRepository.findByPayrollRun_IdOrderByStaff_IdAsc(runId);
    }

    @Transactional(readOnly = true)
    public List<PayrollComponent> listComponents(UserPrincipal actor) {
        assertHr(actor);
        return payrollComponentRepository.findAll();
    }
}
