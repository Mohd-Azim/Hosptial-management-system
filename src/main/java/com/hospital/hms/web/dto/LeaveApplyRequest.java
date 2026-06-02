package com.hospital.hms.web.dto;

import java.time.LocalDate;

public record LeaveApplyRequest(LocalDate startDate, LocalDate endDate, String reason) {
}
