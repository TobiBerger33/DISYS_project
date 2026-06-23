package com.disys.usage_service;

import com.disys.shared.EnergyMessage;
import com.disys.shared.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business logic for aggregating the usage data.
 *
 * Rule from the spec:
 *   community_used can NEVER exceed community_produced.
 *   If a user consumes more than the community has produced,
 *   the surplus goes into grid_used (energy bought from the public grid).
 */
@Service
public class UsageCalculator {

    private final UsageRepository usageRepository;

    public UsageCalculator(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    /**
     * Applies one incoming energy event to the hourly totals and saves them.
     * Runs in a transaction so the read-modify-write stays consistent.
     */
    @Transactional
    public UsageData processMessage(EnergyMessage message) {
        // Derive the hour bucket from the timestamp (zero out minutes/seconds/nanos).
        LocalDateTime hour = message.getDatetime().withMinute(0).withSecond(0).withNano(0);

        // Load the existing row for that hour, or start a fresh (all-zero) one.
        UsageData row = usageRepository.findByHour(hour)
                .orElse(new UsageData(hour));

        if (message.getType() == MessageType.PRODUCER) {
            // Producer: simply add the kWh to community_produced.
            row.setCommunityProduced(row.getCommunityProduced() + message.getKwh());

        } else if (message.getType() == MessageType.USER) {
            // User: cover the demand from the community pool first.
            // available = how much produced energy hasn't been consumed yet.
            double available = row.getCommunityProduced() - row.getCommunityUsed();
            // Take as much as possible from the community, but never more than is available.
            double fromCommunity = Math.min(message.getKwh(), available);
            // Whatever is left over must come from the public grid.
            double fromGrid = message.getKwh() - fromCommunity;

            row.setCommunityUsed(row.getCommunityUsed() + fromCommunity);
            row.setGridUsed(row.getGridUsed() + fromGrid);
        }

        return usageRepository.save(row);
    }
}
