/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.constants;

/**
 * Constant class for holding constant values that are used for configuration
 */
public final class ConfigConstants {
    // Configuration file version
    public static final String CONFIG_VERSION = "version";

    private ConfigConstants() {
        // Prevent instantiation
    }

    /**
     * General configuration
     */
    public static final class General {
        public static final String LANGUAGE = "general.language";
        public static final String METRICS_ENABLED = "general.enable-metrics";
        public static final String CHECK_FOR_UPDATES = "general.check-for-updates";
        public static final String LOG_LEVEL = "general.log-level";

        private General() {
        }
    }

    /**
     * Plugin support configuration
     */
    public static final class PluginSupport {
        public static final String CARBONCHAT = "plugin-support.carbonchat";

        private PluginSupport() {
        }
    }

    /**
     * Module toggle configuration
     */
    public static final class Modules {
        public static final String CHATGUARD = "modules.chatguard";
        public static final String CHAT_CODES = "modules.chat-codes";
        public static final String EMOJIS = "modules.emojis";

        private Modules() {
        }
    }

    /**
     * AI and API related configuration
     */
    public static final class API {
        public static final String ENDPOINT = "api.endpoint";
        public static final String MODEL = "api.ai-model";
        public static final String KEY = "api.key";
        public static final String SYSTEM_PROMPT = "api.sys-prompt";

        private API() {
        }
    }

    /**
     * ChatGuard-specific configuration
     */
    public static final class ChatGuard {
        public static final String ENABLE_CUSTOM_PREFIX = "chatguard.enable-custom-prefix";
        public static final String CUSTOM_PREFIX = "chatguard.custom-prefix";
        public static final String MESSAGE_HANDLING = "chatguard.message-handling";
        public static final String NOTIFY_USER = "chatguard.notify-user";

        private ChatGuard() {
        }

        public static final class Rules {
            public static final String BLOCK_OFFENSIVE_LANGUAGE = "chatguard.rules.blockOffensiveLanguage";
            public static final String BLOCK_USERNAMES = "chatguard.rules.blockUsernames";
            public static final String BLOCK_PASSWORDS = "chatguard.rules.blockPasswords";
            public static final String BLOCK_HOME_ADDRESSES = "chatguard.rules.blockHomeAddresses";
            public static final String BLOCK_EMAIL_ADDRESSES = "chatguard.rules.blockEmailAddresses";
            public static final String BLOCK_WEBSITES = "chatguard.rules.blockWebsites";
            public static final String BLOCK_SEXUAL_CONTENT = "chatguard.rules.blockSexualContent";

            private Rules() {
            }
        }

        public static final class StrikeSystem {
            public static final String USE_BUILT_IN_STRIKE_SYSTEM = "chatguard.strike-system.use-built-in-strike-system";
            public static final String CLEAR_STRIKES_ON_SERVER_RESTART = "chatguard.strike-system.clear-strikes-on-server-restart";
            public static final String STRIKES_BEFORE_KICK = "chatguard.strike-system.strikes-before-kick";
            public static final String KICK_COMMAND = "chatguard.strike-system.kick-command";
            public static final String STRIKES_BEFORE_TEMP_BAN = "chatguard.strike-system.strikes-before-temp-ban";
            public static final String TEMP_BAN_COMMAND = "chatguard.strike-system.temp-ban-command";
            public static final String STRIKES_BEFORE_BAN = "chatguard.strike-system.strikes-before-ban";
            public static final String BAN_COMMAND = "chatguard.strike-system.ban-command";
            public static final String CUSTOM_STRIKE_COMMAND = "chatguard.strike-system.custom-strike-command";

            private StrikeSystem() {
            }
        }
    }

    /**
     * Emoji-related configuration
     */
    public static final class Emoji {
        public static final String LIST = "emoji-list";

        private Emoji() {
        }
    }

    /**
     * Chat code/formatting configuration
     */
    public static final class ChatCodes {
        public static final String LIST = "chat-codes-list";

        private ChatCodes() {
        }
    }
}