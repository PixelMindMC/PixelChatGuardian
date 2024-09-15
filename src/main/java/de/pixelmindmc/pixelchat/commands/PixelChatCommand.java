/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 Gaming12846
 */

package de.pixelmindmc.pixelchat.commands;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import static de.pixelmindmc.pixelchat.PixelChat.PLUGIN_PREFIX;

public class PixelChatCommand implements CommandExecutor {
    private final PixelChat plugin;

    // Constructor for the PixelChatCommand
    public PixelChatCommand(PixelChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigHelper configHelperLanguage = plugin.getConfigHelperLanguage();

        // Display usage information if no arguments are provided
        if (args.length == 0) {
            sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString("invalid-syntax") + " " + ChatColor.RESET + configHelperLanguage.getString("invalid-syntax-usage") + label + " <version|reload>");
            return true;
        }

        // Trollplus subcommand selection
        switch (args[0].toLowerCase()) {
            case "version" -> handleVersionSubcommand(sender, label, args, configHelperLanguage);
            case "reload" -> handleReloadSubcommand(sender, label, args, configHelperLanguage);
            default ->
                    sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString("invalid-syntax") + " " + ChatColor.RESET + configHelperLanguage.getString("invalid-syntax-usage") + " " + label + " <version|reload>");
        }

        return true;
    }

    // Handles the "version" subcommand to display plugin information
    private void handleVersionSubcommand(CommandSender sender, String label, String[] args, ConfigHelper langConfig) {
        // Check if the player has the required permission
        if (!sender.hasPermission("pixelchat.version")) {
            sender.sendMessage(ChatColor.RED + langConfig.getString("no-permission"));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("invalid-syntax") + " " + ChatColor.RESET + langConfig.getString("invalid-syntax-usage") + " " + label + " version");
            return;
        }

        PluginDescriptionFile description = plugin.getDescription();
        String headerFooter = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + StringUtils.repeat("-", 36);

        // Display plugin information
        sender.sendMessage(PLUGIN_PREFIX + headerFooter);
        sender.sendMessage(PLUGIN_PREFIX);
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("pixelchat.version") + " " + ChatColor.WHITE + description.getVersion());
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("pixelchat.developer") + " " + ChatColor.WHITE + description.getAuthors().get(0));
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("pixelchat.plugin-website"));
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.WHITE + description.getWebsite());
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("pixelchat.report-bugs"));
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.WHITE + "https://github.com/Gaming12846/PixelChat_Guardian/issues");
        sender.sendMessage(PLUGIN_PREFIX);
        sender.sendMessage(PLUGIN_PREFIX + headerFooter);

        // Send a message when an update is available
        sender.sendMessage(plugin.updateCheckerLog);
    }

    // Handles the "reload" subcommand to reload plugin configurations
    private void handleReloadSubcommand(CommandSender sender, String label, String[] args, ConfigHelper langConfig) {
        // Check if the player has the required permission
        if (!sender.hasPermission("pixelchat.reload")) {
            sender.sendMessage(ChatColor.RED + langConfig.getString("no-permission"));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(PLUGIN_PREFIX + ChatColor.RED + langConfig.getString("invalid-syntax") + " " + ChatColor.RESET + langConfig.getString("invalid-syntax-usage") + " " + label + " reload");
            return;
        }

        // Reload the plugin configurations
        plugin.getConfigHelper().loadConfig();
        plugin.getConfigHelperLanguage().loadConfig();

        // Send a message after successfully reloading the configurations
        sender.sendMessage(PLUGIN_PREFIX + ChatColor.GREEN + langConfig.getString("pixelchat.reload"));
    }
}