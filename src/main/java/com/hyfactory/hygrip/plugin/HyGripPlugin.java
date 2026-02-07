package com.hyfactory.hygrip.plugin;

import com.hyfactory.hygrip.commands.HyGripCommand;
import com.hyfactory.hygrip.components.CraneStateComponent;
import com.hyfactory.hygrip.systems.CraneInteractionSystem;
import com.hyfactory.hygrip.systems.CraneMovementSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HyGripPlugin extends JavaPlugin {

    private final CraneMovementSystem craneMovementSystem = new CraneMovementSystem();
    private final CraneInteractionSystem craneInteractionSystem = new CraneInteractionSystem();

    /** Test cranes added by /hygrip test: (world, state) pairs. */
    private final List<TestCraneEntry> testCranes = new CopyOnWriteArrayList<>();
    private volatile ScheduledExecutorService tickScheduler;

    public HyGripPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new HyGripCommand(this));
    }

    /**
     * Adds a test crane (from /hygrip test). Starts the tick scheduler if not already running.
     */
    public void addTestCrane(@Nonnull World world, @Nonnull CraneStateComponent state) {
        testCranes.add(new TestCraneEntry(world, state));
        startTickSchedulerIfNeeded();
    }

    private void startTickSchedulerIfNeeded() {
        if (tickScheduler != null) return;
        synchronized (this) {
            if (tickScheduler != null) return;
            tickScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "HyGrip-crane-tick");
                t.setDaemon(true);
                return t;
            });
            tickScheduler.scheduleAtFixedRate(this::tickAllTestCranes, 50, 50, TimeUnit.MILLISECONDS);
        }
    }

    private void tickAllTestCranes() {
        if (testCranes.isEmpty()) return;
        for (TestCraneEntry entry : testCranes) {
            World world = entry.world;
            CraneStateComponent state = entry.state;
            if (world != null && world.isAlive() && state != null) {
                world.execute(() -> runCraneTick(world, state));
            }
        }
    }

    private static final class TestCraneEntry {
        final World world;
        final CraneStateComponent state;

        TestCraneEntry(World world, CraneStateComponent state) {
            this.world = world;
            this.state = state;
        }
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
