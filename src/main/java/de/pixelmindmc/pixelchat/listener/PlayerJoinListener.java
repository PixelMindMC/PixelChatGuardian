/*
 * This file is part of PixelChatGuardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Listener for handling player join events
 */
public class PlayerJoinListener implements Listener {
    private final PixelChat plugin;

    /**
     * Constructs an PlayerJoinListener object
     *
     * @param plugin The plugin instance
     */
    public PlayerJoinListener(@NotNull PixelChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Event handler for the PlayerJoinEvent
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        ConfigHelper configHelper = plugin.getConfigHelper();
        Player player = event.getPlayer();

        // Check if the player has the required permission
        if (!player.isOp() || !player.hasPermission(PermissionConstants.PIXELCHAT_FULL_PERMISSIONS)) return;

        // Retrieve API key from config
        String apiKey = configHelper.getString(ConfigConstants.API_KEY);

        // Check if config file exists
        if (!configHelper.getFileExist()) {
            player.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED +
                    plugin.getConfigHelperLanguage().getString(LangConstants.FIRST_TIME_MESSAGE));
        } else if (configHelper.getFileExist() && Objects.equals(apiKey, "API-KEY") || apiKey == null) player.sendMessage(
                LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.NO_API_KEY_SET));
    }
}