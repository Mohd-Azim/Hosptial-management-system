package com.hospital.hms.web.dto;

import java.math.BigDecimal;

public record BookAppointmentRequest(String scheduledAtIso, BigDecimal consultationFee, String notes) {
}
