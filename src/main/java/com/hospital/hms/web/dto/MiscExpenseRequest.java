package com.hospital.hms.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MiscExpenseRequest(
        String category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        Long vendorId) {
}
