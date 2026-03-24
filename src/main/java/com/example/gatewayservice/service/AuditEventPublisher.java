package com.example.gatewayservice.service;

import com.example.gatewayservice.config.RabbitMQConfig;
import com.example.gatewayservice.entity.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(AuditLogEvent auditLog) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_LOG_EXCHANGE,
                    RabbitMQConfig.AUDIT_LOG_RK,
                    auditLog
            );
        } catch (Exception e) {
            log.warn("Failed to publish audit event to RabbitMQ: {}", e.getMessage());
        }
    }
}
