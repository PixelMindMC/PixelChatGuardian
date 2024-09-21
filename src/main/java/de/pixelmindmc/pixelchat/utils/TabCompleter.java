/*
 * This file is part of PixelChatGuardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

// A class to provide tab completion for commands
public class TabCompleter implements org.bukkit.command.TabCompleter {
    private final List<String> results = new ArrayList<>();

    // Provides a list of possible completions
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // Clear previous results to avoid stale completions
        results.clear();

        // Handle tab completion for the "pixelchat" command
        if (cmd.getLabel().equalsIgnoreCase("pixelchat")) {
            if (args.length == 1) {
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_VERSION)) results.add("version");
                if (sender.hasPermission(PermissionConstants.PIXELCHAT_RELOAD)) results.add("reload");
            }
        }

        return results;
    }
}