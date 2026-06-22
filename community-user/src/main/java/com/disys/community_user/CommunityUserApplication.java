package com.disys.community_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the community-user service, which simulates
 * households consuming energy.
 * {@code @EnableScheduling} activates the @Scheduled method in MessagePublisher
 * that periodically emits USER messages.
 */
@SpringBootApplication
@EnableScheduling
public class CommunityUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityUserApplication.class, args);
    }
}