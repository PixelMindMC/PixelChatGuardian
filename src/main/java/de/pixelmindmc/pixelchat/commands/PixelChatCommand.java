/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2026 PixelMindMC
 */

package de.pixelmindmc.pixelchat.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import de.pixelmindmc.pixelchat.utils.LoggingHelper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.UUID;

/**
 * CommandExecutor for handling the "pixelchat" command, the main command for the plugin
 */
public class PixelChatCommand implements CommandExecutor {
    private final @NotNull PixelChat plugin;
    private final @NotNull LoggingHelper loggingHelper;
    private final @NotNull ConfigHelper configHelper;
    private final @NotNull ConfigHelper configHelperPlayerStrikes;

    /**
     * Constructs a PixelChatCommand object
     *
     * @param plugin The plugin instance
     */
    public PixelChatCommand(@NotNull PixelChat plugin) {
        this.plugin = plugin;
        this.loggingHelper = plugin.getLoggingHelper();
        this.configHelper = plugin.getConfigHelper();
        this.configHelperPlayerStrikes = plugin.getConfigHelperPlayerStrikes();
    }

    /**
     * Handles the execution of the "pixelchat" command
     *
     * @param sender  The source of the command (player or console)
     * @param command The command being executed
     * @param label   The alias used to invoke the command
     * @param args    The arguments provided with the command
     * @return true to indicate the command was processed
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Display usage information if no arguments are provided
        if (args.length == 0) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX) + " " + ChatColor.RESET + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX_USAGE) + label + " <version|reload>");

            return true;
        }

        // Pixelchat subcommand selection
        switch (args[0].toLowerCase()) {
            case "version" -> handleVersionSubcommand(sender, label, args);
            case "reload" -> handleReloadSubcommand(sender, label, args);
            default ->
                    sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX) + " " + ChatColor.RESET + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX_USAGE) + " " + label + " <version|reload>");
        }

        return true;
    }

    /**
     * Handles the "version" subcommand to display plugin information
     *
     * @param sender The command sender
     * @param label  The label
     * @param args   The arguments
     */
    private void handleVersionSubcommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.Commands.VERSION) & sender.hasPermission(PermissionConstants.Commands.RELOAD) & !sender.hasPermission(PermissionConstants.Commands.VERSION) & !sender.hasPermission(PermissionConstants.Commands.VERSION)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.NO_PERMISSION));

            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX) + " " + ChatColor.RESET + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX_USAGE) + " " + label + " version");

            return;
        }

        PluginDescriptionFile description = plugin.getDescription();
        String headerFooter = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + StringUtils.repeat("-", 36);

        String authors = description.getAuthors().toString().replace("[", "").replace("]", "");

        // Display plugin information
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + headerFooter);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.PixelChatCommand.VERSION) + " " + ChatColor.WHITE + description.getVersion());
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.PixelChatCommand.DEVELOPER) + " " + ChatColor.WHITE + authors);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.PixelChatCommand.PLUGIN_WEBSITE));
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.WHITE + description.getWebsite());
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.PixelChatCommand.REPORT_BUGS));
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.WHITE + "https://github.com/PixelMindMC/PixelChatGuardian/issues");
        sender.sendMessage(LangConstants.PLUGIN_PREFIX);
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + headerFooter);

        // Send a message when an update is available
        if (!plugin.updateChecker().equals(LangConstants.Global.NO_UPDATE_AVAILABLE)) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + plugin.updateChecker());
        }
    }

    /**
     * Handles the "reload" subcommand to reload plugin configurations
     *
     * @param sender The command sender
     * @param label  The label
     * @param args   The arguments
     */
    private void handleReloadSubcommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.Commands.RELOAD)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.NO_PERMISSION));

            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX) + " " + ChatColor.RESET + plugin.getConfigHelperLanguage().getString(LangConstants.Global.INVALID_SYNTAX_USAGE) + " " + label + " " + "reload");

            return;
        }

        // Reload all plugin configurations and re-initialize dependent components
        plugin.reloadPlugin();

        // Debug logger message
        loggingHelper.debug("Configuration files successfully reloaded");

        // Send a message after successfully reloading the configurations
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.GREEN + plugin.getConfigHelperLanguage().getString(LangConstants.PixelChatCommand.RELOAD));
    }

    /**
     * Method for retrieving the UUID of an offline player with the Mojang api
     *
     * @param playerName The specific player name
     * @return a player uuid of an offline player
     */
    public @Nullable UUID getOfflinePlayerUUID(@NotNull String playerName) {
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

                UUID uuid = UUID.fromString(contentString.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3" + "-$4-$5"));

                // Debug logger message
                loggingHelper.debug("The uuid of the player " + playerName + " is: " + uuid);

                return uuid;
            }
        } catch (Exception e) {
            loggingHelper.error(e.getMessage());
        }

        return null; // Player not found or error occurred
    }
}