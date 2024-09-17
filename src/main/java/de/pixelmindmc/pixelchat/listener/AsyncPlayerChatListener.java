/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.model.ConfigConstants;
import de.pixelmindmc.pixelchat.model.LangConstants;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.model.MessageClassification.Action;
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
        String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
        if (plugin.getConfigHelper().getConfig().getBoolean(ConfigConstants.CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null) {
            MessageClassification classification;
            try {
                classification = plugin.getAPIHelper().classifyMessage(message);
            } catch (Exception exception) {
                plugin.getLogger().warning(exception.toString());
                return;
            }

            if (classification.block()) {
                event.setCancelled(true);
                switch (classification.action()) {
                    case Action.KICK ->
                            kickPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + classification.reason());
                    case Action.BAN ->
                            banPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + " " + classification.reason());
                    case Action.NONE -> {
                        if (player.hasMetadata("STRIKE")) {
                            player.removeMetadata("STRIKE", plugin);
                            kickPlayer(player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + " " + classification.reason());
                        }
                        player.setMetadata("STRIKE", new FixedMetadataValue(plugin, player.getName()));
                        player.sendMessage(plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + classification.reason());
                    }
                }
                return;
            }
        }

        if (plugin.getConfigHelper().getConfig().getBoolean(ConfigConstants.EMOJIS)) {
            // Initialize emoji map
            initializeEmojiMap();

            message = convertAsciiToEmojis(message);
            event.setMessage(message);
        }
    }


    //Helper method to allow for kicks in async contexts
    private void kickPlayer(Player player, String reason) {
        player.kickPlayer(reason);
    }

    //Helper method to allow for bans in async contexts
    private void banPlayer(Player player, String reason) {
        player.ban(reason, (Date) null, null, true); //s1 is source, b is kickPlayer
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
