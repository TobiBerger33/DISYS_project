package com.disys.community_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for the usage-service. It both consumes (energy.queue) and
 * produces (energy.updates), so it declares both queues plus the JSON converter.
 */
@Configuration
public class RabbitMQConfig {

    // Queue that producer and user send their messages to (this service consumes it).
    public static final String ENERGY_QUEUE = "energy.queue";

    // Queue this service publishes notifications to (consumed by the percentage-service).
    public static final String UPDATES_QUEUE = "energy.updates";

    @Bean
    public Queue energyQueue() {
        // durable=true: the queue survives a RabbitMQ restart.
        return new Queue(ENERGY_QUEUE, true);
    }

    @Bean
    public Queue updatesQueue() {
        return new Queue(UPDATES_QUEUE, true);
    }

    // Makes objects travel as JSON automatically (and dates as ISO text).
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
