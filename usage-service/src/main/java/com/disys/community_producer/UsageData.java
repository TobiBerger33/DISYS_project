package com.disys.community_producer;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity für die usage_data Tabelle in der Datenbank.
 * Spiegelt das Schema aus der Flyway Migration wider.
 */
@Entity
@Table(name = "usage_data")
public class UsageData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_produced", nullable = false)
    private double communityProduced;

    @Column(name = "community_used", nullable = false)
    private double communityUsed;

    @Column(name = "grid_used", nullable = false)
    private double gridUsed;

    public UsageData() {}

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
