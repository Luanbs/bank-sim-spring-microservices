package com.banksim.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtBlacklistFilterTest {

    @Mock
    private ReactiveJwtDecoder jwtDecoder;

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private JwtBlacklistFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtBlacklistFilter(jwtDecoder, reactiveRedisTemplate);
    }

    @Test
    void shouldSkipValidationWhenAuthorizationHeaderIsMissing() {
        when(gatewayFilterChain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/account/me").build()
        );

        filter.filter(exchange, gatewayFilterChain).block();

        verify(gatewayFilterChain).filter(exchange);
        verify(jwtDecoder, never()).decode(any(String.class));
        verify(reactiveRedisTemplate, never()).hasKey(any(String.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsBlacklisted() {
        MockServerWebExchange exchange = exchangeWithBearer("/account/me", "token");
        when(jwtDecoder.decode("token")).thenReturn(Mono.just(jwt("jti-1")));
        when(reactiveRedisTemplate.hasKey("blacklist:jti-1")).thenReturn(Mono.just(true));

        filter.filter(exchange, gatewayFilterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(gatewayFilterChain, never()).filter(any());
    }

    @Test
    void shouldContinueWhenTokenIsNotBlacklisted() {
        when(gatewayFilterChain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchangeWithBearer("/account/me", "token");
        when(jwtDecoder.decode("token")).thenReturn(Mono.just(jwt("jti-1")));
        when(reactiveRedisTemplate.hasKey("blacklist:jti-1")).thenReturn(Mono.just(false));

        filter.filter(exchange, gatewayFilterChain).block();

        verify(gatewayFilterChain).filter(exchange);
    }

    @Test
    void shouldReturnUnauthorizedOnDecoderErrorForPrivateRoute() {
        MockServerWebExchange exchange = exchangeWithBearer("/account/me", "invalid");
        when(jwtDecoder.decode("invalid")).thenReturn(Mono.error(new RuntimeException("invalid token")));

        filter.filter(exchange, gatewayFilterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(gatewayFilterChain, never()).filter(any());
    }

    @Test
    void shouldAllowRequestOnDecoderErrorForPublicRoute() {
        when(gatewayFilterChain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchangeWithBearer("/auth/login", "invalid");
        when(jwtDecoder.decode("invalid")).thenReturn(Mono.error(new RuntimeException("invalid token")));

        filter.filter(exchange, gatewayFilterChain).block();

        verify(gatewayFilterChain).filter(exchange);
    }

    private static MockServerWebExchange exchangeWithBearer(String path, String token) {
        MockServerHttpRequest request = MockServerHttpRequest.get(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return MockServerWebExchange.from(request);
    }

    private static Jwt jwt(String jti) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user")
                .jti(jti)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
