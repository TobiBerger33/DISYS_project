package com.disys.community_user;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class PeakHourSimulator {

    private static final double BASE_KWH = 0.002;

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

    public double calculateKwh() {
        double multiplier = getMultiplier();
        double variation = 0.9 + (Math.random() * 0.2);
        double kwh = BASE_KWH * multiplier * variation;
        return Math.round(kwh * 10000.0) / 10000.0;
    }
}