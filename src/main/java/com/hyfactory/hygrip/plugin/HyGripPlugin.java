package com.hyfactory.hygrip.plugin;

import com.hyfactory.hygrip.components.CraneStateComponent;
import com.hyfactory.hygrip.systems.CraneInteractionSystem;
import com.hyfactory.hygrip.systems.CraneMovementSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;

public class HyGripPlugin extends JavaPlugin {

    private final CraneMovementSystem craneMovementSystem = new CraneMovementSystem();
    private final CraneInteractionSystem craneInteractionSystem = new CraneInteractionSystem();

    public HyGripPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Systems are ready. Run crane ticks via runCraneTick(World, CraneStateComponent)
        // when you have a World (e.g. from HyFactory or from a world event).
    }

    /**
     * Runs one tick of crane logic: movement then interaction.
     * Call this each frame/tick for each crane state (e.g. from a world tick or ECS).
     */
    public void runCraneTick(@Nonnull World world, @Nonnull CraneStateComponent state) {
        craneMovementSystem.tick(state);
        craneInteractionSystem.tick(world, state);
    }

    public CraneMovementSystem getCraneMovementSystem() {
        return craneMovementSystem;
    }

    public CraneInteractionSystem getCraneInteractionSystem() {
        return craneInteractionSystem;
    }
}
