package com.example.gatewayservice.service;

import com.example.gatewayservice.entity.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestAuditLogService {
    private static final String EXECUTOR_BEAN_NAME = "NotificationTaskExecutor";
    private static final int REQUEST_ID_MAX_LENGTH = 100;
    private static final int METHOD_MAX_LENGTH = 16;
    private static final int URI_MAX_LENGTH = 512;
    private static final int QUERY_MAX_LENGTH = 1024;
    private static final int ADDRESS_MAX_LENGTH = 64;
    private static final int USER_AGENT_MAX_LENGTH = 256;

    private final AuditEventPublisher auditEventPublisher;

    @Async(EXECUTOR_BEAN_NAME)
    public void log(String requestId,
                    String method,
                    String requestUri,
                    String queryString,
                    String remoteAddress,
                    String userAgent,
                    int responseStatus,
                    long durationMs) {

        log.info("requestId={} method={} uri={} status={} duration={}ms",
                requestId,
                method,
                queryString,
                responseStatus,
                durationMs
        );

        AuditLogEvent auditLog = AuditLogEvent.builder()
                .requestId(truncate(requestId, REQUEST_ID_MAX_LENGTH))
                .httpMethod(truncate(method, METHOD_MAX_LENGTH))
                .requestUri(truncate(requestUri, URI_MAX_LENGTH))
                .queryString(truncate(queryString, QUERY_MAX_LENGTH))
                .remoteAddress(truncate(remoteAddress, ADDRESS_MAX_LENGTH))
                .userAgent(truncate(userAgent, USER_AGENT_MAX_LENGTH))
                .responseStatus(responseStatus)
                .durationMs(durationMs)
                .build();

        auditEventPublisher.publish(auditLog);
    }

    private String truncate(String v, int max) {
        if (v == null) return null;
        return v.length() <= max ? v : v.substring(0, max);
    }
}
