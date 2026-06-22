package com.disys.community_user;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

/**
 * Produces a realistic consumption value (kWh) for a single USER message.
 * Real households use more energy at certain times, so the base value is scaled
 * by a time-of-day multiplier (morning/evening peaks, low at night).
 */
@Service
public class PeakHourSimulator {

    // Average consumption per message at a "normal" hour, before scaling.
    private static final double BASE_KWH = 0.3;

    /**
     * @return how much to scale BASE_KWH by, based on the current local hour
     */
    public double getMultiplier() {
        int hour = LocalTime.now().getHour();

        if (hour >= 7 && hour < 9) {
            return 2.5; // Morning peak
        } else if (hour >= 12 && hour < 13) {
            return 1.5; // Lunch
        } else if (hour >= 17 && hour < 20) {
            return 2.5; // Evening peak
        } else if (hour >= 22 || hour < 5) {
            return 0.3; // Night
        } else {
            return 1.0; // Normal daytime
        }
    }

    /**
     * @return consumption in kWh for one message: base * time-of-day * noise,
     *         rounded to 4 decimal places
     */
    public double calculateKwh() {
        double multiplier = getMultiplier();
        // +/-10% random variation so consecutive readings aren't identical.
        double variation = 0.9 + (Math.random() * 0.2);
        double kwh = BASE_KWH * multiplier * variation;
        // Round to 4 decimals to keep the kWh value tidy.
        return Math.round(kwh * 10000.0) / 10000.0;
    }
}