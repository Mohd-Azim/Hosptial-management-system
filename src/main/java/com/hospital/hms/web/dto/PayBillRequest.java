package com.hospital.hms.web.dto;

import com.hospital.hms.domain.enums.PaymentMode;
import java.math.BigDecimal;

public record PayBillRequest(PaymentMode mode, BigDecimal amount, String externalRef, Long receivedByUserId) {
}
