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

package de.pixelmindmc.pixelchat.integration;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.utils.ChatGuardHelper;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import de.pixelmindmc.pixelchat.utils.LoggingHelper;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles integration with CarbonChat
 */
public class CarbonChatIntegration {
    private final @NotNull PixelChat plugin;
    private final @NotNull LoggingHelper loggingHelper;
    private final @NotNull ConfigHelper configHelper;
    private final @NotNull ChatGuardHelper chatGuardHelper;

    /**
     * Constructs a CarbonChatIntegration object
     *
     * @param plugin The plugin instance
     */
    public CarbonChatIntegration(@NotNull PixelChat plugin) {
        this.plugin = plugin;
        this.loggingHelper = plugin.getLoggingHelper();
        this.configHelper = plugin.getConfigHelper();
        this.chatGuardHelper = plugin.getChatGuardHelper();
    }

    /**
     * Registers the CarbonChat event listener if CarbonChat is enabled
     */
    public void registerCarbonChatListener() {
        // Debug logger message
        loggingHelper.debug("Register CarbonChat listener");

        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, event -> {
            CarbonPlayer carbonPlayer = event.sender();
            Component messageComponent = event.message();

            // AI based chat guard module
            if (!carbonPlayer.hasPermission(PermissionConstants.Moderation.BYPASS_CHAT_MODERATION)) {
                checkIfMessageShouldBeBlocked(event, messageComponent);
            }
        });
    }

    /**
     * Checks whether a message should be blocked or censored and takes appropriate actions for CarbonChat
     *
     * @param event            The CarbonChatEvent
     * @param messageComponent The component to check
     */
    private void checkIfMessageShouldBeBlocked(@NotNull CarbonChatEvent event, @NotNull Component messageComponent) {
        // Regular expression to extract the content
        Pattern pattern = Pattern.compile("content=\"(.*?)\"");
        Matcher matcher = pattern.matcher(messageComponent.toString());

        String message = null;
        if (matcher.find()) {
            message = matcher.group(1);  // Extracts the content
        }

        if (message == null) {
            return;
        }

        // Debug logger message
        loggingHelper.debug("Check if the message '" + message + "' should be blocked for the CarbonChat integration");

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            loggingHelper.error("Failed to classify message for CarbonChat integration: " + exception.getMessage(), exception);

            return; //Don't block message if there was an error while classifying it
        }

        // Check if classification matches any enabled blocking rules
        if (chatGuardHelper.messageMatchesEnabledRule(classification)) {
            boolean blockOrCensor = "BLOCK".equals(configHelper.getString(ConfigConstants.ChatGuard.MESSAGE_HANDLING));
            if (blockOrCensor) {
                event.cancelled(true);
            } else {
                event.message(Component.text("*".repeat(message.length())));
            }

            Player player = Bukkit.getPlayer(event.sender().uuid());
            if (player != null) {
                chatGuardHelper.notifyAndStrikePlayer(player, message, classification, blockOrCensor);
            }
        }
    }
}