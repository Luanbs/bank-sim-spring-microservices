package com.banksim.auth_service.service;

import com.banksim.auth_service.dto.GeneratedToken;
import com.banksim.auth_service.entity.User;
import com.banksim.auth_service.security.config.JwtProps;
import com.banksim.auth_service.security.role.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String SERVICE_TOKEN_CACHE_KEY = "service:auth-service:token";

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private JwtProps jwtProps;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void generateShouldReturnBearerTokenWithConfiguredExpiration() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("luan");
        user.setRoles(Set.of(Role.USER));

        when(jwtProps.issuer()).thenReturn("http://localhost:8081");
        when(jwtProps.ttlSeconds()).thenReturn(1800L);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtToken("access-token", Instant.now().plusSeconds(1800), "id-1"));

        GeneratedToken token = tokenService.generate(user);

        assertEquals("access-token", token.accessToken());
        assertEquals("Bearer", token.tokenType());
        assertEquals(1800L, token.expiresIn());
    }

    @Test
    void generateServiceTokenShouldReuseCachedToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(SERVICE_TOKEN_CACHE_KEY)).thenReturn("cached-token");

        String token = tokenService.generateServiceToken();

        assertEquals("cached-token", token);
        verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        verify(jwtDecoder, never()).decode(any(String.class));
    }

    @Test
    void generateServiceTokenShouldCacheTokenWithSafeTtlWhenPossible() {
        Instant expiresAt = Instant.now().plusSeconds(300);

        when(jwtProps.issuer()).thenReturn("http://localhost:8081");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(SERVICE_TOKEN_CACHE_KEY)).thenReturn(null);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtToken("service-token", expiresAt, "jti-service"));
        when(jwtDecoder.decode("service-token")).thenReturn(jwtToken("service-token", expiresAt, "jti-service"));

        String token = tokenService.generateServiceToken();

        assertEquals("service-token", token);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(eq(SERVICE_TOKEN_CACHE_KEY), eq("service-token"), ttlCaptor.capture());
        assertTrue(ttlCaptor.getValue().toSeconds() > 0);
        assertTrue(ttlCaptor.getValue().toSeconds() <= 270);
    }

    @Test
    void generateServiceTokenShouldCacheWithoutDurationWhenSafeTtlIsNotPositive() {
        Instant expiresAt = Instant.now().plusSeconds(20);

        when(jwtProps.issuer()).thenReturn("http://localhost:8081");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(SERVICE_TOKEN_CACHE_KEY)).thenReturn(null);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtToken("service-token", expiresAt, "jti-service"));
        when(jwtDecoder.decode("service-token")).thenReturn(jwtToken("service-token", expiresAt, "jti-service"));

        String token = tokenService.generateServiceToken();

        assertEquals("service-token", token);
        verify(valueOperations).set(SERVICE_TOKEN_CACHE_KEY, "service-token");
    }

    @Test
    void blacklistShouldStoreJtiUntilTokenExpiration() {
        Instant expiresAt = Instant.now().plusSeconds(600);
        Jwt jwt = jwtToken("access-token", expiresAt, "jti-123");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenService.blacklist(jwt);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(eq(TokenService.BLACKLIST_KEY_PREFIX + "jti-123"), eq("1"), ttlCaptor.capture());
        assertTrue(ttlCaptor.getValue().toSeconds() > 0);
    }

    @Test
    void blacklistShouldIgnoreTokenWithoutJti() {
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .claim("sub", "user")
                .build();

        tokenService.blacklist(jwt);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void blacklistShouldIgnoreExpiredToken() {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("access-token")
                .header("alg", "none")
                .issuedAt(now.minusSeconds(100))
                .expiresAt(now.minusSeconds(10))
                .claim("sub", "user")
                .jti("jti-expired")
                .build();

        tokenService.blacklist(jwt);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void generateShouldSendClaimsToEncoder() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("luan");
        user.setRoles(Set.of(Role.USER));

        when(jwtProps.issuer()).thenReturn("http://localhost:8081");
        when(jwtProps.ttlSeconds()).thenReturn(1800L);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtToken("access-token", Instant.now().plusSeconds(1800), "id-1"));

        tokenService.generate(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals("http://localhost:8081", claims.getIssuer().toString());
        assertEquals(user.getId().toString(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
    }

    private static Jwt jwtToken(String tokenValue, Instant expiresAt, String jti) {
        return Jwt.withTokenValue(tokenValue)
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .claim("sub", "internal-service")
                .jti(jti)
                .build();
    }
}
