package com.banksim.account_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(@NotBlank String ownerName, @PositiveOrZero BigDecimal initialBalance) {
}
