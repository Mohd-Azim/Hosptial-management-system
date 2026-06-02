package com.hospital.hms.web.dto;

import java.math.BigDecimal;

public record PrescriptionLineRequest(
        String medicineName,
        String dosage,
        String frequency,
        int durationDays,
        BigDecimal unitPrice) {
}
