package com.hospital.hms.web.dto;

/** Lightweight patient row for search — IDs + display strings only (suited for Redis + API). */
public record PatientSearchHit(Long userId, String fullName, String mrn) {}
