package com.disys.percentage_service;

import com.disys.shared.UsageUpdate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Converts the raw hourly totals (produced/used/grid kWh) into the two
 * percentages the UI shows:
 *   - communityDepleted: how much of the produced community energy is used up
 *   - gridPortion:       how much of the total consumption came from the grid
 */
@Service
public class PercentageCalculator {

    private final PercentageRepository percentageRepository;

    public PercentageCalculator(PercentageRepository percentageRepository) {
        this.percentageRepository = percentageRepository;
    }

    /** Recomputes and saves the percentages for the hour in the given update. */
    @Transactional
    public Percentage process(UsageUpdate usageUpdate) {
        double produced = usageUpdate.getCommunityProduced();
        double used = usageUpdate.getCommunityUsed();
        double grid = usageUpdate.getGridUsed();

        // Share of the community pool consumed. Guard against divide-by-zero when
        // nothing was produced yet, and cap at 100% (can't use more than exists).
        double depleted = produced > 0 ? (used / produced) * 100 : 0.0;
        if (depleted > 100.0) depleted = 100.0;

        // Of everything consumed this hour, what fraction came from the grid?
        double total = used + grid;
        double portion = total > 0 ? (grid / total) * 100 : 0.0;

        // Update the existing row for this hour, or create one if it's the first update.
        Percentage row = percentageRepository.findByHour(usageUpdate.getHour())
                .orElse(new Percentage(usageUpdate.getHour()));
        row.setCommunityDepleted(round2(depleted));
        row.setGridPortion(round2(portion));
        return percentageRepository.save(row);
    }

    /** Rounds a percentage to 2 decimal places for tidy display. */
    public double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
