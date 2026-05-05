package com.banksim.auth_service.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProps(String issuer, long ttlSeconds) {}
