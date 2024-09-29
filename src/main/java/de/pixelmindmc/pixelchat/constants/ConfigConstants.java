/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.constants;

public class ConfigConstants {
    public static final String CONFIG_VERSION = "version";
    // Global settings
    public static final String LANGUAGE = "language";
    public static final String METRICS_ENABLED = "metrics-enabled";
    public static final String CHECK_FOR_UPDATES = "check-for-updates";
    public static final String LOG_LEVEL = "log-level";
    // Module settings
    public static final String MODULE_CHATGUARD = "modules.chatguard";
    public static final String MODULE_EMOJIS = "modules.emojis";
    // AI and API settings
    public static final String API_ENDPOINT = "api-endpoint";
    public static final String AI_MODEL = "ai-model";
    public static final String API_KEY = "api-key";
    public static final String SYSTEM_PROMPT = "sys-prompt";
    // Chatguard settings
    public static final String CHATGUARD_MESSAGE_HANDLING = "message-handling";
    public static final String CHATGUARD_USE_BUILT_IN_STRIKE_SYSTEM = "use-built-in-strike-system";
    public static final String CHATGUARD_CUSTOM_STRIKE_COMMAND = "custom-strike-command";
    public static final String CHATGUARD_STRIKES_BEFORE_KICK = "strikes-before-kick";
    public static final String CHATGUARD_KICK_COMMAND = "kick-command";
    public static final String CHATGUARD_STRIKES_BEFORE_TEMP_BAN = "strikes-before-temp-ban";
    public static final String CHATGUARD_TEMP_BAN_COMMAND = "temp-ban-command";
    public static final String CHATGUARD_STRIKES_BEFORE_BAN = "strikes-before-ban";
    public static final String CHATGUARD_BAN_COMMAND = "ban-command";
    // Emoji settings
    public static final String EMOJI_LIST = "emoji-list";

    private ConfigConstants() {
    }
}