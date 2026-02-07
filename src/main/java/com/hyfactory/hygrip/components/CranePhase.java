package com.hyfactory.hygrip.components;

/**
 * Current phase of the crane pick/deposit cycle.
 */
public enum CranePhase {
    IDLE,
    MOVING_TO_SOURCE,
    PICKING,
    MOVING_TO_TARGET,
    DEPOSITING
}
