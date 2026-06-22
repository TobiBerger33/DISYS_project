package com.disys.community_producer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business-Logik für die Berechnung der Usage-Daten.
 *
 * Regel laut Spec:
 *   community_used kann NIEMALS größer sein als community_produced.
 *   Wenn ein User mehr verbraucht als die Community produziert hat,
 *   geht der Überschuss in grid_used.
 */
@Service
public class UsageCalculator {

    private final UsageRepository usageRepository;

    public UsageCalculator(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Transactional
    public UsageData processMessage(EnergyMessage message) {
        // Stunde aus dem datetime ableiten (Minuten/Sekunden auf 0 setzen)
        LocalDateTime hour = message.getDatetime().withMinute(0).withSecond(0).withNano(0);

        // Bestehenden Eintrag für diese Stunde suchen, oder neuen anlegen
        UsageData row = usageRepository.findByHour(hour)
                .orElse(new UsageData(hour));

        if ("PRODUCER".equals(message.getType())) {
            // Producer: einfach kWh zur community_produced addieren
            row.setCommunityProduced(row.getCommunityProduced() + message.getKwh());

        } else if ("USER".equals(message.getType())) {
            // User: zuerst aus dem Community-Pool nehmen
            double available = row.getCommunityProduced() - row.getCommunityUsed();
            double fromCommunity = Math.min(message.getKwh(), available);
            double fromGrid = message.getKwh() - fromCommunity;

            row.setCommunityUsed(row.getCommunityUsed() + fromCommunity);
            row.setGridUsed(row.getGridUsed() + fromGrid);
        }

        return usageRepository.save(row);
    }
}
