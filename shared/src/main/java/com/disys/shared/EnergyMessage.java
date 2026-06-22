package com.disys.shared;

import java.time.LocalDateTime;

/**
 * Message that producers and users send to the energy.queue.
 *
 * One message represents a single energy event: either some kWh that were
 * produced (PRODUCER) or consumed (USER) at a given point in time.
 * Lives in the shared module so producer, user and usage-service all agree
 * on the exact JSON shape that travels over RabbitMQ.
 */
public class EnergyMessage {

    /** PRODUCER = energy fed into the community, USER = energy taken out. */
    private MessageType type;
    /** Name of the community/association the event belongs to (e.g. "COMMUNITY"). */
    private String association;
    /** Amount of energy for this single event, in kilowatt-hours. */
    private double kwh;
    /** When the event happened; the usage-service buckets this into a full hour. */
    private LocalDateTime datetime;

    public EnergyMessage() {
    }

    public EnergyMessage(MessageType type, double kwh, LocalDateTime datetime) {
        this.type = type;
        this.kwh = kwh;
        this.datetime = datetime;
    }

    public EnergyMessage(MessageType type, String association, double kwh, LocalDateTime datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
        this.datetime = datetime;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public double getKwh() {
        return kwh;
    }

    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }
}
