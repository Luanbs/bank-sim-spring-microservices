package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.GeneratedToken;
import com.banksim.auth_service.entity.User;
import com.banksim.auth_service.security.config.JwtProps;
import com.banksim.auth_service.security.role.Role;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    private static final Long EXPIRE_TIME_SERVICE_TOKEN = 300L;
    public static final String BLACKLIST_KEY_PREFIX = "blacklist:";
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProps props;
    private static final String KEY = "service:auth-service:token";
    private final RedisTemplate<String, String> redisTemplate;


    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProps props, RedisTemplate<String, String> redisTemplate) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.props = props;
        this.redisTemplate = redisTemplate;
    }

    public GeneratedToken generate(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        var claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(props.ttlSeconds()))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .id(UUID.randomUUID().toString())
                .build();

        return new GeneratedToken(
                jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(),
                "Bearer",
                props.ttlSeconds()
        );
    }

    public String generateServiceToken() {
        String token = redisTemplate.opsForValue().get(KEY);

        if (token != null) {
            return token;
        }

        String tokenGenerated = getGeneratedServiceToken();
        Instant expiration = jwtDecoder.decode(tokenGenerated).getExpiresAt();
        long safeTtl = Duration.between(Instant.now(), expiration).getSeconds() - 30;

        if (safeTtl > 0) {
            redisTemplate.opsForValue().set(
                    KEY,
                    tokenGenerated,
                    Duration.ofSeconds(safeTtl)
            );
        } else {
            redisTemplate.opsForValue().set(KEY, tokenGenerated);
        }

        return tokenGenerated;
    }

    private String getGeneratedServiceToken() {
        Instant now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRE_TIME_SERVICE_TOKEN))
                .subject("internal-service")
                .claim("roles", List.of(Role.SERVICE.name()))
                .claim("type", "service")
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public void blacklist(Jwt jwt) {
        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        if (jti == null || expiresAt == null) {
            return;
        }

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }

        redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + jti, "1", ttl);
    }

}
