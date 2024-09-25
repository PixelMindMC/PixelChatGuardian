/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.commands;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.constants.PermissionConstants;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class PixelChatCommand implements CommandExecutor {
    private final PixelChat plugin;

    public PixelChatCommand(PixelChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigHelper configHelperLanguage = plugin.getConfigHelperLanguage();

        // Display usage information if no arguments are provided
        if (args.length == 0) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + label + " <version|reload>");
            return true;
        }

        // Trollplus subcommand selection
        switch (args[0].toLowerCase()) {
            case "version" -> handleVersionSubcommand(sender, label, args, configHelperLanguage);
            case "reload" -> handleReloadSubcommand(sender, label, args, configHelperLanguage);
            default ->
                    sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + configHelperLanguage.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " <version|reload>");
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
        sender.sendMessage(plugin.updateCheckerLog);
    }

    /**
     * Handles the "reload" subcommand to reload plugin configurations
     *
     * @param sender     The command sender
     * @param label      The label
     * @param args       The arguments
     * @param langConfig The ConfigHelper for the languageConfig
     */
    private void handleReloadSubcommand(CommandSender sender, String label, String[] args, ConfigHelper langConfig) {
        // Check if the player has the required permission
        if (!sender.hasPermission(PermissionConstants.PIXELCHAT_RELOAD)) {
            sender.sendMessage(ChatColor.RED + langConfig.getString(LangConstants.NO_PERMISSION));
            return;
        }

        // Check if the command syntax is correct
        if (args.length != 1) {
            sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.RED + langConfig.getString(LangConstants.INVALID_SYNTAX) + " " + ChatColor.RESET + langConfig.getString(LangConstants.INVALID_SYNTAX_USAGE) + " " + label + " reload");
            return;
        }

        // Reload the plugin configurations
        plugin.getConfigHelper().loadConfig();
        plugin.getConfigHelperLanguage().loadConfig();

        // Send a message after successfully reloading the configurations
        sender.sendMessage(LangConstants.PLUGIN_PREFIX + ChatColor.GREEN + langConfig.getString(LangConstants.PIXELCHAT_RELOAD));
    }
}