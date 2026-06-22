package com.disys.community_producer;

import org.springframework.stereotype.Component;

 import java.util.concurrent.ThreadLocalRandom;

@Component
public class SolarOutputCalculator {

    private static final double BASE = 0.3;

    public double calculate(double sunFactor) {
        double jitter = ThreadLocalRandom.current().nextDouble(0.9, 1.1);
        return BASE * sunFactor * jitter;
    }
}