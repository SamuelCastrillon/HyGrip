package com.hyfactory.hygrip.systems;

import com.hyfactory.hygrip.components.CranePhase;
import com.hyfactory.hygrip.components.CraneStateComponent;

/**
 * Updates crane arm position and phase transitions that depend only on movement.
 * Run before CraneInteractionSystem each tick.
 */
public final class CraneMovementSystem {

    public void tick(CraneStateComponent state) {
        CranePhase phase = state.getPhase();
        if (phase != CranePhase.MOVING_TO_SOURCE && phase != CranePhase.MOVING_TO_TARGET) {
            return;
        }

        int step = Math.max(1, state.getMoveSpeed());
        int dx = 0, dy = 0, dz = 0;

        if (phase == CranePhase.MOVING_TO_SOURCE) {
            dx = clampStep(state.getArmX(), state.getSourceX(), step);
            dy = clampStep(state.getArmY(), state.getSourceY(), step);
            dz = clampStep(state.getArmZ(), state.getSourceZ(), step);
        } else {
            dx = clampStep(state.getArmX(), state.getTargetX(), step);
            dy = clampStep(state.getArmY(), state.getTargetY(), step);
            dz = clampStep(state.getArmZ(), state.getTargetZ(), step);
        }

        state.setArmX(state.getArmX() + dx);
        state.setArmY(state.getArmY() + dy);
        state.setArmZ(state.getArmZ() + dz);

        if (phase == CranePhase.MOVING_TO_SOURCE && state.isArmAt(state.getSourceX(), state.getSourceY(), state.getSourceZ())) {
            state.setPhase(CranePhase.PICKING);
        } else if (phase == CranePhase.MOVING_TO_TARGET && state.isArmAt(state.getTargetX(), state.getTargetY(), state.getTargetZ())) {
            state.setPhase(CranePhase.DEPOSITING);
        }
    }

    private static int clampStep(int from, int to, int step) {
        int d = to - from;
        if (d == 0) return 0;
        if (d > 0) return Math.min(step, d);
        return Math.max(-step, d);
    }
}
