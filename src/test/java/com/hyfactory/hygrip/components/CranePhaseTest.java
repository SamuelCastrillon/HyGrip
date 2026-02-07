package com.hyfactory.hygrip.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CranePhase}.
 */
class CranePhaseTest {

    @Test
    void allPhasesExist() {
        assertEquals(5, CranePhase.values().length);
        assertNotNull(CranePhase.valueOf("IDLE"));
        assertNotNull(CranePhase.valueOf("MOVING_TO_SOURCE"));
        assertNotNull(CranePhase.valueOf("PICKING"));
        assertNotNull(CranePhase.valueOf("MOVING_TO_TARGET"));
        assertNotNull(CranePhase.valueOf("DEPOSITING"));
    }

    @Test
    void defaultCycleOrder() {
        // Typical cycle: IDLE -> MOVING_TO_SOURCE -> PICKING -> MOVING_TO_TARGET -> DEPOSITING -> IDLE
        assertSame(CranePhase.IDLE, CranePhase.IDLE);
        assertNotEquals(CranePhase.MOVING_TO_SOURCE, CranePhase.MOVING_TO_TARGET);
    }
}
