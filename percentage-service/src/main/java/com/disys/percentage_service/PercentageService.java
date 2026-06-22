package com.disys.percentage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the percentage-service. Once running it consumes
 * energy.updates and keeps the percentage table up to date for the rest-api/GUI.
 */
@SpringBootApplication
public class PercentageService {
    public static void main(String[] args) {
        SpringApplication.run(PercentageService.class, args);
    }
}
