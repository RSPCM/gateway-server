package com.example.gatewayservice.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(config.headerName);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = authorizationHeader.substring(7);

            try {
                UUID id = jwtService.extractUsername(token);

                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r.header("X-Authenticated-User", String.valueOf(id)))
                        .request(r -> r.header("X-Authenticated-Roles", jwtService.extractRoles(token)))
                        .build();

                return chain.filter(mutatedExchange);
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                byte[] bytes = "{\"error\": \"Invalid or expired token\"}".getBytes();
                DataBuffer wrap = exchange.getResponse().bufferFactory().wrap(bytes);

                return exchange.getResponse().writeWith(Mono.just(wrap));
            }
        };
    }


    @Getter
    @Setter
    public static class Config {
        private String headerName;
    }
}
