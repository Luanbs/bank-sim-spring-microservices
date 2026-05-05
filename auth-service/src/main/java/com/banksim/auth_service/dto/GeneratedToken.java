package com.banksim.auth_service.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GeneratedToken(
        String accessToken,
        String tokenType,
        Long expiresIn
) {}
