/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.model;

public class ConfigConstants {
    public static final String CONFIG_VERSION = "version";
    // Global settings
    public static final String LANGUAGE = "language";
    public static final String METRICS_ENABLED = "metrics-enabled";
    public static final String CHECK_FOR_UPDATES = "check-for-updates";
    public static final String LOG_LEVEL = "log-level";
    // Module settings
    public static final String CHATGUARD = "modules.chatguard";
    public static final String EMOJIS = "modules.emojis";
    // AI and API settings
    public static final String API_ENDPOINT = "api-endpoint";
    public static final String AI_MODEL = "ai-model";
    public static final String API_KEY = "api-key";
    public static final String SYSTEM_PROMPT = "sys-prompt";

    // Prevent instantiation
    private ConfigConstants() {
    }
}