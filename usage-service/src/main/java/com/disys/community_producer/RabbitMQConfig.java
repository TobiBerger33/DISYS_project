package com.disys.community_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue auf der Producer und User ihre Messages schicken
    public static final String ENERGY_QUEUE = "energy.queue";

    // Queue auf der der Usage-Service Notifications schickt (für den Percentage-Service)
    public static final String UPDATES_QUEUE = "energy.updates";

    @Bean
    public Queue energyQueue() {
        // durable=true: Queue überlebt RabbitMQ-Neustart
        return new Queue(ENERGY_QUEUE, true);
    }

    @Bean
    public Queue updatesQueue() {
        return new Queue(UPDATES_QUEUE, true);
    }

    // Sorgt dafür dass Objekte automatisch als JSON übertragen werden
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
