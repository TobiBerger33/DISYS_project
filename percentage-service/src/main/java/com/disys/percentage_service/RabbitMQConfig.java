package com.disys.percentage_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for the percentage-service: declares the queue it consumes and
 * configures the JSON converter (matching the usage-service that publishes here).
 */
@Configuration
public class RabbitMQConfig {

    // Queue the usage-service publishes hourly updates to; this service consumes it.
    public static final String UPDATE_QUEUE = "energy.updates";

    @Bean
    public Queue updateQueue() {
        // durable=true: the queue survives a RabbitMQ restart.
        return new Queue(UPDATE_QUEUE, true);
    }

    /** Receives message bodies as JSON and reads LocalDateTime from ISO text. */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        // JavaTimeModule teaches Jackson how to handle java.time types (LocalDateTime).
        mapper.registerModule(new JavaTimeModule());
        // Match the producer side: dates as ISO text, not epoch timestamps.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
