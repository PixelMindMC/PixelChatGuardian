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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles integration with CarbonChat
 */
public class CarbonChatIntegration {
    private final PixelChat plugin;
    private boolean chatCodesEnabled = false;

    private Map<String, String> chatCodesMap = new HashMap<>();

    /**
     * Constructs a CarbonChatIntegration object
     *
     * @param plugin The plugin instance
     */
    public CarbonChatIntegration(@NotNull PixelChat plugin) {
        this.plugin = plugin;

        // Chat codes module
        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHAT_CODES)) {
            this.chatCodesEnabled = true;
            this.chatCodesMap = plugin.getConfigHelper().getStringMap(ConfigConstants.CHAT_CODES_LIST);
        }
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
            if (chatCodesEnabled && !chatGuardMessageBlocked && carbonPlayer.hasPermission(PermissionConstants.PIXELCHAT_CHAT_CODES)) {
                messageComponent = replaceMessageChatCodes(messageComponent, chatCodesMap);
                event.message(messageComponent);
            }
        });
    }

    /**
     * Checks whether a message should be blocked or censored and takes appropriate actions for CarbonChat
     *
     * @param event            The CarbonChatEvent
     * @param messageComponent The component to check
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean checkIfMessageShouldBeBLocked(@NotNull CarbonChatEvent event, @NotNull Component messageComponent) {
        // Regular expression to extract the content
        Pattern pattern = Pattern.compile("content=\"(.*?)\"");
        Matcher matcher = pattern.matcher(messageComponent.toString());

        String message = null;
        if (matcher.find()) message = matcher.group(1);  // Extracts the content

        if (message == null) return false;

        // Debug logger message
        plugin.getLoggingHelper().debug("Check if the message '" + message + "' should be blocked for the CarbonChat integration");

        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        // Retrieve chatguard-rules
        boolean blockOffensiveLanguage = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_OFFENSIVE_LANGUAGE);
        boolean blockUsernames = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_USERNAMES);
        boolean blockPasswords = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_PASSWORDS);
        boolean blockHomeAddresses = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_HOME_ADDRESSES);
        boolean blockEmailAddresses = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_EMAIL_ADDRESSES);
        boolean blockWebsites = plugin.getConfigHelper().getBoolean(ConfigConstants.CHTAGUARD_RULES_BLOCK_WEBSITES);

        // Check if classification matches any enabled blocking rules
        if ((classification.isOffensiveLanguage() && blockOffensiveLanguage) || (classification.isUsername() && blockUsernames) ||
                (classification.isPassword() && blockPasswords) || (classification.isHomeAddress() && blockHomeAddresses) ||
                (classification.isEmailAddress() && blockEmailAddresses) || (classification.isWebsite() && blockWebsites)) {
            boolean blockOrCensor = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING).equals("BLOCK");
            if (blockOrCensor) event.cancelled(true);
            else event.message(Component.text("*".repeat(message.length())));

            Player player = Bukkit.getPlayer(event.sender().uuid());
            ChatGuardHelper.notifyAndStrikePlayer(plugin, player, message, classification, blockOrCensor);

            return true; // Message has been blocked or censored
        }
        return false;
    }

    /**
     * Helper method to convert color and format codes with :codename: format to a formatted message component
     *
     * @param messageComponent The original message component
     * @param chatCodesMap     The map of chat codes and replacements
     * @return The message component with the formatting
     */
    private Component replaceMessageChatCodes(@NotNull Component messageComponent, @NotNull Map<String, String> chatCodesMap) {
        // Map of color codes
        Map<String, NamedTextColor> formattedColorCodesMap = Map.ofEntries(Map.entry("black", NamedTextColor.BLACK), Map.entry("dark_blue", NamedTextColor.DARK_BLUE), Map.entry("dark_green", NamedTextColor.DARK_GREEN), Map.entry("dark_aqua", NamedTextColor.DARK_AQUA), Map.entry("dark_red", NamedTextColor.DARK_RED), Map.entry("dark_purple", NamedTextColor.DARK_PURPLE), Map.entry("gold", NamedTextColor.GOLD), Map.entry("gray", NamedTextColor.GRAY), Map.entry("dark_gray", NamedTextColor.DARK_GRAY), Map.entry("blue", NamedTextColor.BLUE), Map.entry("green", NamedTextColor.GREEN), Map.entry("aqua", NamedTextColor.AQUA), Map.entry("red", NamedTextColor.RED), Map.entry("light_purple", NamedTextColor.LIGHT_PURPLE), Map.entry("yellow", NamedTextColor.YELLOW), Map.entry("white", NamedTextColor.WHITE));

        // Map of formatting codes
        Map<String, TextDecoration> formattedTextDecorationCodesMap = Map.ofEntries(Map.entry("obfuscated", TextDecoration.OBFUSCATED), Map.entry("bold", TextDecoration.BOLD), Map.entry("strikethrough", TextDecoration.STRIKETHROUGH), Map.entry("underline", TextDecoration.UNDERLINED), Map.entry("italic", TextDecoration.ITALIC));

        String content = ((TextComponent) messageComponent).content();
        TextComponent.Builder builder = Component.text(); // Builder for the new component message

        int lastIndex = 0; // Track the last processed index in the content string
        Pattern pattern = Pattern.compile(":(\\w+):"); // Pattern to find color and format codes like :blue:
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String codeKey = matcher.group(1); // Extract the code key
            int startIndex = matcher.start();

            // Add unformatted text before the matched code
            if (startIndex > lastIndex) {
                builder.append(Component.text(content.substring(lastIndex, startIndex)));
            }

            // Check if the codeKey is a color or a text decoration
            if (formattedColorCodesMap.containsKey(codeKey)) {
                NamedTextColor textColor = formattedColorCodesMap.get(codeKey);

                // Get the text following the code and add it with the specified color
                int textStart = matcher.end();
                int nextSpaceIndex = content.indexOf(' ', textStart);
                String colorText = content.substring(textStart);

                builder.append(Component.text(colorText).color(textColor));
                lastIndex = textStart + colorText.length();

                // Debug logger message
                plugin.getLoggingHelper().debug("Replacing: " + ":" + codeKey + ":" + " with: " + textColor);
            } else if (formattedTextDecorationCodesMap.containsKey(codeKey)) {
                // Handle text decoration similarly if needed
                TextDecoration textDecoration = formattedTextDecorationCodesMap.get(codeKey);
                // Apply decoration and move lastIndex appropriately
                int textStart = matcher.end();
                int nextSpaceIndex = content.indexOf(' ', textStart);
                String decoratedText = (content.substring(textStart));

                builder.append(Component.text(decoratedText).decorate(textDecoration));
                lastIndex = textStart + decoratedText.length();

                // Debug logger message
                plugin.getLoggingHelper().debug("Replacing: " + ":" + codeKey + ":" + " with: " + textDecoration);
            } else {
                // If the code is not recognized, append as plain text
                builder.append(Component.text(matcher.group(0)));
                lastIndex = matcher.end();
            }
        }

        // Append any remaining text after the last processed code
        if (lastIndex < content.length()) {
            builder.append(Component.text(content.substring(lastIndex)));
        }

        return builder.build();
    }
}