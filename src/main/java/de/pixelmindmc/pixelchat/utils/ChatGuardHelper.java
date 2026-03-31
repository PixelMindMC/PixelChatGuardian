/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2026 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;

/**
 * Utility class for managing configuration files
 */
public class ChatGuardHelper {
    private final @NotNull PixelChat plugin;
    private final @NotNull LoggingHelper loggingHelper;
    private final @NotNull ConfigHelper configHelper;
    private final @NotNull ConfigHelper configHelperLanguage;

    /**
     * Constructs a ChatGuardHelper object
     */
    public ChatGuardHelper(@NotNull PixelChat plugin) {
        this.plugin = plugin;
        this.loggingHelper = plugin.getLoggingHelper();
        this.configHelper = plugin.getConfigHelper();
        this.configHelperLanguage = plugin.getConfigHelperLanguage();
    }

    /**
     * Notifies the player of their message being blocked, logs the block itself, and also applies the strike system
     *
     * @param player         The player that sent the message
     * @param userMessage    The message that the user sent
     * @param classification The classification of the message
     * @param blockOrCensor  Whether the message should be blocked ({@code true}) or censored ({@code false})
     */
    public void notifyAndStrikePlayer(@NotNull Player player, @NotNull String userMessage, @NotNull MessageClassification classification, boolean blockOrCensor) {
        String chatGuardPrefix = (configHelper.getBoolean(ConfigConstants.ChatGuard.CustomPrefix.ENABLED) ? configHelper.getString(ConfigConstants.ChatGuard.CustomPrefix.FORMAT) + ChatColor.RESET + " " : LangConstants.PLUGIN_PREFIX);

        // Notify player if enabled
        if (configHelper.getBoolean(ConfigConstants.ChatGuard.Notify.USER)) {
            // Debug logger message
            loggingHelper.debug("Notify player");

            String playerMessage = chatGuardPrefix + configHelperLanguage.getString(blockOrCensor ? LangConstants.ChatGuard.Player.MESSAGE_BLOCKED : LangConstants.ChatGuard.Player.MESSAGE_CENSORED) + " " + ChatColor.RED + classification.reason();
            player.sendMessage(playerMessage);
        }

        // Notify online admins with the 'pixelchat.strike-notify' permission if enabled
        if (configHelper.getBoolean(ConfigConstants.ChatGuard.Notify.ADMINS)) {
            // Debug logger message
            loggingHelper.debug("Notify online admins with the 'pixelchat.strike-notify' permission");

            String adminMessage = chatGuardPrefix + configHelperLanguage.getString(blockOrCensor ? LangConstants.ChatGuard.Admin.MESSAGE_BLOCKED : LangConstants.ChatGuard.Admin.MESSAGE_CENSORED).replace("[message]", ChatColor.GRAY + userMessage + ChatColor.RESET).replace("[player]", ChatColor.RED + player.getName() + ChatColor.RESET) + " " + ChatColor.RED + classification.reason();

            Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            for (Player admin : onlinePlayers) {
                if (admin.hasPermission(PermissionConstants.Moderation.STRIKE_NOTIFY)) {
                    admin.sendMessage(adminMessage);
                }
            }
        }

        String loggerMessage = configHelperLanguage.getString(blockOrCensor ? LangConstants.ChatGuard.Admin.MESSAGE_BLOCKED : LangConstants.ChatGuard.Admin.MESSAGE_CENSORED).replace("[MESSAGE]", userMessage).replace("[PLAYER]", player.getName()) + " " + ChatColor.RED + classification.reason();

        loggingHelper.info(loggerMessage);

        if (!classification.isOffensiveLanguage()) {
            return;
        }

        if (configHelper.getBoolean(ConfigConstants.ChatGuard.StrikeSystem.ENABLED)) {
            runStrikeSystem(player.getUniqueId(), player.getName(), classification.reason());
        } else {
            executeCommand(configHelper.getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.CUSTOM_STRIKE), player.getName(), classification.reason());
        }
    }

    /**
     * Runs the built-in strike system on the given player
     * This is executed whenever a message has been blocked and the built-in strike system is enabled
     *
     * @param playerUUID The player uuid to run the strike system on
     * @param playerName The player name to run the strike system on
     * @param reason     The reason why the player's message has been blocked or censored
     */
    public void runStrikeSystem(@NotNull UUID playerUUID, @NotNull String playerName, @NotNull String reason) {
        // Debug logger message
        loggingHelper.debug("Run strike system on " + playerName);

        ConfigHelper configHelperPlayerStrikes = plugin.getConfigHelperPlayerStrikes();
        String action = "NOTHING";

        // Retrieve the player's current strike count
        int strikes = configHelperPlayerStrikes.getInt(playerUUID + ".strikes");

        // Increment the player's strike count
        strikes++;

        // Get the thresholds for kick, temp ban, and permanent ban
        int strikesToKick = configHelper.getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.KICK);
        int strikesToTempBan = configHelper.getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.TEMP_BAN);
        int strikesToBan = configHelper.getInt(ConfigConstants.ChatGuard.StrikeSystem.Thresholds.BAN);

        // Check if the player has reached the threshold for punishment
        if (strikes >= strikesToKick && strikes < strikesToTempBan) {
            // Player has enough strikes to be kicked
            executeCommand(configHelper.getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.KICK), playerName, configHelperLanguage.getString(LangConstants.ChatGuard.Player.KICK) + " " + reason);
            action = "KICK";
        } else if (strikes >= strikesToTempBan && strikes < strikesToBan) {
            // Player has enough strikes to be temporarily banned
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.TEMP_BAN), playerName, configHelperLanguage.getString(LangConstants.ChatGuard.Player.BAN_TEMPORARY) + " " + reason);
            action = "TEMP-BAN";
        } else if (strikes >= strikesToBan) {
            // Player has enough strikes to be permanently banned
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.StrikeSystem.Commands.BAN), playerName, configHelperLanguage.getString(LangConstants.ChatGuard.Player.BAN_PERMANENT) + " " + reason);
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
        loggingHelper.info(playerName + " got a Strike for " + reason + " and now has " + strikes + " " + "strike(s)");
    }

    /**
     * Helper method to allow for command execution in async contexts
     *
     * @param command    The command to execute
     * @param playerName The player name to execute the command on
     * @param reason     The reason for the command
     */
    private void executeCommand(@NotNull String command, @NotNull String playerName, @NotNull String reason) {
        // Replace placeholders with actual values
        String processedCommand = command.replace("<player>", playerName).replace("<reason>", reason);

        // Schedule to execute the task on the next server tick, as it cannot run from an async context (where we are
        // now)
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand));

        // Debug logger message
        loggingHelper.debug("Executed the command: " + processedCommand);
    }

    /**
     * Checks whether the classified message actually violates an active block rule
     *
     * @param classification The classification of the message
     * @return true if the message violates an active block rule, false if no active block rules have been violated
     * by the message
     */
    public boolean messageMatchesEnabledRule(@NotNull MessageClassification classification) {
        boolean blockOffensiveLanguage = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_OFFENSIVE_LANGUAGE);
        boolean blockUsernames = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_USERNAMES);
        boolean blockPasswords = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_PASSWORDS);
        boolean blockHomeAddresses = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_HOME_ADDRESSES);
        boolean blockEmailAddresses = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_EMAIL_ADDRESSES);
        boolean blockWebsites = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_WEBSITES);
        boolean blockSexualContent = configHelper.getBoolean(ConfigConstants.ChatGuard.Rules.BLOCK_SEXUAL_CONTENT);


        return blockOffensiveLanguage && classification.isOffensiveLanguage() || blockUsernames && classification.isUsername() || blockPasswords && classification.isPassword() || blockHomeAddresses && classification.isHomeAddress() || blockEmailAddresses && classification.isEmailAddress() || blockWebsites && classification.isWebsite() || blockSexualContent && classification.isSexualContent();
    }
}