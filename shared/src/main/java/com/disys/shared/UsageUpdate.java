package com.disys.shared;

import java.time.LocalDateTime;

/**
 * Notification the usage-service sends to the percentage-service after it has
 * updated the totals for one hour. It carries the aggregated hourly figures,
 * so the percentage-service can recompute its percentages without touching the
 * database itself. Shared so both services agree on the JSON shape.
 */
public class UsageUpdate {

    /** The hour these totals belong to (minutes/seconds are zeroed). */
    private LocalDateTime hour;
    /** Total kWh produced by the community in this hour. */
    private double communityProduced;
    /** Total kWh of community energy actually consumed in this hour. */
    private double communityUsed;
    /** Total kWh that had to be drawn from the public grid in this hour. */
    private double gridUsed;

    public LocalDateTime getHour() {
        return hour;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }

    public void setCommunityProduced(double communityProduced) {
        this.communityProduced = communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }

    public void setCommunityUsed(double communityUsed) {
        this.communityUsed = communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }

    public void setGridUsed(double gridUsed) {
        this.gridUsed = gridUsed;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public UsageUpdate() {
    }

    public UsageUpdate(LocalDateTime hour, double communityProduced,
                       double communityUsed, double gridUsed) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
    }
}