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
        private ChatGuard() {
        }

        public static final String MESSAGE_HANDLING = "chatguard.message-handling";
        public static final String NOTIFY_USER = "chatguard.notify-user";

        public static final class CustomPrefix {
            public static final String ENABLED = "chatguard.custom-prefix.enabled";
            public static final String FORMAT = "chatguard.custom-prefix.format";

            private CustomPrefix() {
            }
        }

        public static final class Rules {
            public static final String BLOCK_OFFENSIVE_LANGUAGE = "chatguard.rules.block-offensive-language";
            public static final String BLOCK_USERNAMES = "chatguard.rules.block-usernames";
            public static final String BLOCK_PASSWORDS = "chatguard.rules.block-passwords";
            public static final String BLOCK_HOME_ADDRESSES = "chatguard.rules.block-home-addresses";
            public static final String BLOCK_EMAIL_ADDRESSES = "chatguard.rules.block-email-addresses";
            public static final String BLOCK_WEBSITES = "chatguard.rules.block-websites";
            public static final String BLOCK_SEXUAL_CONTENT = "chatguard.rules.block-sexual-content";

            private Rules() {
            }
        }

        public static final class StrikeSystem {
            public static final String ENABLED = "chatguard.strike-system.enabled";
            public static final String CLEAR_ON_RESTART = "chatguard.strike-system.clear-on-restart";

            public static final class Thresholds {
                public static final String KICK = "chatguard.strike-system.thresholds.kick";
                public static final String TEMP_BAN = "chatguard.strike-system.thresholds.temp-ban";
                public static final String BAN = "chatguard.strike-system.thresholds.ban";

                private Thresholds() {
                }
            }

            public static final class Commands {
                public static final String KICK = "chatguard.strike-system.commands.kick";
                public static final String TEMP_BAN = "chatguard.strike-system.commands.temp-ban";
                public static final String BAN = "chatguard.strike-system.commands.ban";
                public static final String CUSTOM_STRIKE = "chatguard.strike-system.custom-strike-command";

                private Commands() {
                }
            }

            private StrikeSystem() {
            }
        }
    }

    /**
     * Emoji-related configuration
     */
    public static final class Emoji {
        public static final String LIST = "emojis";

        private Emoji() {
        }
    }

    /**
     * Chat code/formatting configuration
     */
    public static final class ChatCodes {
        public static final String LIST = "chat-codes";

        private ChatCodes() {
        }
    }
}