package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

public class ProducerApp {

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        WeatherService ws = new WeatherService();
        MessagePublisher publisher = new MessagePublisher();

        EnergyMessage msg = new EnergyMessage(
                MessageType.PRODUCER,
                ws.getSunFactor(),
                LocalDateTime.now()
        );
        publisher.publish(msg);
        System.out.println("Published: kwh=" + msg.getKwh() + " at=" + msg.getDatetime());
    }
}
