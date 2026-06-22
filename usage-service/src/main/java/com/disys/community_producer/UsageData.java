package com.disys.community_producer;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the usage_data table in the database.
 * Mirrors the schema from the Flyway migration. One row holds the aggregated
 * totals for a single hour.
 */
@Entity
@Table(name = "usage_data")
public class UsageData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The hour this row aggregates (e.g. 2025-01-10T14:00); one row per hour.
    @Column(nullable = false)
    private LocalDateTime hour;

    // Total kWh produced by the community in this hour.
    @Column(name = "community_produced", nullable = false)
    private double communityProduced;

    // Total kWh of community energy actually consumed (never above produced).
    @Column(name = "community_used", nullable = false)
    private double communityUsed;

    // Total kWh that had to be bought from the public grid in this hour.
    @Column(name = "grid_used", nullable = false)
    private double gridUsed;

    // No-arg constructor required by JPA/Hibernate.
    public UsageData() {}

    // Convenience constructor for a brand-new hour: all totals start at zero.
    public UsageData(LocalDateTime hour) {
        this.hour = hour;
        this.communityProduced = 0.0;
        this.communityUsed = 0.0;
        this.gridUsed = 0.0;
    }

    public UUID getId() { return id; }

    public LocalDateTime getHour() { return hour; }
    public void setHour(LocalDateTime hour) { this.hour = hour; }

    public double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(double communityProduced) { this.communityProduced = communityProduced; }

    public double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }

    public double getGridUsed() { return gridUsed; }
    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }
}
