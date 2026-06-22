package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final PercentageCalculator percentageCalculator;

    public MessageConsumer(PercentageCalculator percentageCalculator) {
        this.percentageCalculator = percentageCalculator;
    }

    @RabbitListener(queues = RabbitMQConfig.UPDATE_QUEUE)
    public void handleUpdate(UsageUpdate usageUpdate) {
        log.info("Update empfangen für Stunde {}", usageUpdate.getHour());
        Percentage result = percentageCalculator.process(usageUpdate);
        log.info("Percentage gespeichert {}: depleted={}%, gridPortion={}%",
                result.getHour(), result.getCommunityDepleted(), result.getGridPortion());
    }
}
