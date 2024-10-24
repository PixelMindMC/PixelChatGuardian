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
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_STRIKE_PLAYER)) results.add("strike");
            } else if (args.length == 2) {
                if (args[0].equals("remove-strikes") || args[0].equals("remove-strike") || args[0].equals("removestrikes") || args[0].equals("removestrike") || args[0].equals("rmstrikes") || args[0].equals("rmstrike"))
                    addOnlinePlayerCompletions();
                if (args[0].equals("strike")) addOnlinePlayerCompletions();
            } else if (args.length == 3) if (args[0].equals("strike")) results.add("<reason>");
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