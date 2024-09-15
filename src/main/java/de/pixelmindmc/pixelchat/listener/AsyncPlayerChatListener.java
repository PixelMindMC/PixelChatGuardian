/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Listener for handling player chat events asynchronously
public class AsyncPlayerChatListener implements Listener {
    private final PixelChat plugin;
    // Map to store emoji translations
    private final Map<String, String> emojiMap = new HashMap<>();

    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;
    }

    // Event handler for the AsyncPlayerChatEvent
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guard
        String apiKey = plugin.getConfigHelper().getString("api-key");
        if (plugin.getConfigHelper().getConfig().getBoolean("modules.chatguard") && !Objects.equals(apiKey, "API-KEY") && apiKey != null) {
            String action;
            try {
                action = plugin.getAPIHelper().makeApiCall(message);
            } catch (Exception exception) {
                plugin.getLogger().warning(exception.toString());
                return;
            }

            String reason = "";
            switch (action) {
                case "BLOCK" -> {
                    event.setCancelled(true);
                    if (player.hasMetadata("STRIKE")) {
                        player.removeMetadata("STRIKE", plugin);
                        Bukkit.getScheduler().runTask(plugin, () -> kickPlayer(player, plugin.getConfigHelperLanguage().getString("player-kick") + " " + reason));
                        return;
                    }
                    player.setMetadata("STRIKE", new FixedMetadataValue(plugin, player.getName()));
                    player.sendMessage(plugin.getConfigHelperLanguage().getString("player-kick") + reason);
                    return;
                }
                case "KICK" -> {
                    event.setCancelled(true);
                    player.kickPlayer(plugin.getConfigHelperLanguage().getString("player-kick") + reason);
                    return;
                }
            }
        }


        if (plugin.getConfigHelper().getConfig().getBoolean("modules.emojis")) {
            // Initialize emoji map
            initializeEmojiMap();

            message = convertAsciiToEmojis(message);
            event.setMessage(message);
        }
    }

    private void kickPlayer(Player player, String reason) {
        player.kickPlayer(reason);
    }

    private void initializeEmojiMap() {
        emojiMap.put(":-)", "😊");
        emojiMap.put(":-(", "😢");
        emojiMap.put("<3", "❤️");
    }

    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}