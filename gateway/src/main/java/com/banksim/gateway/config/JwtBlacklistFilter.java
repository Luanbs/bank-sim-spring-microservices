package com.banksim.gateway.config;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtBlacklistFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistFilter.class);

    private final ReactiveJwtDecoder jwtDecoder;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private final List<PathPattern> publicPatterns = List.of(
            new PathPatternParser().parse("/auth/**"),
            new PathPatternParser().parse("/.well-known/**")
    );

    public JwtBlacklistFilter(ReactiveJwtDecoder jwtDecoder,
                              ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.jwtDecoder = jwtDecoder;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public int getOrder() {
        return -900;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    String jti = jwt.getId();
                    if (jti == null) {
                        return Mono.just(false);
                    }
                    return reactiveRedisTemplate.hasKey("blacklist:" + jti);
                })
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.warn("JTI in blacklist. Path: {}", exchange.getRequest().getPath());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Token/Blacklist validation error: {}", e.getMessage());

                    if (isPrivateRoute(exchange)) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    private boolean isPrivateRoute(ServerWebExchange exchange) {
        PathContainer path = exchange.getRequest().getPath().pathWithinApplication();
        return publicPatterns.stream()
                .noneMatch(pattern -> pattern.matches(path));
    }
}