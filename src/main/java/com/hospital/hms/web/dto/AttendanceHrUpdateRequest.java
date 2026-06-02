package com.hospital.hms.web.dto;

public record AttendanceHrUpdateRequest(
        String dayStatus,
        String remarks,
        String shiftCode,
        String workLocation) {
}
