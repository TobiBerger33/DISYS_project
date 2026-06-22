package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around RabbitTemplate that sends an EnergyMessage to the queue.
 * Keeps the AMQP details out of the scheduler.
 */
@Component
public class MessagePublisher {
    // Spring AMQP helper that serializes the object to JSON and sends it.
    private final RabbitTemplate rabbitTemplate;

    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Publishes one production event onto the shared energy.queue. */
    public void publish(EnergyMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, message);
    }
}
