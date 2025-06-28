/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.constants;

import org.bukkit.ChatColor;

/**
 * Constant class for holding constant values that are used for the language configuration
 */
public final class LangConstants {
    /**
     * The plugin prefix shown before messages
     */
    public static final String PLUGIN_PREFIX =
            ChatColor.DARK_GRAY + "[" + ChatColor.RED + ChatColor.BOLD + "Pixel" + ChatColor.BLUE + "Chat" + ChatColor.RESET +
                    ChatColor.DARK_GRAY + "]" + ChatColor.RESET + " ";

    /**
     * Configuration file version
     */
    public static final String LANGUAGE_CONFIG_VERSION = "version";

    private LangConstants() {
        // Prevent instantiation
    }

    /**
     * Global system messages and errors
     */
    public static final class Global {
        public static final String METRICS_ENABLED = "global.metrics-enabled";
        public static final String CHECKING_FOR_UPDATES = "global.checking-for-updates";
        public static final String UPDATE_AVAILABLE = "global.update-available";
        public static final String NO_UPDATE_AVAILABLE = "global.no-update-available";
        public static final String UNABLE_TO_CHECK_FOR_UPDATES = "global.unable-to-check-for-updates";
        public static final String CONFIG_OUTDATED = "global.config-outdated";
        public static final String LANGUAGE_CONFIG_OUTDATED = "global.language-config-outdated";
        public static final String FAILED_TO_SAVE_CONFIG = "global.failed-to-save-config";
        public static final String FIRST_TIME_MESSAGE = "global.first-time-message";
        public static final String NO_API_KEY_SET = "global.no-api-key-set";
        public static final String NO_PERMISSION = "global.no-permission";
        public static final String INVALID_SYNTAX = "global.invalid-syntax";
        public static final String INVALID_SYNTAX_USAGE = "global.invalid-syntax-usage";

        private Global() {
        }
    }

    /**
     * Messages related to the /pixelchat command
     */
    public static final class PixelChatCommand {
        public static final String VERSION = "pixelchat.version";
        public static final String DEVELOPER = "pixelchat.developer";
        public static final String PLUGIN_WEBSITE = "pixelchat.plugin-website";
        public static final String REPORT_BUGS = "pixelchat.report-bugs";
        public static final String RELOAD = "pixelchat.reload";
        public static final String REMOVED_PLAYER_STRIKES = "pixelchat.chatguard.removed-player-strikes";
        public static final String STRUCK_PLAYER = "pixelchat.chatguard.struck-player";

        private PixelChatCommand() {
        }
    }

    /**
     * Messages related to the ChatGuard module
     */
    public static final class ChatGuard {
        public static final String CLEARED_STRIKES_ON_SERVER_RESTART = "chatguard.cleared-strikes-on-server-restart";
        public static final String MESSAGE_BLOCKED = "chatguard.player.message-blocked";
        public static final String MESSAGE_CENSORED = "chatguard.player.message-censored";
        public static final String PLAYER_KICK = "chatguard.player.kick";
        public static final String PLAYER_TEMP_BAN = "chatguard.player.ban-temporary";
        public static final String PLAYER_PERM_BAN = "chatguard.player.ban-permanent";

        private ChatGuard() {
        }
    }
}