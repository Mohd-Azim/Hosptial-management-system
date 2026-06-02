package com.hospital.hms.web.dto;

public record RegisterPatientRequest(
        String username,
        String email,
        String password,
        String fullName,
        String phone,
        String bloodGroup) {
}
