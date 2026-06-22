package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens on energy.updates for hourly totals coming from the usage-service and
 * hands them to the calculator, which turns them into stored percentages.
 */
@Component
public class MessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final PercentageCalculator percentageCalculator;

    public MessageConsumer(PercentageCalculator percentageCalculator) {
        this.percentageCalculator = percentageCalculator;
    }

    // Invoked by Spring whenever a UsageUpdate arrives on the energy.updates queue.
    @RabbitListener(queues = RabbitMQConfig.UPDATE_QUEUE)
    public void handleUpdate(UsageUpdate usageUpdate) {
        log.info("Update received for hour {}", usageUpdate.getHour());
        Percentage result = percentageCalculator.process(usageUpdate);
        log.info("Percentage saved {}: depleted={}%, gridPortion={}%",
                result.getHour(), result.getCommunityDepleted(), result.getGridPortion());
    }
}
