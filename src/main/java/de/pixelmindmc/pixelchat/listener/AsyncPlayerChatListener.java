/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.integration.CarbonChatIntegration;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.utils.ChatGuardHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Listener for handling player chat events asynchronously
 */
public class AsyncPlayerChatListener implements Listener {
    private final PixelChat plugin;
    private boolean chatGuardEnabled = false;
    private boolean emojiEnabled = false;
    private boolean chatCodesEnabled = false;

    private Map<String, String> emojiMap = new HashMap<>();
    private Map<String, String> chatCodesMap = new HashMap<>();
    private CarbonChatIntegration carbonChatIntegration = null;

    /**
     * Constructs an AsyncPlayerChatListener object
     *
     * @param plugin The plugin instance
     */
    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
            this.chatGuardEnabled = plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null;

            // Initialize CarbonChat integration if available
            if (chatGuardEnabled && plugin.getConfigHelper().getBoolean(ConfigConstants.PLUGIN_SUPPORT_CARBONCHAT) && setupCarbonChatIntegration())
                carbonChatIntegration.registerCarbonChatListener();
        }

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            emojiEnabled = true;
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);
        }

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHAT_CODES)) {
            chatCodesEnabled = true;
            chatCodesMap = plugin.getConfigHelper().getStringMap(ConfigConstants.CHAT_CODES_LIST);
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
        if (chatGuardEnabled && carbonChatIntegration == null && !player.hasPermission(PermissionConstants.PIXELCHAT_BYPASS_CHAT_MODERATION) && isMessageBlocked(event, message, player))
            return;

        // Emoji module
        if (emojiEnabled && player.hasPermission(PermissionConstants.PIXELCHAT_EMOJIS)) {
            String newMessage = convertAsciiToEmojis(message);
            event.setMessage(newMessage);
        }

        // Color module
        if (chatCodesEnabled && player.hasPermission(PermissionConstants.PIXELCHAT_CHAT_CODES)) {
            String newMessage = convertChatCodesToMinecraftChatCodes(message);
            event.setMessage(newMessage);
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
        // Debug logger message
        plugin.getLoggingHelper().debug("Check if the message '" + message + "' should be blocked");
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

        ChatGuardHelper.notifyAndStrikePlayer(plugin, player, message, classification, blockMessage);

        return true;
    }

    /**
     * Helper method to convert ascii to emojis
     *
     * @param message The original message
     * @return The message with replaced emojis
     */
    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            if (message.contains(entry.getKey())) {
                plugin.getLoggingHelper().debug("Replacing: " + entry.getKey() + " with: " + entry.getValue());

                // Update newMessage with each replacement
                message = message.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        return message;
    }

    /**
     * Helper method to convert color and format codes with :codename: format to minecraft chat codes
     *
     * @param message The original message
     * @return The message with replaced color and format codes
     */
    private String convertChatCodesToMinecraftChatCodes(String message) {
        for (Map.Entry<String, String> entry : chatCodesMap.entrySet()) {
            if (message.contains(entry.getKey())) {
                plugin.getLoggingHelper().debug("Replacing: " + entry.getKey() + " with: " + entry.getValue());

                // Update newMessage with each replacement
                message = message.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        return message;
    }
}