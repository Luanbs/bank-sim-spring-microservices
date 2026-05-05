package com.banksim.account_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InternalCreateAccountRequest(
        @NotNull UUID userId,
        @NotBlank String ownerName,
        @NotBlank @Email String email
) {
}
