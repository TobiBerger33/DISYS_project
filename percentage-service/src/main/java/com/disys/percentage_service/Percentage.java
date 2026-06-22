package com.disys.percentage_service;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the computed percentages, one row per hour. Maps to the
 * default "percentage" table, which the rest-api reads back as CurrentPercentage.
 */
@Entity
public class Percentage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // The hour these percentages describe.
    @Column(nullable = false)
    private LocalDateTime hour;

    // Percentage of the produced community energy that has been consumed (0..100).
    @Column(name = "community_depleted", nullable = false)
    private double communityDepleted;

    // Percentage of total consumption that came from the public grid (0..100).
    @Column(name = "grid_portion")
    private double gridPortion;

    // No-arg constructor required by JPA/Hibernate.
    public Percentage() {}

    // New row for an hour; the two percentages default to 0.0 until set.
    public Percentage(LocalDateTime hour) {
        this.hour = hour;
    }


    public UUID getId() { return id; }

    public LocalDateTime getHour() { return hour; }
    public void setHour(LocalDateTime hour) { this.hour = hour; }

    public double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(double communityDepleted) { this.communityDepleted = communityDepleted; }

    public double getGridPortion() { return gridPortion; }
    public void setGridPortion(double gridPortion) { this.gridPortion = gridPortion; }
}
