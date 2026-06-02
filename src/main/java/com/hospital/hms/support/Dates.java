package com.hospital.hms.support;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class Dates {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private Dates() {
    }

    public static Instant startOfDayIst(LocalDate d) {
        return d.atStartOfDay(IST).toInstant();
    }

    public static Instant endOfDayExclusiveIst(LocalDate d) {
        return d.plusDays(1).atStartOfDay(IST).toInstant();
    }
}
