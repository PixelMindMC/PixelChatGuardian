/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.integration;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import de.pixelmindmc.pixelchat.utils.ChatGuardHelper;
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

    /**
     * Constructs a CarbonChatIntegration object
     *
     * @param plugin The plugin instance
     */
    public CarbonChatIntegration(@NotNull PixelChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the CarbonChat event listener if CarbonChat is enabled
     */
    public void registerCarbonChatListener() {
        // Debug logger message
        plugin.getLoggingHelper().debug("Register CarbonChat listener");

        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, event -> {
            CarbonPlayer carbonPlayer = event.sender();
            Component messageComponent = event.message();

            // AI based chat guard module
            if (!carbonPlayer.hasPermission(PermissionConstants.Moderation.BYPASS_CHAT_MODERATION))
                checkIfMessageShouldBeBlocked(event, messageComponent);
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
        if (matcher.find()) message = matcher.group(1);  // Extracts the content

        if (message == null) return;

        // Debug logger message
        plugin.getLoggingHelper().debug("Check if the message '" + message + "' should be blocked for the CarbonChat integration");

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return; //Don't block message if there was an error while classifying it
        }

        // Check if classification matches any enabled blocking rules
        if (ChatGuardHelper.messageMatchesEnabledRule(plugin, classification)) {
            boolean blockOrCensor = plugin.getConfigHelper().getString(ConfigConstants.ChatGuard.MESSAGE_HANDLING).equals("BLOCK");
            if (blockOrCensor) event.cancelled(true);
            else event.message(Component.text("*".repeat(message.length())));

            Player player = Bukkit.getPlayer(event.sender().uuid());
            if (player != null) ChatGuardHelper.notifyAndStrikePlayer(plugin, player, message, classification, blockOrCensor);
        }
    }
}