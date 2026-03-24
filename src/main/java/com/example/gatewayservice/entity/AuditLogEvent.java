package com.example.gatewayservice.entity;

import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogEvent {
    private String requestId;

    private String httpMethod;

    private String requestUri;

    private String queryString;

    private String remoteAddress;

    private String userAgent;

    private Integer responseStatus;

    private Long durationMs;
}
