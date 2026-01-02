/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for managing configuration files
 */
public class ChatGuardHelper {

    /**
     * Constructs a ChatGuardHelper object
     */
    private ChatGuardHelper() {
    }

    /**
     * Notifies the player of their message being blocked, logs the block itself, and also applies the strike system
     *
     * @param player         The player that sent the message
     * @param userMessage    The message that the user sent
     * @param classification The classification of the message
     * @param blockOrCensor  Whether the message should be blocked ({@code true}) or censored ({@code false})
     */
    public static void notifyAndStrikePlayer(@NotNull PixelChat plugin, @NotNull Player player, @NotNull String userMessage, @NotNull MessageClassification classification, boolean blockOrCensor) {
        // Debug logger message
        plugin.getLoggingHelper().debug("Notify player");

        String chatGuardPrefix;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.CustomPrefix.ENABLED)) {
            chatGuardPrefix = plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.CustomPrefix.FORMAT) + ChatColor.RESET + " ";
        } else chatGuardPrefix = LangConstants.PLUGIN_PREFIX;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.NOTIFY_USER)) player.sendMessage(chatGuardPrefix +
                plugin.getConfigHelperLanguage()
                        .getString(blockOrCensor ? LangConstants.ChatGuard.MESSAGE_BLOCKED : LangConstants.ChatGuard.MESSAGE_CENSORED) +
                " " + ChatColor.RED + classification.reason());

        plugin.getLoggingHelper()
                .info("Message by " + player.getName() + (blockOrCensor ? " has been blocked: " : " has been censored: ") + userMessage);

        if (!classification.isOffensiveLanguage()) return;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.StrikeSystem.ENABLED)) {
            runStrikeSystem(plugin, player.getUniqueId(), player.getName(), classification.reason());
        } else executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.CUSTOM_STRIKE),
                player.getName(), classification.reason());
    }

    /**
     * Runs the built-in strike system on the given player
     * This is executed whenever a message has been blocked and the built-in strike system is enabled
     *
     * @param playerUUID The player uuid to run the strike system on
     * @param playerName The player name to run the strike system on
     * @param reason     The reason why the player's message has been blocked or censored
     */
    public static void runStrikeSystem(@NotNull PixelChat plugin, @NotNull UUID playerUUID, @NotNull String playerName, @NotNull String reason) {
        // Debug logger message
        plugin.getLoggingHelper().debug("Run strike system on " + playerName);

        ConfigHelper configHelperPlayerStrikes = plugin.getConfigHelperPlayerStrikes();
        String action = "NOTHING";

        // Retrieve the player's current strike count
        int strikes = configHelperPlayerStrikes.getInt(playerUUID + ".strikes");

        // Increment the player's strike count
        strikes++;

        // Get the thresholds for kick, temp ban, and permanent ban
        int strikesToKick = plugin.getConfigHelper().getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.KICK);
        int strikesToTempBan = plugin.getConfigHelper().getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.TEMP_BAN);
        int strikesToBan = plugin.getConfigHelper().getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.BAN);

        // Check if the player has reached the threshold for punishment
        if (strikes >= strikesToKick && strikes < strikesToTempBan) {
            // Player has enough strikes to be kicked
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.KICK), playerName,
                    plugin.getConfigHelperLanguage().getString(LangConstants.ChatGuard.PLAYER_KICK) + " " + reason);
            action = "KICK";
        } else if (strikes >= strikesToTempBan && strikes < strikesToBan) {
            // Player has enough strikes to be temporarily banned
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.TEMP_BAN), playerName,
                    plugin.getConfigHelperLanguage().getString(LangConstants.ChatGuard.PLAYER_TEMP_BAN) + " " + reason);
            action = "TEMP-BAN";
        } else if (strikes >= strikesToBan) {
            // Player has enough strikes to be permanently banned
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.BAN), playerName,
                    plugin.getConfigHelperLanguage().getString(LangConstants.ChatGuard.PLAYER_PERM_BAN) + " " + reason);
            action = "BAN";
        }

        // Save the player's name in case it hasn't been stored yet
        configHelperPlayerStrikes.set(playerUUID + ".name", playerName);

        // Save the player's strike count
        configHelperPlayerStrikes.set(playerUUID + ".strikes", strikes);

        // Get the current date and time
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Create a new strike entry with reason and date in the strike history
        String strikePath = playerUUID + ".strikeHistory." + currentDate;
        configHelperPlayerStrikes.set(strikePath + ".reason", reason);
        configHelperPlayerStrikes.set(strikePath + ".action", action);

        // Log the new strike count for debugging
        plugin.getLoggingHelper().info(playerName + " got a Strike for " + reason + " and now has " + strikes + " strike(s)");
    }

    /**
     * Helper method to allow for command execution in async contexts
     *
     * @param command    The command to execute
     * @param playerName The player name to execute the command on
     * @param reason     The reason for the command
     */
    private static void executeCommand(@NotNull PixelChat plugin, @NotNull String command, @NotNull String playerName, @NotNull String reason) {
        // Replace placeholders with actual values
        String processedCommand = command.replace("<player>", playerName).replace("<reason>", reason);

        // Schedule to execute the task on the next server tick, as it cannot run from an async context (where we are now)
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand));

        // Debug logger message
        plugin.getLoggingHelper().debug("Executed the command: " + processedCommand);
    }

    /**
     * Checks whether the classified message actually violates an active block rule
     *
     * @param classification The classification of the message
     * @return true if the message violates an active block rule, false if no active block rules have been violated by the message
     */
    public static boolean messageMatchesEnabledRule(@NotNull PixelChat plugin, @NotNull MessageClassification classification) {
        boolean blockOffensiveLanguage = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_OFFENSIVE_LANGUAGE);
        boolean blockUsernames = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_USERNAMES);
        boolean blockPasswords = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_PASSWORDS);
        boolean blockHomeAddresses = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_HOME_ADDRESSES);
        boolean blockEmailAddresses = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_EMAIL_ADDRESSES);
        boolean blockWebsites = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_WEBSITES);
        boolean blockSexualContent = plugin.getConfigHelper().getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_SEXUAL_CONTENT);


        return blockOffensiveLanguage && classification.isOffensiveLanguage() || blockUsernames && classification.isUsername() ||
                blockPasswords && classification.isPassword() || blockHomeAddresses && classification.isHomeAddress() ||
                blockEmailAddresses && classification.isEmailAddress() || blockWebsites && classification.isWebsite() ||
                blockSexualContent && classification.isSexualContent();
    }
}