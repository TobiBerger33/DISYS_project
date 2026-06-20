package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class  ProducerApp {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApp.class, args);
    }

    @Bean
    public CommandLineRunner sendTestMessage(MessagePublisher publisher) {
        return args -> {
            EnergyMessage msg = new EnergyMessage(MessageType.PRODUCER, 3.5, LocalDateTime.now());
            publisher.publish(msg);
            System.out.println("Test-Message gesendet: " + msg.getKwh() + " kWh");
        };
    }
}
