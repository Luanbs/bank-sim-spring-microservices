package com.banksim.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLinkedAccountRequest(
        @NotNull UUID userId,
        @NotBlank String ownerName,
        @NotBlank @Email String email
) {
}
