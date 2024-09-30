/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

/**
 * CommandExecutor for handling the "pixelchat" command, the main command for the plugin
 */
public class PixelChatCommand implements CommandExecutor {
    private final PixelChat plugin;

    /**
     * Constructs a PixelChatCommand object
     *
     * @param plugin The plugin instance
     */
    public PixelChatCommand(PixelChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the execution of the "pixelchat" command
     *
     * @param sender The source of the command (player or console)
     * @param cmd    The command being executed
     * @param label  The alias used to invoke the command
     * @param args   The arguments provided with the command
     * @return true to indicate the command was processed
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigHelper configHelperLanguage = plugin.getConfigHelperLanguage();

        // Display usage information if no arguments are provided
        if (args.length == 0) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + label + " <version|reload|remove-strikes>");
            return true;
        }

        // Trollplus subcommand selection
        switch (args[0].toLowerCase()) {
            case "version" -> handleVersionSubcommand(sender, label, args, configHelperLanguage);
            case "reload" -> handleReloadSubcommand(sender, label, args, configHelperLanguage);
            case "remove-strikes" ->
                    handleRemoveStrikesSubcommand(sender, label, args, plugin.getConfigHelperPlayerStrikes(), configHelperLanguage);
            default ->
                    sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " <version|reload|remove-strikes>");
        }

        return true;
    }

    /**
     * Handles the "version" subcommand to display plugin information
     *
     * @param sender     The command sender
     * @param label      The label
     * @param args       The arguments
     * @param langConfig The ConfigHelper for the languageConfig
     */
    private void handleVersionSubcommand(CommandSender sender, String label, String[] args, ConfigHelper langConfig) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.PIXELCHAT_VERSION)) {
            sender.sendMessage(ChatColor.RED + langConfig.getString(LangConstants.NO_PERMISSION));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + langConfig.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " version");
            return;
        }

        PluginDescriptionFile description = plugin.getDescription();
        String headerFooter = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + StringUtils.repeat("-", 36);

        // Display plugin information
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + headerFooter);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.PIXELCHAT_VERSION) + " " + ChatColor.WHITE + description.getVersion());
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.PIXELCHAT_DEVELOPER) + " " + ChatColor.WHITE + description.getAuthors());
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.PIXELCHAT_PLUGIN_WEBSITE));
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.WHITE + description.getWebsite());
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.PIXELCHAT_REPORT_BUGS));
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.WHITE + "https://github.com/PixelMindMC/PixelChatGuardian/issues");
        sender.sendMessage(LangConstants.PLUGIN_PREFIX);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + headerFooter);

        // Send a message when an update is available
        if (plugin.updateAvailable) sender.sendMessage(LangConstants.UPDATE_AVAILABLE);
    }

    /**
     * Handles the "reload" subcommand to reload plugin configurations
     *
     * @param sender               The command sender
     * @param label                The label
     * @param args                 The arguments
     * @param configHelperLanguage The ConfigHelper for the languageConfig
     */
    private void handleReloadSubcommand(CommandSender sender, String label, String[] args, ConfigHelper configHelperLanguage) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.PIXELCHAT_RELOAD)) {
            sender.sendMessage(ChatColor.RED + configHelperLanguage.getString(LangConstants.NO_PERMISSION));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " reload");
            return;
        }

        // Reload the plugin configurations
        plugin.getConfigHelper().loadConfig();
        plugin.getConfigHelperPlayerStrikes().loadConfig();
        plugin.getConfigHelperLanguage().loadConfig();

        // Send a message after successfully reloading the configurations
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.GREEN + configHelperLanguage.getString(LangConstants.PIXELCHAT_RELOAD));
    }

    /**
     * Handles the "remove-strikes" subcommand to remove strikes from a specific player
     *
     * @param sender                    The command sender
     * @param label                     The label
     * @param args                      The arguments
     * @param configHelperPlayerStrikes The ConfigHelper for the languageConfig
     * @param configHelperLanguage      The ConfigHelper for the languageConfig
     */
    private void handleRemoveStrikesSubcommand(CommandSender sender, String label, String[] args, ConfigHelper configHelperPlayerStrikes, ConfigHelper configHelperLanguage) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.PIXELCHAT_REMOVE_PLAYER_STRIKES)) {
            sender.sendMessage(ChatColor.RED + configHelperLanguage.getString(LangConstants.NO_PERMISSION));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 2) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " remove-strikes <player>");
            return;
        }

        // Check if the player is in the blocklist
        Player onlinePlayer = Bukkit.getPlayer(args[1]);
        UUID playerUUID;
        if (onlinePlayer != null) {
            playerUUID = onlinePlayer.getUniqueId();
        } else playerUUID = getOfflinePlayerUUID(args[1]);


        if (playerUUID != null) if (configHelperPlayerStrikes.contains(playerUUID.toString()))
            configHelperPlayerStrikes.set(playerUUID.toString(), null);

        // Send a message after successfully remove player strikes from a specific player
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + configHelperLanguage.getString(LangConstants.PIXELCHAT_REMOVED_PLAYER_STRIKES) + " " + ChatColor.RED + ChatColor.BOLD + args[1] + ChatColor.RESET + ".");
    }

    /**
     * Method for retrieving the UUID of an offline player with the Mojang api
     *
     * @param playerName The specific player name
     * @return a player uuid of an offline player
     */
    private UUID getOfflinePlayerUUID(String playerName) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the UUID from the JSON response
                String jsonResponse = response.toString();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                // Extract the content string from the first choice's message
                String contentString = jsonObject.get("id").getAsString();
                return UUID.fromString(contentString.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"));
            }
        } catch (Exception e) {
            plugin.getLoggingHelper().error(e.getMessage());
        }
        return null; // Player not found or error occurred
    }
}