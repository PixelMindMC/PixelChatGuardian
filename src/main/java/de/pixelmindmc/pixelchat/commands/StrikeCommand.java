/*
 * This file is part of PixelChatGuardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.commands;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.utils.ChatGuardHelper;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * CommandExecutor for handling the "strike" command
 */
public class StrikeCommand implements CommandExecutor {
    private final @NotNull PixelChat plugin;

    /**
     * Constructs a PixelChatCommand object
     *
     * @param plugin The plugin instance
     */
    public StrikeCommand(@NotNull PixelChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the execution of the "strike" command
     *
     * @param sender  The source of the command (player or console)
     * @param command The command being executed
     * @param label   The alias used to invoke the command
     * @param args    The arguments provided with the command
     * @return true to indicate the command was processed
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        ConfigHelper configHelperLanguage = plugin.getConfigHelperLanguage();

        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.Moderation.STRIKE_PLAYER)) {
            sender.sendMessage(ChatColor.RED + configHelperLanguage.getString(LangConstants.Global.NO_PERMISSION));
            return true;
        }

        // Check if the command syntax is correct
        if (args.length != 2) {
            sender.sendMessage(
                    LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.Global.INVALID_SYNTAX) +
                            " " +
                            ChatColor.RESET + configHelperLanguage.getString(LangConstants.Global.INVALID_SYNTAX_USAGE) + label +
                            " <player> <reason>");
            return true;
        }

        Player onlinePlayer = Bukkit.getPlayer(args[0]);
        UUID playerUUID;
        if (onlinePlayer != null) {
            playerUUID = onlinePlayer.getUniqueId();
        } else playerUUID = plugin.getPixelChatCommand().getOfflinePlayerUUID(args[0]);

        assert playerUUID != null;
        ChatGuardHelper.runStrikeSystem(plugin, playerUUID, args[0], args[1]);

        // Send a message after successfully struck a player
        sender.sendMessage(
                LangConstants.PLUGIN_PREFIX + configHelperLanguage.getString(LangConstants.PixelChatCommand.STRUCK_PLAYER) + " " +
                        ChatColor.RED +
                        ChatColor.BOLD + args[0] + ChatColor.RESET + ".");

        return true;
    }
}