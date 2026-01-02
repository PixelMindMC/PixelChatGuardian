/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.integration.CarbonChatIntegration;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.utils.ChatGuardHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Listener for handling player chat events asynchronously
 */
public class AsyncPlayerChatListener implements Listener {
    private final @NotNull PixelChat plugin;
    private boolean chatGuardEnabled = false;
    private boolean emojiEnabled = false;
    private boolean chatCodesEnabled = false;

    private @NotNull Map<String, String> emojiMap = new HashMap<>();
    private @NotNull Map<String, ChatColor> chatCodesMap = new HashMap<>();
    private @Nullable CarbonChatIntegration carbonChatIntegration = null;

    /**
     * Constructs an AsyncPlayerChatListener object
     *
     * @param plugin The plugin instance
     */
    public AsyncPlayerChatListener(@NotNull PixelChat plugin) {
        this.plugin = plugin;

        // Initialize CarbonChat integration if available
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.PluginSupport.CARBONCHAT) && setupCarbonChatIntegration())
            carbonChatIntegration.registerCarbonChatListener();

        // Chatguard module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.Modules.CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API.KEY);
            plugin.getLoggingHelper().debug("API key is: " + apiKey);
            this.chatGuardEnabled = plugin.getConfigHelper().getBoolean(ConfigConstants.Modules.CHATGUARD) && !apiKey.isEmpty() &&
                    !Objects.equals(apiKey, "API-KEY");
        }

        // Emoji module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.Modules.EMOJIS)) {
            this.emojiEnabled = true;
            this.emojiMap = plugin.getConfigHelperEmojiList().getStringMap(ConfigConstants.Emoji.LIST);
        }

        // Chat codes module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.Modules.CHAT_CODES)) {
            this.chatCodesEnabled = true;
            this.chatCodesMap = plugin.getConfigHelperChatCodesList().getChatColorMap(ConfigConstants.ChatCodes.LIST);
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
    private void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        boolean chatGuardMessageBlocked = false;

        // AI based chat guard module
        if (chatGuardEnabled && carbonChatIntegration == null &&
                !player.hasPermission(PermissionConstants.Moderation.BYPASS_CHAT_MODERATION))
            chatGuardMessageBlocked = checkIfMessageShouldBeBlocked(event, message, player);

        // Emoji module
        if (emojiEnabled && !chatGuardMessageBlocked && player.hasPermission(PermissionConstants.Modules.EMOJIS)) {
            message = replaceMessageEmojis(message, emojiMap);
            event.setMessage(message);
        }

        // Chat codes module
        if (chatCodesEnabled && !chatGuardMessageBlocked && carbonChatIntegration == null &&
                player.hasPermission(PermissionConstants.Modules.CHAT_CODES)) {
            message = replaceMessageChatCodes(message, chatCodesMap);
            event.setMessage(message);
        }
    }

    /**
     * Checks whether a message should be blocked or censored and takes appropriate actions
     *
     * @param event   The message event
     * @param message The message to check
     * @param player  The player that sent the message
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean checkIfMessageShouldBeBlocked(@NotNull AsyncPlayerChatEvent event, @NotNull String message, @NotNull Player player) {
        // Debug logger message
        plugin.getLoggingHelper().debug("Check if the message '" + message + "' from " + player.getName() + " should be blocked");

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        // Check if classification matches any enabled blocking rules
        if (ChatGuardHelper.messageMatchesEnabledRule(plugin, classification)) {
            boolean blockOrCensor = plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.MESSAGE_HANDLING).equals("BLOCK");
            if (blockOrCensor) event.setCancelled(true);
            else event.setMessage("*".repeat(message.length()));

            ChatGuardHelper.notifyAndStrikePlayer(plugin, player, message, classification, blockOrCensor);

            return true; // Message has been blocked or censored
        }
        return false;
    }

    /**
     * Helper method to apply a given map of emojis to replacements to the given string
     *
     * @param message  The original message
     * @param emojiMap The map of emojis and replacements
     * @return The message with the applied replacements
     */
    private @NotNull String replaceMessageEmojis(@NotNull String message, @NotNull Map<String, String> emojiMap) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            if (message.contains(entry.getKey())) {
                // Debug logger message
                plugin.getLoggingHelper().debug("Replacing: " + entry.getKey() + " with: " + entry.getValue());

                // Replace each occurrence of the placeholder (key) in the string with its value
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        return message;
    }

    /**
     * Helper method to convert color and format codes with :codename: format to a formatted message
     *
     * @param message      The original message
     * @param chatCodesMap The map of chat codes and replacements
     * @return The message with the formatting
     */
    private @NotNull String replaceMessageChatCodes(@NotNull String message, @NotNull Map<String, ChatColor> chatCodesMap) {
        for (Map.Entry<String, ChatColor> entry : chatCodesMap.entrySet()) {
            if (message.contains(entry.getKey())) {
                // Debug logger message
                plugin.getLoggingHelper().debug("Replacing: " + entry.getKey() + " with: " + entry.getValue());

                // Replace each occurrence of the placeholder (key) in the string with its value
                message = message.replace(entry.getKey(), entry.getValue().toString());
            }
        }

        return message;
    }
}