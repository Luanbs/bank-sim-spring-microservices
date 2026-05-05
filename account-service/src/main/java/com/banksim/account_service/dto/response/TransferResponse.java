package com.banksim.account_service.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        String recipientName,
        String recipientEmail,
        BigDecimal amount,
        BigDecimal currentBalance,
        Instant transferredAt
) {
}
