package com.disys.community_user;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PeakHourSimulator peakHourSimulator;
    private final Random random = new Random();

    public MessagePublisher(RabbitTemplate rabbitTemplate,
                            PeakHourSimulator peakHourSimulator) {
        this.rabbitTemplate = rabbitTemplate;
        this.peakHourSimulator = peakHourSimulator;
    }

    @Scheduled(fixedDelay = 1000)
    public void sendUsageMessage() {
        try {
            int delay = (random.nextInt(4) + 1) * 1000;
            Thread.sleep(delay);

            double kwh = peakHourSimulator.calculateKwh();
            EnergyMessage message = new EnergyMessage(MessageType.USER, "COMMUNITY", kwh, LocalDateTime.now());
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, message);

            System.out.println("Sent usage message: " + kwh + " kWh");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
