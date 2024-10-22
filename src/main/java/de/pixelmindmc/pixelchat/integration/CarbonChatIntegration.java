/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.integration;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.listener.AsyncPlayerChatListener;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles integration with CarbonChat
 */
public class CarbonChatIntegration {
    private final PixelChat plugin;
    private final AsyncPlayerChatListener listener;

    /**
     * Constructs an CarbonChatIntegration object
     *
     * @param plugin The plugin instance
     */
    public CarbonChatIntegration(PixelChat plugin, AsyncPlayerChatListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    /**
     * Registers the CarbonChat event listener if CarbonChat is enabled
     */
    public void registerCarbonChatListener() {
        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, event -> isMessageBlockedCarbonChat(event, event.message()));
    }

    /**
     * Checks whether a message should be blocked or censored for CarbonChat
     *
     * @param event     The CarbonChatEvent
     * @param component The component to check
     */
    private void isMessageBlockedCarbonChat(CarbonChatEvent event, Component component) {
        // Regular expression to extract the content
        Pattern pattern = Pattern.compile("content=\"(.*?)\"");
        Matcher matcher = pattern.matcher(component.toString());

        String content = null;
        if (matcher.find()) content = matcher.group(1);  // Extracts the content

        if (content == null) return;

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(content);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return;
        }

        if (!classification.block()) return;

        boolean blockMessage = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING).equals("BLOCK");
        if (blockMessage) {
            event.cancelled(true);
        } else event.message(Component.text("*".repeat(content.length())));

        listener.notifyAndStrikeplayer(Bukkit.getPlayer(event.sender().uuid()), content, classification, blockMessage);
    }
}