package com.hospital.hms.web.dto;

import java.math.BigDecimal;

public record ReceptionBookRequest(
        Long patientUserId,
        String scheduledAtIso,
        BigDecimal consultationFee,
        String notes,
        String bookingChannel,
        boolean offlinePaidImmediate) {
}
