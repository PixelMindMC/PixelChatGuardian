/*
 * Copyright (C) 2024 - 2026 PixelMindMC
 *
 * This file is part of PixelChat Guardian.
 *
 * PixelChat Guardian is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * PixelChat Guardian is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PixelChat Guardian.
 * If not, see <https://www.gnu.org/licenses/>.
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
public class PixelChatTabCompleter implements org.bukkit.command.TabCompleter {
    @NotNull
    final List<String> results = new ArrayList<>();

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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        switch (cmd.getLabel()) {
            case "pixelchat" -> handlePixelChatTabCompletion(sender, args);
            case "remove-strikes" -> handleRemoveStrikesTabCompletion(sender, args);
            case "strike" -> handleStrikeTabCompletion(sender, args);
            default -> {
                return results;
            }
        }

        return results;
    }

    /**
     * Handles tab completion for the "pixelchat" command
     */
    private void handlePixelChatTabCompletion(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            if (sender.hasPermission(PermissionConstants.Commands.VERSION)) {
                results.add("version");
            }
            if (sender.hasPermission(PermissionConstants.Commands.RELOAD)) {
                results.add("reload");
            }
        }
    }

    /**
     * Handles tab completion for the "strike" command
     */
    private void handleStrikeTabCompletion(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (sender.hasPermission(PermissionConstants.Moderation.STRIKE_PLAYER)) {
            if (args.length == 1) {
                addOnlinePlayerCompletions();
            } else if (args.length == 2) {
                results.add("<reason>");
            }
        }
    }

    /**
     * Handles tab completion for the "remove-strikes" command
     */
    private void handleRemoveStrikesTabCompletion(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 1 && sender.hasPermission(PermissionConstants.Moderation.REMOVE_PLAYER_STRIKES)) {
            addOnlinePlayerCompletions();
        }
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