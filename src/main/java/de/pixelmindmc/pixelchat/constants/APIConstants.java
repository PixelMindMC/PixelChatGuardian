/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
 */

package de.pixelmindmc.pixelchat.constants;

/**
 * Constant class for holding constant values that are used for the API
 */
public final class APIConstants {

    private APIConstants() {
        // Prevent instantiation
    }

    /**
     * General keys
     */
    public static final class General {
        public static final String CONTENT = "content";
        public static final String REASON = "reason";

        private General() {
        }
    }

    /**
     * Detection result keys for content analysis
     */
    public static final class DetectionFlags {
        public static final String IS_OFFENSIVE_LANGUAGE = "isOffensiveLanguage";
        public static final String IS_USERNAME = "isUsername";
        public static final String IS_PASSWORD = "isPassword";
        public static final String IS_HOME_ADDRESS = "isHomeAddress";
        public static final String IS_EMAIL_ADDRESS = "isEmailAddress";
        public static final String IS_WEBSITE = "isWebsite";
        public static final String IS_SEXUAL_CONTENT = "isSexualContent";

        private DetectionFlags() {
        }
    }
}