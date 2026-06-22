package com.disys.community_producer;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Drives the production simulation: in an endless background loop it asks the
 * weather service for the current sun factor, converts it to kWh, and publishes
 * a PRODUCER message. The loop waits a random 1-5s between messages so the
 * stream looks irregular like real sensor data.
 */
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

    /** Builds one PRODUCER message from the current weather and publishes it. */
    private void produceAndSend() throws IOException, InterruptedException {
        double sunFactor = weather.getSunFactor();
        double kwh = calculator.calculate(sunFactor);
        EnergyMessage msg = new EnergyMessage(MessageType.PRODUCER, "COMMUNITY", kwh, LocalDateTime.now());
        publisher.publish(msg);
        System.out.println("Sent: " + kwh + " kWh (sunFactor " + sunFactor + ")");
    }

    // Runs automatically once the Spring bean is fully constructed.
    @PostConstruct
    public void start() {
        // Run the loop on its own thread so it doesn't block application startup.
        Thread producer = new Thread(this::loop);
        producer.start();
    }

    // Endless producing loop until the thread is interrupted (e.g. on shutdown).
    private void loop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Random pause of 1000-5000 ms before sending the next reading.
                long delay = ThreadLocalRandom.current().nextLong(1000, 5001);
                Thread.sleep(delay);
                produceAndSend();
            } catch (InterruptedException e) {
                // Restore the interrupt flag so the while-condition ends the loop.
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Don't let a single failed send (e.g. API hiccup) kill the loop.
                System.out.println("Error while sending: " + e.getMessage());
            }
        }
    }
}