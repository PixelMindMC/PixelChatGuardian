/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.integration.CarbonChatIntegration;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Listener for handling player chat events asynchronously
 */
public class AsyncPlayerChatListener implements Listener {
    private final PixelChat plugin;
    private final String chatguardPrefix;
    private boolean chatGuardEnabled = false;
    private boolean emojiEnabled = false;
    // Map to store emoji translations
    private Map<String, String> emojiMap = new HashMap<>();
    private CarbonChatIntegration carbonChatIntegration = null;

    /**
     * Constructs an AsyncPlayerChatListener object
     *
     * @param plugin The plugin instance
     */
    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_ENABLE_CUSTOM_CHATGUARD_PREFIX)) {
            chatguardPrefix = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_CUSTOM_CHATGUARD_PREFIX) + ChatColor.RESET + " ";
        } else chatguardPrefix = LangConstants.PLUGIN_PREFIX;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
            this.chatGuardEnabled = plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null;

            // Initialize CarbonChat integration if available
            if (plugin.getConfigHelper().getBoolean(ConfigConstants.PLUGIN_SUPPORT_CARBONCHAT) && setupCarbonChatIntegration())
                carbonChatIntegration.registerCarbonChatListener();
        }

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            emojiEnabled = true;
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);
        }
    }

    /**
     * Sets up CarbonChat integration if CarbonChat is detected
     *
     * @return true if CarbonChat integration is successfully set up, false otherwise
     */
    private boolean setupCarbonChatIntegration() {
        try {
            Class.forName("net.draycia.carbon.api.CarbonChatProvider");
            carbonChatIntegration = new CarbonChatIntegration(plugin);

            // Debug logger message
            plugin.getLoggingHelper().debug("Using CarbonChat integration");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Event handler for the AsyncPlayerChatEvent
     *
     * @param event The AsyncPlayerChatEvent
     */
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guard
        if (chatGuardEnabled && !player.hasPermission(PermissionConstants.PIXELCHAT_BYPASS_CHAT_MODERATION) && isMessageBlocked(event, message, player))
            return;

        // Emoji module
        if (emojiEnabled && player.hasPermission(PermissionConstants.PIXELCHAT_EMOJIS)) {
            message = convertAsciiToEmojis(message);
            event.setMessage(message);
        }
    }

    /**
     * Checks whether a message should be blocked or allowed through into the chat, and takes appropriate actions
     *
     * @param event   The message event
     * @param message The message to check
     * @param player  The player that sent the message
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean isMessageBlocked(AsyncPlayerChatEvent event, String message, Player player) {
        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        if (!classification.block()) return false;

        boolean blockMessage = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING).equals("BLOCK");
        if (blockMessage) event.setCancelled(true);
        else event.setMessage("*".repeat(message.length()));

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_NOTIFY_USER))
            player.sendMessage(chatguardPrefix + plugin.getConfigHelperLanguage().getString(blockMessage ? LangConstants.PLAYER_MESSAGE_BLOCKED : LangConstants.PLAYER_MESSAGE_CENSORED) + " " + ChatColor.RED + classification.reason());

        plugin.getLoggingHelper().info("Message by " + player.getName() + (blockMessage ? " has been blocked: " : " has been censored: ") + message);

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_USE_BUILT_IN_STRIKE_SYSTEM)) {
            runStrikeSystem(player, classification.reason());
        } else
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_CUSTOM_STRIKE_COMMAND), player, classification.reason());

        return true;
    }

    /**
     * Runs the built-in strike system on the given player
     * This is executed whenever a message has been blocked and the built-in strike system is enabled
     *
     * @param player The player to run the strike system on
     * @param reason The reason why the player's message has been blocked or censored
     */
    private void runStrikeSystem(Player player, String reason) {
        ConfigHelper configHelperPlayerStrikes = plugin.getConfigHelperPlayerStrikes();
        String playerUUID = player.getUniqueId().toString();
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
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_KICK_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + reason);
            action = "KICK";
        } else if (strikes >= strikesToTempBan && strikes < strikesToBan) {
            // Player has enough strikes to be temporarily banned
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_TEMP_BAN_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_TEMPORARY) + " " + reason);
            action = "TEMP-BAN";
        } else if (strikes >= strikesToBan) {
            // Player has enough strikes to be permanently banned
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_BAN_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + " " + reason);
            action = "BAN";
        }

        // Save the player's name in case it hasn't been stored yet
        configHelperPlayerStrikes.set(playerUUID + ".name", player.getName());

        // Save the player's strike count
        configHelperPlayerStrikes.set(playerUUID + ".strikes", strikes);

        // Get the current date and time
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Create a new strike entry with reason and date in the strike history
        String strikePath = playerUUID + ".strikeHistory." + currentDate;
        configHelperPlayerStrikes.set(strikePath + ".reason", reason);
        configHelperPlayerStrikes.set(strikePath + ".action", action);

        // Save changes to the config file
        configHelperPlayerStrikes.saveConfig();

        // Log the new strike count for debugging
        plugin.getLoggingHelper().debug(player.getName() + " got a Strike for " + reason + " and now has " + strikes + " strike(s).");
    }

    /**
     * Helper method to allow for command execution in async contexts
     *
     * @param command The command to execute
     * @param player  The player to execute the command on
     * @param reason  The reason for the command
     */
    private void executeCommand(String command, Player player, String reason) {
        // Replace placeholders with actual values
        String processedCommand = command.replace("<player>", player.getName()).replace("<reason>", reason);

        // Schedule to execute the task on the next server tick, as it cannot run from an async context (where we are now)
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand));
    }

    /**
     * Helper method to convert ascii to emojis
     *
     * @param message The original message
     * @return The message with replaced emojis
     */
    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}
