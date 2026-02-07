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

        String[] args = getArgsFromContext(context);
        if (args != null && args.length >= 1) {
            try {
                if (args.length >= 4) {
                    baseX = Integer.parseInt(args[0]);
                    baseY = Integer.parseInt(args[1]);
                    baseZ = Integer.parseInt(args[2]);
                    String dirStr = args[3].trim().toLowerCase(Locale.ROOT);
                    CraneDirection parsed = CraneDirection.byName(dirStr);
                    if (parsed == null) {
                        context.sendMessage(Message.raw("Unknown direction: " + dirStr + ". " + USAGE));
                        return CompletableFuture.completedFuture(null);
                    }
                    direction = parsed;
                } else if (args.length >= 3) {
                    baseX = Integer.parseInt(args[0]);
                    baseY = Integer.parseInt(args[1]);
                    baseZ = Integer.parseInt(args[2]);
                } else if (args.length >= 1) {
                    String dirStr = args[0].trim().toLowerCase(Locale.ROOT);
                    CraneDirection parsed = CraneDirection.byName(dirStr);
                    if (parsed != null) direction = parsed;
                }
            } catch (NumberFormatException e) {
                context.sendMessage(Message.raw("Invalid number in coordinates. " + USAGE));
                return CompletableFuture.completedFuture(null);
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

    /**
     * Get our command args (base coords + direction) from context.
     * Tries ParserContext first; if not available, parses the input string (CommandContext is built with getInputString()).
     */
    @Nullable
    private static String[] getArgsFromContext(@Nonnull CommandContext context) {
        ParserContext parserContext = getParserContextViaReflection(context);
        if (parserContext != null) {
            int numTokens = parserContext.getNumPreOptionalTokens();
            int offset = (numTokens >= 1 && "test".equalsIgnoreCase(parserContext.getPreOptionalSingleValueToken(0))) ? 1 : 0;
            int available = numTokens - offset;
            if (available <= 0) return null;
            String[] out = new String[available];
            for (int i = 0; i < available; i++) {
                out[i] = parserContext.getPreOptionalSingleValueToken(offset + i);
            }
            return out;
        }
        String input = getInputStringViaReflection(context);
        if (input == null || input.isBlank()) return null;
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length < 2 || !"test".equalsIgnoreCase(tokens[1])) return null;
        if (tokens.length <= 2) return null;
        String[] args = new String[tokens.length - 2];
        System.arraycopy(tokens, 2, args, 0, args.length);
        return args;
    }

    @Nullable
    private static ParserContext getParserContextViaReflection(@Nonnull CommandContext context) {
        try {
            Method m = context.getClass().getMethod("getParserContext");
            Object result = m.invoke(context);
            return result instanceof ParserContext ? (ParserContext) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static String getInputStringViaReflection(@Nonnull CommandContext context) {
        for (String methodName : new String[] { "getInputString", "getCommand", "getRawInput", "getInput" }) {
            try {
                Method m = context.getClass().getMethod(methodName);
                Object result = m.invoke(context);
                if (result instanceof String s && !s.isBlank()) return s;
            } catch (Exception ignored) { }
        }
        return null;
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
