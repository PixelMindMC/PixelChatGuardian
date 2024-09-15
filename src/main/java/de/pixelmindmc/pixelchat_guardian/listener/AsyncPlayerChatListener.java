/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 Gaming12846
 */

package de.pixelmindmc.pixelchat_guardian.listener;

import de.pixelmindmc.pixelchat_guardian.PixelChat_Guardian;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

// Listener for handling player chat events asynchronously
public class AsyncPlayerChatListener implements Listener {
    private final PixelChat_Guardian plugin;

    public AsyncPlayerChatListener(PixelChat_Guardian plugin) {
        this.plugin = plugin;
    }

    // Event handler for the AsyncPlayerChatEvent
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guardian
        String apiKey = plugin.getConfigHelper().getString("api-key");
        if (!Objects.equals(apiKey, "API-KEY") && apiKey != null) {
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
                        Bukkit.getScheduler().runTask(plugin, () -> kickPlayer(player, plugin.getConfigHelperLanguage().getString("player-kick") + reason));
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

        //TODO emoji-ersetzungs-system
        player.hasPermission("pixelchat_guardian.emojis");
        if (true) {
            switch (message) {
                case "<3" -> {
                    event.setCancelled(true);
                    event.setMessage("<" + player.getDisplayName() + "> " + "❤");
                    player.sendMessage("Ich lieb dich auch Schatzi ❤");
                }
                case ":D" -> {
                    event.setCancelled(true);
                    event.setMessage("�");
                }
            }
        }
    }

    private void kickPlayer(Player player, String reason) {
        player.kickPlayer(reason);
    }
}