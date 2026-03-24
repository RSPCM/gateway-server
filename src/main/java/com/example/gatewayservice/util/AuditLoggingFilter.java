package com.example.gatewayservice.util;

import com.example.gatewayservice.service.RequestAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLoggingFilter implements GlobalFilter, Ordered {
    private final RequestAuditLogService auditLogService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String requestId = UUID.randomUUID().toString();

        // mutatedExchange used everywhere
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-Request-ID", requestId))
                .build();

        ServerHttpRequest request = mutatedExchange.getRequest();

        return chain.filter(mutatedExchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = mutatedExchange.getResponse().getStatusCode() != null
                            ? mutatedExchange.getResponse().getStatusCode().value()
                            : 0;

                    auditLogService.log(
                            requestId,
                            request.getMethod().name(),
                            request.getURI().getPath(),           // requestUri
                            request.getURI().getQuery(),          // queryString — was missing
                            resolveClientIp(request),             // remoteAddress
                            request.getHeaders().getFirst("User-Agent"), // userAgent
                            status,                               // responseStatus
                            duration                              // durationMs
                    );
                })
        );
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}