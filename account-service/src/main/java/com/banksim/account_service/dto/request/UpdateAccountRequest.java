package com.banksim.account_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAccountRequest(@NotBlank String ownerName) {
}
