package com.hyfactory.hygrip.commands;

import com.hyfactory.hygrip.components.CraneStateComponent;
import com.hyfactory.hygrip.plugin.HyGripPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Test command: /hygrip test — creates a test crane at (0,0,0) with job
 * source (1,0,0) and target (-1,0,0), stores it in the plugin and runs
 * runCraneTick every tick so items move between inventory blocks at those coords.
 */
public class HyGripTestCommand extends AbstractCommand {

    private final HyGripPlugin plugin;

    public HyGripTestCommand(@Nonnull HyGripPlugin plugin) {
        super("test", "Starts a test crane: base (0,0,0), source (1,0,0), target (-1,0,0). Place inventory blocks there.");
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can run this command."));
            return CompletableFuture.completedFuture(null);
        }

        Player player = context.senderAs(Player.class);
        if (player.getPlayerRef() == null) {
            context.sendMessage(Message.raw("Player ref not available."));
            return CompletableFuture.completedFuture(null);
        }

        UUID worldUuid = player.getPlayerRef().getWorldUuid();
        if (worldUuid == null) {
            context.sendMessage(Message.raw("You are not in a world."));
            return CompletableFuture.completedFuture(null);
        }

        World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            context.sendMessage(Message.raw("World not found."));
            return CompletableFuture.completedFuture(null);
        }

        CraneStateComponent state = new CraneStateComponent();
        state.setBaseX(0);
        state.setBaseY(0);
        state.setBaseZ(0);
        state.setJob(1, 0, 0, -1, 0, 0);

        plugin.addTestCrane(world, state);
        context.sendMessage(Message.raw("HyGrip test crane started at (0,0,0): source (1,0,0) -> target (-1,0,0). Place inventory blocks there to see items move."));
        return CompletableFuture.completedFuture(null);
    }
}
