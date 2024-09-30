/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.constants;

import org.bukkit.ChatColor;

/**
 * Constant class for holding constant values that are used for the language configuration
 */
public class LangConstants {
    public static final String LANGUAGE_CONFIG_VERSION = "version";
    public static final String PLUGIN_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.RED + ChatColor.BOLD + "Pixel" + ChatColor.BLUE + "Chat" + ChatColor.RESET + ChatColor.DARK_GRAY + "]" + ChatColor.RESET + " ";
    // Global
    public static final String METRICS_ENABLED = "metrics-enabled";
    public static final String CHECKING_FOR_UPDATES = "checking-for-updates";
    public static final String UPDATE_AVAILABLE = "update-available";
    public static final String NO_UPDATE_AVAILABLE = "no-update-available";
    public static final String UNABLE_CHECK_FOR_UPDATES = "unable-check-for-updates";
    public static final String CONFIG_OUTDATED = "config-outdated";
    public static final String LANGUAGE_CONFIG_OUTDATED = "language-config-outdated";
    public static final String FAILED_TO_SAVE_CONFIG = "failed-to-save-config";
    public static final String NO_API_KEY_SET = "no-api-key-set";
    public static final String NO_PERMISSION = "no-permission";
    public static final String INVALID_SYNTAX = "invalid-syntax";
    public static final String INVALID_SYNTAX_USAGE = "invalid-syntax-usage";
    // PixelChat command
    public static final String PIXELCHAT_VERSION = "pixelchat.version";
    public static final String PIXELCHAT_DEVELOPER = "pixelchat.developer";
    public static final String PIXELCHAT_PLUGIN_WEBSITE = "pixelchat.plugin-website";
    public static final String PIXELCHAT_REPORT_BUGS = "pixelchat.report-bugs";
    public static final String PIXELCHAT_RELOAD = "pixelchat.reload";
    // Pixelchat Guardian
    public static final String MESSAGE_BLOCKED = "message-blocked";
    public static final String MESSAGE_CENSORED = "message-censored";
    public static final String PLAYER_KICK = "player.kick";
    public static final String PLAYER_BAN_TEMPORARY = "player.ban-temporary";
    public static final String PLAYER_BAN_PERMANENT = "player.ban-permanent";

    private LangConstants() {
    }
}