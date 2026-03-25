/*
package com.example.gatewayservice.config;

import com.example.gatewayservice.util.AuditLoggingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuditLoggingFilter auditLoggingFilter) {
        return builder.routes()
                .route("auth-service",
                        r -> r.path("/api/v1/auth/**")
                                .filters(f -> f.filter(auditLoggingFilter))
                                //.uri("http://localhost:8083"))
                                .uri("lb://AUTH-SERVICE"))
                .build();
    }
}
*/
