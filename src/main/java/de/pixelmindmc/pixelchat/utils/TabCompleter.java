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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to provide tab completion for commands
 */
public class TabCompleter implements org.bukkit.command.TabCompleter {
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> results = new ArrayList<>();

        switch (cmd.getLabel()) {
            case "pixelchat" -> handlePixelChatTabCompletion(sender, args, results);
            case "remove-strikes" -> handleRemoveStrikesTabCompletion(sender, args, results);
            case "strike" -> handleStrikeTabCompletion(sender, args, results);
        }

        return results;
    }

    /**
     * Handles tab completion for the "pixelchat" command.
     */
    private void handlePixelChatTabCompletion(@NotNull CommandSender sender, @NotNull String[] args, @NotNull List<String> results) {
        if (args.length == 1) {
            if (sender.hasPermission(PermissionConstants.PIXELCHAT_VERSION)) results.add("version");
            if (sender.hasPermission(PermissionConstants.PIXELCHAT_RELOAD)) results.add("reload");
        }
    }

    /**
     * Handles tab completion for the "remove-strikes" command.
     */
    private void handleRemoveStrikesTabCompletion(@NotNull CommandSender sender, @NotNull String[] args, @NotNull List<String> results) {
        if (args.length == 1 && sender.hasPermission(PermissionConstants.PIXELCHAT_REMOVE_PLAYER_STRIKES))
            addOnlinePlayerCompletions(results);
    }

    /**
     * Handles tab completion for the "strike" command.
     */
    private void handleStrikeTabCompletion(@NotNull CommandSender sender, @NotNull String[] args, @NotNull List<String> results) {
        if (sender.hasPermission(PermissionConstants.PIXELCHAT_STRIKE_PLAYER)) {
            if (args.length == 1) {
                addOnlinePlayerCompletions(results);
            } else if (args.length == 2) results.add("<reason>");
        }
    }

    /**
     * Adds online players to the results list for player-based commands.
     */
    private void addOnlinePlayerCompletions(@NotNull List<String> results) {
        for (Player player : Bukkit.getOnlinePlayers())
            results.add(player.getName());
    }
}