package com.disys.community_producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the community-producer service.
 * Starting it boots the scheduler that keeps publishing simulated solar
 * production into the RabbitMQ energy.queue.
 */
@SpringBootApplication
public class ProducerApp {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApp.class, args);
    }
}
