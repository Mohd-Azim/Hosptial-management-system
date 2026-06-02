package com.hospital.hms.web.dto;

import java.util.List;

public record CompleteVisitRequest(
        Long doctorUserId,
        Long assistantUserId,
        boolean pharmacyOptIn,
        String instructions,
        List<PrescriptionLineRequest> lines) {
}
