package com.disys.usage_service;

import com.disys.shared.EnergyMessage;
import com.disys.shared.UsageUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens on energy.queue and processes incoming PRODUCER/USER messages.
 * After each message is processed, a notification is published to energy.updates
 * so the percentage-service can react.
 */
@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final UsageCalculator usageCalculator;
    private final RabbitTemplate rabbitTemplate;

    public MessageConsumer(UsageCalculator usageCalculator, RabbitTemplate rabbitTemplate) {
        this.usageCalculator = usageCalculator;
        this.rabbitTemplate = rabbitTemplate;
    }

    // @RabbitListener tells Spring: "call this method whenever a message arrives on energy.queue".
    @RabbitListener(queues = RabbitMQConfig.ENERGY_QUEUE)
    public void handleEnergyMessage(EnergyMessage message) {
        log.info("Message received: type={}, kwh={}, datetime={}",
                message.getType(), message.getKwh(), message.getDatetime());

        try {
            // Fold this event into the running hourly totals (and persist them).
            UsageData updated = usageCalculator.processMessage(message);

            log.info("DB updated for hour {}: produced={}, used={}, grid={}",
                    updated.getHour(), updated.getCommunityProduced(),
                    updated.getCommunityUsed(), updated.getGridUsed());

            // Notify the percentage-service. We forward the fresh hourly totals so
            // it can recompute the percentages for exactly this hour.
            String hourStr = updated.getHour().toString();
            UsageUpdate usageUpdate = new UsageUpdate(updated.getHour(),
                    updated.getCommunityProduced(),
                    updated.getCommunityUsed(),
                    updated.getGridUsed());
            rabbitTemplate.convertAndSend(RabbitMQConfig.UPDATES_QUEUE, usageUpdate);
            log.info("Update notification sent for hour: {}", hourStr);

        } catch (Exception e) {
            // Log and swallow: a single bad message must not stop the listener.
            log.error("Error while processing message: {}", e.getMessage(), e);
        }
    }
}
