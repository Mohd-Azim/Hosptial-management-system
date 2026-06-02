package com.hospital.hms.web.dto;

import java.time.LocalDate;

public record AttendanceClockRequest(
        LocalDate day,
        String shiftCode,
        String workLocation,
        String remarks) {
}
