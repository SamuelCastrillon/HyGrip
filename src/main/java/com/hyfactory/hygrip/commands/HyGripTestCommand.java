package com.hyfactory.hygrip.commands;

import com.hyfactory.hygrip.components.CraneStateComponent;
import com.hyfactory.hygrip.plugin.HyGripPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.ParserContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Test command: /hygrip test [baseX baseY baseZ] [direction]
 * Creates a crane at base; source = base + 1 block in direction, target = base - 1 block.
 * Direction: north, south, east, west, up, down. Default: base (0,117,0), direction east.
 * When invoked as subcommand, token 0 is "test"; our args start at index 1.
 */
public class HyGripTestCommand extends AbstractCommand {

    private static final String USAGE = "Usage: /hygrip test [baseX baseY baseZ] [direction]. Direction: north, south, east, west, up, down.";

    private final HyGripPlugin plugin;

    public HyGripTestCommand(@Nonnull HyGripPlugin plugin) {
        super("test", "Starts a test crane at base looking in direction (source = base+1, target = base-1). Optional: base coords and direction.");
        this.plugin = plugin;
        // Allow 0 to 4 optional positional args without declaring them (AbstractCommand.setAllowsExtraArguments).
        setAllowsExtraArguments(true);
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

        int baseX = 0, baseY = 117, baseZ = 0;
        CraneDirection direction = CraneDirection.EAST;

        ParserContext parserContext = getParserContext(context);
        if (parserContext != null) {
            int numTokens = parserContext.getNumPreOptionalTokens();
            // When invoked as subcommand, token 0 is "test"; our args start at 1. If token 0 is already a number, use 0.
            int offset = 0;
            if (numTokens >= 1 && "test".equalsIgnoreCase(parserContext.getPreOptionalSingleValueToken(0))) {
                offset = 1;
            }
            int available = numTokens - offset;
            if (available >= 1) {
                try {
                    if (available >= 4) {
                        baseX = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 0));
                        baseY = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 1));
                        baseZ = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 2));
                        String dirStr = parserContext.getPreOptionalSingleValueToken(offset + 3).trim().toLowerCase(Locale.ROOT);
                        CraneDirection parsed = CraneDirection.byName(dirStr);
                        if (parsed == null) {
                            context.sendMessage(Message.raw("Unknown direction: " + dirStr + ". " + USAGE));
                            return CompletableFuture.completedFuture(null);
                        }
                        direction = parsed;
                    } else if (available >= 3) {
                        baseX = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 0));
                        baseY = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 1));
                        baseZ = Integer.parseInt(parserContext.getPreOptionalSingleValueToken(offset + 2));
                    } else if (available >= 1) {
                        String dirStr = parserContext.getPreOptionalSingleValueToken(offset + 0).trim().toLowerCase(Locale.ROOT);
                        CraneDirection parsed = CraneDirection.byName(dirStr);
                        if (parsed != null) direction = parsed;
                    }
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid number in coordinates. " + USAGE));
                    return CompletableFuture.completedFuture(null);
                }
            }
        }

        int dx = direction.dx;
        int dy = direction.dy;
        int dz = direction.dz;
        int sourceX = baseX + dx;
        int sourceY = baseY + dy;
        int sourceZ = baseZ + dz;
        int targetX = baseX - dx;
        int targetY = baseY - dy;
        int targetZ = baseZ - dz;

        CraneStateComponent state = new CraneStateComponent();
        state.setBaseX(baseX);
        state.setBaseY(baseY);
        state.setBaseZ(baseZ);
        state.setJob(sourceX, sourceY, sourceZ, targetX, targetY, targetZ);

        plugin.addTestCrane(world, state);
        context.sendMessage(Message.raw(String.format(
                "HyGrip test crane at (%d,%d,%d) facing %s: source (%d,%d,%d) -> target (%d,%d,%d). Place inventory blocks there.",
                baseX, baseY, baseZ, direction.name, sourceX, sourceY, sourceZ, targetX, targetY, targetZ)));
        return CompletableFuture.completedFuture(null);
    }

    /** Obtain ParserContext from CommandContext via reflection (API may not expose it publicly). */
    @Nullable
    private static ParserContext getParserContext(@Nonnull CommandContext context) {
        try {
            Method m = context.getClass().getMethod("getParserContext");
            Object result = m.invoke(context);
            return result instanceof ParserContext ? (ParserContext) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Cardinal + vertical direction; unit vector (dx, dy, dz) toward that direction. */
    private enum CraneDirection {
        NORTH("north", 0, 0, -1),
        SOUTH("south", 0, 0, 1),
        EAST("east", 1, 0, 0),
        WEST("west", -1, 0, 0),
        UP("up", 0, 1, 0),
        DOWN("down", 0, -1, 0);

        final String name;
        final int dx;
        final int dy;
        final int dz;

        CraneDirection(String name, int dx, int dy, int dz) {
            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        @Nullable
        static CraneDirection byName(String s) {
            if (s == null || s.isEmpty()) return null;
            switch (s) {
                case "north": return NORTH;
                case "south": return SOUTH;
                case "east": return EAST;
                case "west": return WEST;
                case "up": return UP;
                case "down": return DOWN;
                default: return null;
            }
        }
    }
}
