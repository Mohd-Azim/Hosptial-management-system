package com.hospital.hms.web.error;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, String message, String code) {
}
