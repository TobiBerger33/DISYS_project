package com.disys.shared;

/**
 * Tells the usage-service how to treat an {@link EnergyMessage}:
 * PRODUCER adds to the community pool, USER draws from it.
 */
public enum MessageType {
    PRODUCER,
    USER
}
