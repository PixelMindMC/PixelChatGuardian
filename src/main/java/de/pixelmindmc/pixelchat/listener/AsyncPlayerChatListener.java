/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.model.MessageClassification;
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

    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;
    }

    // Event handler for the AsyncPlayerChatEvent
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guard
        String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null) {
            MessageClassification classification;
            try {
                classification = plugin.getAPIHelper().classifyMessage(message);
            } catch (MessageClassificationException exception) {
                plugin.getLogger().warning(exception.toString());
                return;
            }

            if (classification.block()) {
                event.setCancelled(true);
                switch (classification.action()) {
                    case KICK ->
                            kickPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + classification.reason());
                    case BAN ->
                            banPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + " " + classification.reason());
                    case NONE -> {
                        if (player.hasMetadata(STRIKE_KEY)) {
                            player.removeMetadata(STRIKE_KEY, plugin);
                            kickPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + classification.reason());
                        }
                        player.setMetadata(STRIKE_KEY, new FixedMetadataValue(plugin, player.getName()));
                        player.sendMessage(plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + classification.reason());
                    }
                }
                return;
            }
        }

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            // Initialize emoji map
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);

            message = convertAsciiToEmojis(message);
            event.setMessage(message);
        }
    }

    // Helper method to allow for kicks in async contexts
    private void kickPlayer(Player player, String reason) {
        player.kickPlayer(reason);
    }

    // Helper method to allow for bans in async contexts
    private void banPlayer(Player player, String reason) {
        player.ban(reason, (Date) null, null, true); //s1 is source, b is kickPlayer
    }

    // Helper method to convert ascii to emojis
    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}
