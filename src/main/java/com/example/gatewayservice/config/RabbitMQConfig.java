package com.example.gatewayservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    // teacher rabbit mq config
    public static final String AUDIT_LOG_EXCHANGE = "audit.log.exchange";
    public static final String AUDIT_LOG_QUEUE = "audit.log.queue";
    public static final String AUDIT_LOG_RK = "audit.log";

    public static final String AUDIT_LOG_DLX = "dlx.audit.log.exchange";
    public static final String AUDIT_LOG_DLQ_NAME = "dlq.audit.log.queue";
    public static final String AUDIT_LOG_DLX_RK = "dlx.audit.log";


    @Bean
    public DirectExchange auditLogExchange() {
        return new DirectExchange(AUDIT_LOG_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange auditLogDeadLetterExchange() {
        return new DirectExchange(AUDIT_LOG_DLX, true, false);
    }

    @Bean
    public Queue auditLogQueue() {
        return QueueBuilder.durable(AUDIT_LOG_QUEUE)
                .deadLetterExchange(AUDIT_LOG_DLX)
                .deadLetterRoutingKey(AUDIT_LOG_DLX_RK)
                .build();
    }

    @Bean
    public Queue auditLogDeadLetterQueue() {
        return QueueBuilder.durable(AUDIT_LOG_DLQ_NAME)
                .build();
    }

    // Binding
    @Bean
    public Binding auditLogBinding() {
        return BindingBuilder.bind(auditLogQueue())
                .to(auditLogExchange())
                .with(AUDIT_LOG_RK);
    }

    // DLQ Binding
    @Bean
    public Binding auditLogDLQBinding() {
        return BindingBuilder.bind(auditLogDeadLetterQueue())
                .to(auditLogDeadLetterExchange())
                .with(AUDIT_LOG_DLX_RK);
    }

    // ===== COMMON =====
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
    // ==========================================
}
