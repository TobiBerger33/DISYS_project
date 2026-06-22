package com.disys.community_user;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Periodically publishes simulated consumption (USER) messages onto the shared
 * energy.queue. How much energy is "used" depends on the time of day via the
 * PeakHourSimulator.
 */
@Component
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PeakHourSimulator peakHourSimulator;
    // Used to randomize the gap between messages so the stream looks irregular.
    private final Random random = new Random();

    public MessagePublisher(RabbitTemplate rabbitTemplate,
                            PeakHourSimulator peakHourSimulator) {
        this.rabbitTemplate = rabbitTemplate;
        this.peakHourSimulator = peakHourSimulator;
    }

    // fixedDelay = 1000: Spring re-invokes this 1s after the previous call returns.
    @Scheduled(fixedDelay = 1000)
    public void sendUsageMessage() {
        try {
            // Extra random 1-4s wait on top of the fixedDelay for an uneven cadence.
            int delay = (random.nextInt(4) + 1) * 1000;
            Thread.sleep(delay);

            // Compute a realistic consumption value for the current hour and publish it.
            double kwh = peakHourSimulator.calculateKwh();
            EnergyMessage message = new EnergyMessage(MessageType.USER, "COMMUNITY", kwh, LocalDateTime.now());
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, message);

            System.out.println("Sent usage message: " + kwh + " kWh");

        } catch (InterruptedException e) {
            // Restore the interrupt flag and let the scheduler decide what happens next.
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Swallow other errors so one bad send doesn't stop future scheduled runs.
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
