package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PercentageCalculator {

    private final PercentageRepository percentageRepository;

    public PercentageCalculator(PercentageRepository percentageRepository) {
        this.percentageRepository = percentageRepository;
    }

    @Transactional
    public Percentage process(UsageUpdate usageUpdate) {
        double produced = usageUpdate.getCommunityProduced();
        double used = usageUpdate.getCommunityUsed();
        double grid = usageUpdate.getGridUsed();

        double depleted = produced > 0 ? (used / produced) * 100 : 0.0;
        if (depleted > 100.0) depleted = 100.0;

        double total = used + grid;
        double portion = total > 0 ? (grid / total) * 100 : 0.0;
        Percentage row = percentageRepository.findByHour(usageUpdate.getHour())
                .orElse(new Percentage(usageUpdate.getHour()));
        row.setCommunityDepleted(round2(depleted));
        row.setGridPortion(round2(portion));
        return percentageRepository.save(row);
    }

    public double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
