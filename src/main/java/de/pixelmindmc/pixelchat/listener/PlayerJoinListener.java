/*
 * Copyright (C) 2024 - 2026 PixelMindMC
 *
 * This file is part of PixelChat Guardian.
 *
 * PixelChat Guardian is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * PixelChat Guardian is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PixelChat Guardian.
 * If not, see <https://www.gnu.org/licenses/>.
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
    private final @NotNull ConfigHelper configHelper;
    private final @NotNull ConfigHelper configHelperLanguage;

    /**
     * Constructs an PlayerJoinListener object
     *
     * @param plugin The plugin instance
     */
    public PlayerJoinListener(@NotNull PixelChat plugin) {
        this.configHelper = plugin.getConfigHelper();
        this.configHelperLanguage = plugin.getConfigHelperLanguage();
    }

    /**
     * Event handler for the PlayerJoinEvent
     *
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player has the required permission
        if (!player.isOp() || !player.hasPermission(PermissionConstants.FULL_PERMISSIONS)) {
            return;
        }

        // Retrieve API key from config
        String apiKey = configHelper.getString(ConfigConstants.API.KEY);

        // Check if config file exists
        if (!configHelper.getFileExist()) {
            player.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.Global.FIRST_TIME_MESSAGE));
        } else if (apiKey.isEmpty() || configHelper.getFileExist() && Objects.equals(apiKey, "API-KEY")) {
            player.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.Global.NO_API_KEY_SET));
        }
    }
}