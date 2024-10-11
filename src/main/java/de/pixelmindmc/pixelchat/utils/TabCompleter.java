/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to provide tab completion for commands
 */
public class TabCompleter implements org.bukkit.command.TabCompleter {
    private final List<String> results = new ArrayList<>();

    /**
     * Handles tab completion for the "pixelchat" command
     *
     * @param sender The source of the command (player/console)
     * @param cmd    The command being executed
     * @param label  The alias used
     * @param args   The command arguments
     * @return A list of possible completions based on input and permissions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // Clear previous results to avoid stale completions
        results.clear();

        // Handle tab completion for the "pixelchat" command
        if (cmd.getLabel().equalsIgnoreCase("pixelchat")) {
            if (args.length == 1) {
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_VERSION)) results.add("version");
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_RELOAD)) results.add("reload");
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_REMOVE_PLAYER_STRIKES))
                    results.add("remove-strikes");
            } else if (args.length == 2 && args[0].equals("remove-strikes")) addOnlinePlayerCompletions();
        }

        return results;
    }

    /**
     * Adds online players to the results list for player-based commands
     */
    private void addOnlinePlayerCompletions() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            results.add(player.getName());
        }
    }
}