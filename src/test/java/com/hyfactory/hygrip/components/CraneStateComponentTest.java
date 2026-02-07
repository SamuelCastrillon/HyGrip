package com.hyfactory.hygrip.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CraneStateComponent} state logic (setJob, isArmAt).
 * Does not call clone() to avoid ItemStack dependency in test scope.
 */
class CraneStateComponentTest {

    private CraneStateComponent state;

    @BeforeEach
    void setUp() {
        state = new CraneStateComponent();
        state.setBaseX(0);
        state.setBaseY(0);
        state.setBaseZ(0);
    }

    @Test
    void setJobMovesArmToBaseAndStartsMovingToSource() {
        state.setJob(1, 0, 0, -1, 0, 0);

        assertEquals(0, state.getArmX());
        assertEquals(0, state.getArmY());
        assertEquals(0, state.getArmZ());
        assertEquals(CranePhase.MOVING_TO_SOURCE, state.getPhase());
        assertEquals(1, state.getSourceX());
        assertEquals(0, state.getSourceY());
        assertEquals(0, state.getSourceZ());
        assertEquals(-1, state.getTargetX());
        assertEquals(0, state.getTargetY());
        assertEquals(0, state.getTargetZ());
    }

    @Test
    void isArmAtReturnsTrueWhenPositionMatches() {
        state.setArmX(3);
        state.setArmY(5);
        state.setArmZ(7);
        assertTrue(state.isArmAt(3, 5, 7));
    }

    @Test
    void isArmAtReturnsFalseWhenAnyCoordinateDiffers() {
        state.setArmX(3);
        state.setArmY(5);
        state.setArmZ(7);
        assertFalse(state.isArmAt(2, 5, 7));
        assertFalse(state.isArmAt(3, 4, 7));
        assertFalse(state.isArmAt(3, 5, 8));
    }

    @Test
    void gettersAndSettersForReachAndSpeed() {
        state.setMoveSpeed(2);
        state.setMaxReach(24);
        assertEquals(2, state.getMoveSpeed());
        assertEquals(24, state.getMaxReach());
    }
}
