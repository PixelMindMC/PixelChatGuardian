/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles integration with CarbonChat
 */
public class CarbonChatIntegration {
    private final PixelChat plugin;

    /**
     * Constructs a CarbonChatIntegration object
     *
     * @param plugin The plugin instance
     */
    public CarbonChatIntegration(PixelChat plugin) {
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

            boolean chatGuardMessageBlocked = false;

            // AI based chat guard module
            if (!carbonPlayer.hasPermission(PermissionConstants.PIXELCHAT_BYPASS_CHAT_MODERATION))
                chatGuardMessageBlocked = checkIfMessageShouldBeBLocked(event, messageComponent);

            // Chat codes module
            if (!chatGuardMessageBlocked && carbonPlayer.hasPermission(PermissionConstants.PIXELCHAT_CHAT_CODES)) {
                messageComponent = convertChatCodes(messageComponent);
                if (messageComponent != null) event.message(messageComponent);
            }
        });
    }

    /**
     * Checks whether a message should be blocked or censored and takes appropriate actions for CarbonChat
     *
     * @param event     The CarbonChatEvent
     * @param messageComponent The component to check
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean checkIfMessageShouldBeBLocked(CarbonChatEvent event, Component messageComponent) {
        // Regular expression to extract the content
        Pattern pattern = Pattern.compile("content=\"(.*?)\"");
        Matcher matcher = pattern.matcher(messageComponent.toString());

        String content = null;
        if (matcher.find()) content = matcher.group(1);  // Extracts the content

        if (content == null) return false;

        // Debug logger message
        plugin.getLoggingHelper().debug("Check if the message '" + content + "' should be blocked for the CarbonChat integration");

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(content);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        if (!classification.block()) return false;

        boolean blockMessage = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING).equals("BLOCK");
        if (blockMessage) event.cancelled(true);
        else event.message(Component.text("*".repeat(content.length())));

        ChatGuardHelper.notifyAndStrikePlayer(plugin, Bukkit.getPlayer(event.sender().uuid()), content, classification, blockMessage);

        return true;
    }

    /**
     * Helper method to convert color and format codes with :codename: format to a formatted message component
     *
     * @param messageComponent The original message component
     * @return The message component with the formatting
     */
    private Component convertChatCodes(Component messageComponent) {
        // TODO

        return messageComponent;
    }
}