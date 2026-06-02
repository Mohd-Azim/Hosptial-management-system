package com.hospital.hms.web.dto;

public record AdmissionDeskRequest(
        Long patientUserId,
        Long bedId,
        String admissionType,
        boolean offlinePaidImmediate) {
}
