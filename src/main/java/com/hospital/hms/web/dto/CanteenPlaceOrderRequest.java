package com.hospital.hms.web.dto;

import java.util.List;

public record CanteenPlaceOrderRequest(List<CanteenOrderLineDto> lines) {
}
