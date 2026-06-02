package com.hospital.hms.web.dto;

import java.math.BigDecimal;

public record MedicalWasteLogRequest(
        String categoryCode,
        String description,
        BigDecimal quantityValue,
        String quantityUnit,
        String segregationStatus,
        String storageLocation,
        boolean handedOverToAuthorisedAgent,
        String cbwtfManifestNumber,
        String incidentNotes) {
}
