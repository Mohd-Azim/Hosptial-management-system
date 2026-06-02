package com.hospital.hms.support;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class HmsWebTime {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private HmsWebTime() {
    }

    /** Parses HTML {@code datetime-local} value as IST wall time. */
    public static Instant parseDatetimeLocalIst(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Date/time required");
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(value.trim(), LOCAL);
            return ldt.atZone(IST).toInstant();
        } catch (DateTimeParseException e) {
            LocalDateTime ldt = LocalDateTime.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atZone(IST).toInstant();
        }
    }
}
