package com.hyfactory.hygrip.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Component holding crane/arm state data for HyGrip.
 * Compatible with Hytale ECS (Component&lt;EntityStore&gt;).
 */
public class CraneStateComponent implements Component<EntityStore> {

    private float rotationAngle;
    private boolean active;

    public float getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        CraneStateComponent copy = new CraneStateComponent();
        copy.rotationAngle = this.rotationAngle;
        copy.active = this.active;
        return copy;
    }
}
