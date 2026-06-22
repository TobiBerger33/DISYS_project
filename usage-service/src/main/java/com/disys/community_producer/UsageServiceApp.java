package com.disys.community_producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the usage-service. Once running it consumes
 * energy.queue, aggregates the events into hourly usage_data rows, and forwards
 * updates to the percentage-service via energy.updates.
 */
@SpringBootApplication
public class UsageServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApp.class, args);
    }
}
