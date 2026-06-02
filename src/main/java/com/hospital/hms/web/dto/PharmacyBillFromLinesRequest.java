package com.hospital.hms.web.dto;

import java.util.List;

public record PharmacyBillFromLinesRequest(List<Long> fulfillmentIds) {
}
