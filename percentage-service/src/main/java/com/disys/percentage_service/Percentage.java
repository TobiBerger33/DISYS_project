package com.disys.percentage_service;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Percentage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_depleted", nullable = false)
    private double communityDepleted;

    @Column(name = "grid_portion")
    private double gridPortion;

    public Percentage() {}

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
