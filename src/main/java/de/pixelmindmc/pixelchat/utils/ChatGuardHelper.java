/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
     * @param blockMessage   Whether the message should be blocked ({@code true}) or censored ({@code false})
     */
    public static void notifyAndStrikePlayer(PixelChat plugin, Player player, String userMessage, MessageClassification classification, boolean blockMessage) {
        // Debug logger message
        plugin.getLoggingHelper().debug("Notify and strike player");

        String chatguardPrefix;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_ENABLE_CUSTOM_CHATGUARD_PREFIX)) {
            chatguardPrefix = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_CUSTOM_CHATGUARD_PREFIX) + ChatColor.RESET + " ";
        } else chatguardPrefix = LangConstants.PLUGIN_PREFIX;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_NOTIFY_USER))
            player.sendMessage(chatguardPrefix + plugin.getConfigHelperLanguage().getString(blockMessage ? LangConstants.PLAYER_MESSAGE_BLOCKED : LangConstants.PLAYER_MESSAGE_CENSORED) + " " + ChatColor.RED + classification.reason());

        plugin.getLoggingHelper().info("Message by " + player.getName() + (blockMessage ? " has been blocked: " : " has been censored: ") + userMessage);

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_USE_BUILT_IN_STRIKE_SYSTEM)) {
            runStrikeSystem(plugin, player.getUniqueId(), player.getName(), classification.reason());
        } else
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_CUSTOM_STRIKE_COMMAND), player.getName(), classification.reason());
    }

    /**
     * Runs the built-in strike system on the given player
     * This is executed whenever a message has been blocked and the built-in strike system is enabled
     *
     * @param playerUUID The player uuid to run the strike system on
     * @param playerName The player name to run the strike system on
     * @param reason The reason why the player's message has been blocked or censored
     */
    public static void runStrikeSystem(PixelChat plugin, UUID playerUUID, String playerName, String reason) {
        // Debug logger message
        plugin.getLoggingHelper().debug("Run strike system");

        ConfigHelper configHelperPlayerStrikes = plugin.getConfigHelperPlayerStrikes();
        String action = "NOTHING";

        // Retrieve the player's current strike count
        int strikes = configHelperPlayerStrikes.getInt(playerUUID + ".strikes");

        // Increment the player's strike count
        strikes++;

        // Get the thresholds for kick, temp ban, and permanent ban
        int strikesToKick = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_KICK);
        int strikesToTempBan = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_TEMP_BAN);
        int strikesToBan = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_BAN);

        // Check if the player has reached the threshold for punishment
        if (strikes >= strikesToKick && strikes < strikesToTempBan) {
            // Player has enough strikes to be kicked
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_KICK_COMMAND), playerName, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + reason);
            action = "KICK";
        } else if (strikes >= strikesToTempBan && strikes < strikesToBan) {
            // Player has enough strikes to be temporarily banned
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_TEMP_BAN_COMMAND), playerName, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_TEMPORARY) + " " + reason);
            action = "TEMP-BAN";
        } else if (strikes >= strikesToBan) {
            // Player has enough strikes to be permanently banned
            executeCommand(plugin, plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_BAN_COMMAND), playerName, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + " " + reason);
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
     * @param command The command to execute
     * @param playerName  The player name to execute the command on
     * @param reason  The reason for the command
     */
    private static void executeCommand(PixelChat plugin, String command, String playerName, String reason) {
        // Replace placeholders with actual values
        String processedCommand = command.replace("<player>", playerName).replace("<reason>", reason);

        // Schedule to execute the task on the next server tick, as it cannot run from an async context (where we are now)
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand));

        // Debug logger message
        plugin.getLoggingHelper().debug("Executed the command: " + processedCommand);
    }
}
