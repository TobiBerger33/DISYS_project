package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ProductionScheduler {

    private final WeatherService weather;
    private final SolarOutputCalculator calculator;
    private final MessagePublisher publisher;

    public ProductionScheduler(WeatherService weather,
                               SolarOutputCalculator calculator,
                               MessagePublisher publisher) {
        this.weather = weather;
        this.calculator = calculator;
        this.publisher = publisher;
    }

    private void produceAndSend() throws IOException, InterruptedException {
        double sunFactor = weather.getSunFactor();
        double kwh = calculator.calculate(sunFactor);
        EnergyMessage msg = new EnergyMessage(MessageType.PRODUCER, "COMMUNITY", kwh, LocalDateTime.now());
        publisher.publish(msg);
        System.out.println("Gesendet: " + kwh + " kWh (sunFactor " + sunFactor + ")");
    }

    @PostConstruct
    public void start() {
        Thread producer = new Thread(this::loop);
        producer.start();
    }

    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long delay = ThreadLocalRandom.current().nextLong(1000, 5001);
                Thread.sleep(delay);
                produceAndSend();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.println("Fehler beim Senden: " + e.getMessage());
            }
        }
    }
}