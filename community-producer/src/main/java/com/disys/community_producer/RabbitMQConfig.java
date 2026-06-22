package com.disys.community_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ setup for the producer: declares the target queue and configures
 * JSON (de)serialization so EnergyMessage travels as readable JSON.
 */
@Configuration
public class RabbitMQConfig {

    // Shared queue name; producer, user and usage-service must all use this exact string.
    public static final String QUEUE = "energy.queue";

    @Bean
    public Queue energyQueue() {
        // durable=true: the queue survives a RabbitMQ restart.
        return new Queue(QUEUE, true);
    }

    /**
     * Makes Spring send/receive message bodies as JSON instead of Java
     * serialization, and writes LocalDateTime as ISO text rather than a number.
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
