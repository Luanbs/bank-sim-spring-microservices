package com.banksim.gateway.logging;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public int getOrder() {
        return -1000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = Optional
                .ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        var mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER, correlationId)
                .build();

        exchange.getResponse().getHeaders().set(HEADER, correlationId);

        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange)
                .contextWrite(ctx -> ctx.put(MDC_KEY, correlationId))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        signal.getContextView()
                                .<String>getOrEmpty(MDC_KEY)
                                .ifPresent(id -> MDC.put(MDC_KEY, id));
                    }
                })
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }
}
