package com.disys.community_user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@Configuration
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PeakHourSimulator peakHourSimulator;
    private final ObjectMapper mapper;
    private final Random random = new Random();

    @Value("${energy.queue.name}")
    private String queueName;

    public MessagePublisher(RabbitTemplate rabbitTemplate,
                            PeakHourSimulator peakHourSimulator) {
        this.rabbitTemplate = rabbitTemplate;
        this.peakHourSimulator = peakHourSimulator;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Bean
    public Queue energyQueue() {
        return new Queue(queueName, true);
    }

    @Scheduled(fixedDelay = 1000)
    public void sendUsageMessage() {
        try {
            int delay = (random.nextInt(4) + 1) * 1000;
            Thread.sleep(delay);

            double kwh = peakHourSimulator.calculateKwh();

            Map<String, Object> message = new HashMap<>();
            message.put("type", "USER");
            message.put("association", "COMMUNITY");
            message.put("kwh", kwh);
            message.put("datetime", LocalDateTime.now().toString());

            String json = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(queueName, json);

            System.out.println("Sent usage message: " + json);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}