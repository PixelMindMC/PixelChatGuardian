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
import org.bukkit.ChatColor;
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

        // Initialize CarbonChat integration if available
        if (plugin.getConfigHelper()
                .getBoolean(ConfigConstants.PLUGIN_SUPPORT_CARBONCHAT) && setupCarbonChatIntegration())
            carbonChatIntegration.registerCarbonChatListener();

        // Chatguard module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
            this.chatGuardEnabled = plugin.getConfigHelper()
                    .getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null;
        }

        // Emoji module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            emojiEnabled = true;
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);
        }

        // Chat codes module
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

        boolean chatGuardMessageBlocked = false;

        // AI based chat guard module
        if (chatGuardEnabled && carbonChatIntegration == null && !player.hasPermission(PermissionConstants.PIXELCHAT_BYPASS_CHAT_MODERATION))
            chatGuardMessageBlocked = checkIfMessageShouldBeBLocked(event, message, player);

        // Emoji module
        if (emojiEnabled && !chatGuardMessageBlocked && player.hasPermission(PermissionConstants.PIXELCHAT_EMOJIS)) {
            message = replaceMessageEmojis(message, emojiMap);
            event.setMessage(message);
        }

        // Chat codes module
        if (chatCodesEnabled && !chatGuardMessageBlocked && carbonChatIntegration == null && player.hasPermission(PermissionConstants.PIXELCHAT_CHAT_CODES)) {
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
    private boolean checkIfMessageShouldBeBLocked(AsyncPlayerChatEvent event, String message, Player player) {
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

        boolean blockMessage = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING)
                .equals("BLOCK");
        if (blockMessage) event.setCancelled(true);
        else event.setMessage("*".repeat(message.length()));

        ChatGuardHelper.notifyAndStrikePlayer(plugin, player, message, classification, blockMessage);

        return true;
    }

    /**
     * Helper method to apply a given map of emojis to replacements to the given string
     *
     * @param message  The original message
     * @param emojiMap The map of emojis and replacements
     * @return The message with the applied replacements
     */
    private String replaceMessageEmojis(String message, Map<String, String> emojiMap) {
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
    private String replaceMessageChatCodes(String message, Map<String, String> chatCodesMap) {
        Map<String, ChatColor> formattedChatCodesMap = Map.ofEntries(
                // Color codes
                Map.entry("black", ChatColor.BLACK),
                Map.entry("dark_blue", ChatColor.DARK_BLUE),
                Map.entry("dark_green", ChatColor.DARK_GREEN),
                Map.entry("dark_aqua", ChatColor.DARK_AQUA),
                Map.entry("dark_red", ChatColor.DARK_RED),
                Map.entry("dark_purple", ChatColor.DARK_PURPLE),
                Map.entry("gold", ChatColor.GOLD),
                Map.entry("gray", ChatColor.GRAY),
                Map.entry("dark_gray", ChatColor.DARK_GRAY),
                Map.entry("blue", ChatColor.BLUE),
                Map.entry("green", ChatColor.GREEN),
                Map.entry("aqua", ChatColor.AQUA),
                Map.entry("red", ChatColor.RED),
                Map.entry("light_purple", ChatColor.LIGHT_PURPLE),
                Map.entry("yellow", ChatColor.YELLOW),
                Map.entry("white", ChatColor.WHITE),

                // Formatting codes
                Map.entry("obfuscated", ChatColor.MAGIC),
                Map.entry("bold", ChatColor.BOLD),
                Map.entry("strikethrough", ChatColor.STRIKETHROUGH),
                Map.entry("underline", ChatColor.UNDERLINE),
                Map.entry("italic", ChatColor.ITALIC),
                Map.entry("reset", ChatColor.RESET)
        );

        // Iterate through each chat code replacement
        for (Map.Entry<String, String> entry : chatCodesMap.entrySet()) {
            if (message.contains(entry.getValue())) {
                // Debug logger message
                plugin.getLoggingHelper()
                        .debug("Replacing: " + entry.getValue() + " with: " + formattedChatCodesMap.get(entry.getKey()));

                // Replace each occurrence of the placeholder (key) in the string with its value
                message = message.replace(entry.getValue(),
                        formattedChatCodesMap.get(entry.getKey()).toString());
            }
        }

        return message;
    }
}