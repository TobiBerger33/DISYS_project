package com.disys.community_user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommunityUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityUserApplication.class, args);
    }
}