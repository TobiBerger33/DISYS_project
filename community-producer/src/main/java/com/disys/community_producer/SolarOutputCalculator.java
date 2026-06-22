package com.disys.community_producer;

import org.springframework.stereotype.Component;

 import java.util.concurrent.ThreadLocalRandom;

/**
 * Turns a weather "sun factor" into a simulated kWh production value.
 * Pure calculation, no I/O.
 */
@Component
public class SolarOutputCalculator {

    // Reference output (kWh) of the panels under full sun, before jitter.
    private static final double BASE = 0.3;

    /**
     * @param sunFactor 0.0 (no sun) .. 1.0 (full sun) from the WeatherService
     * @return simulated production in kWh, scaled by sun and slightly randomized
     */
    public double calculate(double sunFactor) {
        // +/-10% random noise so two identical-weather readings still differ.
        double jitter = ThreadLocalRandom.current().nextDouble(0.9, 1.1);
        return BASE * sunFactor * jitter;
    }
}