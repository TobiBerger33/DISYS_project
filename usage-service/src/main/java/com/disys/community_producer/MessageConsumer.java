package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import com.disys.shared.UsageUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Hört auf die energy.queue und verarbeitet eingehende PRODUCER/USER Messages.
 * Nach jeder Verarbeitung wird eine Notification auf energy.updates geschickt,
 * damit der Percentage-Service reagieren kann.
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

    // @RabbitListener sagt Spring: "Ruf diese Methode auf, wenn eine Message auf energy.queue ankommt"
    @RabbitListener(queues = RabbitMQConfig.ENERGY_QUEUE)
    public void handleEnergyMessage(EnergyMessage message) {
        log.info("Message empfangen: type={}, kwh={}, datetime={}",
                message.getType(), message.getKwh(), message.getDatetime());

        try {
            UsageData updated = usageCalculator.processMessage(message);

            log.info("DB aktualisiert für Stunde {}: produced={}, used={}, grid={}",
                    updated.getHour(), updated.getCommunityProduced(),
                    updated.getCommunityUsed(), updated.getGridUsed());

            // Notification an Percentage-Service schicken
            // Wir schicken die Stunde als String, damit der Percentage-Service
            // weiß welche Stunde neu berechnet werden muss
            String hourStr = updated.getHour().toString();
            UsageUpdate usageUpdate = new UsageUpdate(updated.getHour(),
                    updated.getCommunityProduced(),
                    updated.getCommunityUsed(),
                    updated.getGridUsed());
            rabbitTemplate.convertAndSend(RabbitMQConfig.UPDATES_QUEUE, usageUpdate);
            log.info("Update-Notification geschickt für Stunde: {}", hourStr);

        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der Message: {}", e.getMessage(), e);
        }
    }
}
