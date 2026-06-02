package com.hospital.hms.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StaffSalaryAssignmentRequest(
        Long staffUserId,
        Long payrollComponentId,
        BigDecimal amountMonthly,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
