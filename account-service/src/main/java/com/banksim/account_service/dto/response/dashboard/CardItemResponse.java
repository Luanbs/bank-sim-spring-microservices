package com.banksim.account_service.dto.response.dashboard;

import java.util.UUID;

public record CardItemResponse(
        UUID id,
        String type,
        String last4,
        String expiry,
        String brand
) {
}
