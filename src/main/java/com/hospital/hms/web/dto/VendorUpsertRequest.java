package com.hospital.hms.web.dto;

public record VendorUpsertRequest(
        String partyType,
        String legalName,
        String tradeName,
        String gstin,
        String pan,
        String contactPerson,
        String phone,
        String email,
        String address,
        Integer paymentTermsDays,
        Boolean active) {
}
