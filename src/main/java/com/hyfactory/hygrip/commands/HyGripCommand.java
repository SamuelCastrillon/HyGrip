package com.hyfactory.hygrip.commands;

import com.hyfactory.hygrip.plugin.HyGripPlugin;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import javax.annotation.Nonnull;

/**
 * Root command for HyGrip: /hygrip &lt;subcommand&gt;.
 * Subcommands: test.
 */
public class HyGripCommand extends AbstractCommandCollection {

    public HyGripCommand(@Nonnull HyGripPlugin plugin) {
        super("hygrip", "HyGrip crane mod commands.");
        addSubCommand(new HyGripTestCommand(plugin));
    }
}
