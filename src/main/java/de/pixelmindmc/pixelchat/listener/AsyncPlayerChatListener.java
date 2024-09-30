/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.listener;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Listener for handling player chat events asynchronously
 */
public class AsyncPlayerChatListener implements Listener {
    private static final String STRIKE_KEY = "STRIKE";
    private final PixelChat plugin;
    // Map to store emoji translations
    private Map<String, String> emojiMap = new HashMap<>();
    private boolean chatGuardEnabled = false;
    private boolean emojiEnabled = false;

    /**
     * Constructs a AsyncPlayerChatListener object
     *
     * @param plugin The plugin instance
     */
    public AsyncPlayerChatListener(PixelChat plugin) {
        this.plugin = plugin;

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) {
            String apiKey = plugin.getConfigHelper().getString(ConfigConstants.API_KEY);
            this.chatGuardEnabled = plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD) && !Objects.equals(apiKey, "API-KEY") && apiKey != null;
        }

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.MODULE_EMOJIS)) {
            emojiEnabled = true;
            emojiMap = plugin.getConfigHelper().getStringMap(ConfigConstants.EMOJI_LIST);
        }
    }

    /**
     * Event handler for the AsyncPlayerChatEvent
     *
     * @param event The AsyncPlayerChatEvent
     */
    @EventHandler
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // AI based chat guard
        if (chatGuardEnabled && isMessageBlocked(event, message, player))
            return;

        // Emoji module
        if (emojiEnabled && player.hasPermission(PermissionConstants.PIXELCHAT_EMOJIS)) {
            message = convertAsciiToEmojis(message);
            event.setMessage(message);
        }
    }

    /**
     * Checks whether a message should be blocked or allowed through into the chat, and takes appropriate actions
     *
     * @param event   The message event
     * @param message The message to check
     * @param player  The player that sent the message
     * @return {@code true} if the message has been blocked, {@code false} if it has been allowed through
     */
    private boolean isMessageBlocked(AsyncPlayerChatEvent event, String message, Player player) {
        MessageClassification classification;
        try {
            classification = plugin.getAPIHelper().classifyMessage(message);
        } catch (MessageClassificationException exception) {
            plugin.getLoggingHelper().error(exception.toString());
            return false; //Don't block message if there was an error while classifying it
        }

        if (!classification.block())
            return false;

        String eventMessage = event.getMessage();
        boolean blockMessage = plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_MESSAGE_HANDLING).equals(ConfigConstants.MESSAGE_HANDLING_BLOCK);
        if (blockMessage)
            event.setCancelled(true);
        else
            event.setMessage("*".repeat(eventMessage.length()));

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_NOTIFY_USER))
            player.sendMessage(
                    LangConstants.PLUGIN_PREFIX + ChatColor.YELLOW +
                            plugin.getConfigHelperLanguage().getString(
                                    blockMessage ? LangConstants.MESSAGE_BLOCKED : LangConstants.MESSAGE_CENSORED
                            ) + ChatColor.RESET + classification.reason()
            );

        plugin.getLoggingHelper().info("Message by " + player.getName() +
                (blockMessage ? " has been blocked: " : " has been censored: ") + message);

        if (plugin.getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_USE_BUILT_IN_STRIKE_SYSTEM)) {
            runStrikeSystem(player, classification.reason());
        } else
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_CUSTOM_STRIKE_COMMAND), player, "");

        return true;
    }

    /**
     * Runs the built-in strike system on the given player.
     * This is executed whenever a message has been blocked and the built-in strike system is enabled.
     *
     * @param player The player to run the strike system on
     * @param reason The reason why the player's message has been blocked or censored
     */
    private void runStrikeSystem(Player player, String reason) {
        if (!player.hasMetadata(STRIKE_KEY))
            player.setMetadata(STRIKE_KEY, new FixedMetadataValue(plugin, 0));

        int strikes = player.getMetadata(STRIKE_KEY).get(0).asInt() + 1;
        player.setMetadata(STRIKE_KEY, new FixedMetadataValue(plugin, strikes));

        plugin.getLoggingHelper().debug(player.getName() + " has " + strikes + " strikes.");

        int strikesToKick = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_KICK);
        int strikesToTempBan = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_TEMP_BAN);
        int strikesToBan = plugin.getConfigHelper().getInt(ConfigConstants.CHATGUARD_STRIKES_BEFORE_BAN);

        if (strikes >= strikesToKick && strikes <= strikesToTempBan) { //Enough strikes to kick, but not (temp)ban
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_KICK_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_KICK) + reason);
        } else if (strikes >= strikesToTempBan && strikes <= strikesToBan) { //Enough strikes to tempban but not ban
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_TEMP_BAN_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_TEMPORARY) + reason);
        } else if (strikes >= strikesToBan) //Enough strikes to permaban
            executeCommand(plugin.getConfigHelper().getString(ConfigConstants.CHATGUARD_BAN_COMMAND), player, plugin.getConfigHelperLanguage().getString(LangConstants.PLAYER_BAN_PERMANENT) + reason);
    }

    /**
     * Helper method to allow for command execution in async contexts
     *
     * @param command The command to execute
     * @param player  The player to execute the command on
     * @param reason  The reason for the command
     */
    private void executeCommand(String command, Player player, String reason) {
        // Replace placeholders with actual values
        String processedCommand = command.replace("<player>", player.getName()).replace("<reason>", reason);

        // Schedule to execute the task on the next server tick, as it cannot run from an async context (where we are now)
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand));
    }

    /**
     * Helper method to convert ascii to emojis
     *
     * @param message The original message
     * @return The message with replaced emojis
     */
    private String convertAsciiToEmojis(String message) {
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
}
