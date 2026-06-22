package com.disys.community_user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for the user service: declares the queue it publishes to and
 * configures JSON (de)serialization, matching the producer's config so both
 * sides speak the same wire format.
 */
@Configuration
public class RabbitMQConfig {

    // Same queue the producer uses; the usage-service consumes from it.
    public static final String QUEUE = "energy.queue";

    @Bean
    public Queue energyQueue() {
        // durable=true: the queue survives a RabbitMQ restart.
        return new Queue(QUEUE, true);
    }

    /**
     * Sends/receives message bodies as JSON and writes LocalDateTime as ISO text.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        // JavaTimeModule teaches Jackson how to handle java.time types (LocalDateTime).
        mapper.registerModule(new JavaTimeModule());
        // Write dates as "2025-01-10T14:00:00" instead of an epoch timestamp.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
