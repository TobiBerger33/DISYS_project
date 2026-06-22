package com.disys.community_producer;

import java.time.LocalDateTime;

/**
 * DTO für Messages die von Producer und User auf der Queue ankommen.
 * Format laut Spec:
 *   type: "PRODUCER" oder "USER"
 *   association: "COMMUNITY"
 *   kwh: z.B. 0.003
 *   datetime: z.B. 2025-01-10T14:33:00
 */
public class EnergyMessage {

    private String type;        // "PRODUCER" oder "USER"
    private String association; // "COMMUNITY"
    private double kwh;
    private LocalDateTime datetime;

    public EnergyMessage() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAssociation() { return association; }
    public void setAssociation(String association) { this.association = association; }

    public double getKwh() { return kwh; }
    public void setKwh(double kwh) { this.kwh = kwh; }

    public LocalDateTime getDatetime() { return datetime; }
    public void setDatetime(LocalDateTime datetime) { this.datetime = datetime; }
}
