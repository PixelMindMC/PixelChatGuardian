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
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Listener for handling player chat events asynchronously

public class AsyncPlayerChatListener implements Listener {
    private static final String STRIKE_KEY = "STRIKE";
    private final PixelChat plugin;
    // Map to store emoji translations
    private Map<String, String> emojiMap = new HashMap<>();
    private boolean chatGuardEnabled = false;
    private boolean emojiEnabled = false;

    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
            this.chatGuardEnabled = plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null;
        }

        if(plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            emojiEnabled = true;
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);
        }
    }

    // Event handler for the AsyncPlayerChatEvent
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guard
        if (chatGuardEnabled && isMessageBlocked(event, message, player))
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
     * @param player  The player that sent the mesage
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean isMessageBlocked(AsyncPlayerChatEvent event, String message, Player player) {
        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLogger().warning(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        if (!classification.block())
            return false;

        event.setCancelled(true);

        if (!player.hasMetadata(STRIKE_KEY))
            player.setMetadata(STRIKE_KEY, new FixedMetadataValue(plugin, 0));

        int strikes = player.getMetadata(STRIKE_KEY).get(0).asInt() + 1;
        player.setMetadata(STRIKE_KEY, new FixedMetadataValue(plugin, strikes));

        int strikesToKick = 2; //TODO In config-wert ändern
        int strikesToBan = 4; //TODO In config-wert ändern

        if (strikes >= strikesToKick && strikes < strikesToBan) {
            kickPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + classification.reason());
        } else if (strikes >= strikesToBan) {
            banPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + classification.reason());
        } else {
            player.sendMessage(
                    LangConstants.PLUGIN_PREFIX +
                            ChatColor.RED +
                            plugin.getConfigHelperLanguage().getString(LangConstants.MESSAGE_BLOCKED) +
                            ChatColor.RESET +
                            classification.reason());
        }
        return true;
    }

    // Helper method to allow for kicks in async contexts
    private void kickPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(plugin, e -> player.kickPlayer(reason));
    }

    // Helper method to allow for bans in async contexts
    private void banPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(plugin, e -> player.ban(reason, (Date) null, null, true)); //s1 is source, b is kickPlayer
    }

    // Helper method to convert ascii to emojis
    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}
