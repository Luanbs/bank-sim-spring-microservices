package com.banksim.auth_service.dto.response;

import java.util.UUID;

public record CreateUserResponse(UUID id, UUID accountId) {
}
