package com.disys.shared;

import java.time.LocalDateTime;

public class EnergyMessage {

    private MessageType type;
    private double kwh;
    private LocalDateTime datetime;

    public EnergyMessage() {
    }

    public EnergyMessage(MessageType type, double kwh, LocalDateTime datetime) {
        this.type = type;
        this.kwh = kwh;
        this.datetime = datetime;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
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
