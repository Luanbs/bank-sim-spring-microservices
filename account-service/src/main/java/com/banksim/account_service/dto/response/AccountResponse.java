package com.banksim.account_service.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID id, UUID userId, String ownerName, BigDecimal balance, Instant createdAt) {
}
