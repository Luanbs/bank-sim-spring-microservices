package com.banksim.account_service.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsGoalItemResponse(
        UUID id,
        String name,
        BigDecimal currentAmount,
        BigDecimal targetAmount
) {
}
