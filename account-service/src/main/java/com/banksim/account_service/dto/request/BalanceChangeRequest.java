package com.banksim.account_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BalanceChangeRequest(@NotNull @Positive BigDecimal amount) {
}
