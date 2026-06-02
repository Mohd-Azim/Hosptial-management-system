package com.hospital.hms.web.dto;

import java.math.BigDecimal;

public record PharmacyPendingRow(
        Long fulfillmentId,
        Long lineId,
        String medicineName,
        BigDecimal unitPrice,
        Long patientUserId,
        String patientName
) {
    public String searchBlob() {
        return (medicineName + " " + patientName + " " + patientUserId + " " + lineId).toLowerCase();
    }
}
