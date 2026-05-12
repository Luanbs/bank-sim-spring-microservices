package com.banksim.account_service.dto.response.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionItemResponse(
        UUID id,
        String title,
        String category,
        BigDecimal amount,
        Instant date,
        String type
) {
}
