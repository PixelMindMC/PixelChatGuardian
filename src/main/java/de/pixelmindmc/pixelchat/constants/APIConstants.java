/*
 * Copyright (C) 2024 - 2026 PixelMindMC
 *
 * This file is part of PixelChat Guardian.
 *
 * PixelChat Guardian is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * PixelChat Guardian is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PixelChat Guardian.
 * If not, see <https://www.gnu.org/licenses/>.
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
        public static final String IS_EMAIL_ADDRESS = "isEmail";
        public static final String IS_WEBSITE = "isWebsite";
        public static final String IS_SEXUAL_CONTENT = "isSexualContent";

        private DetectionFlags() {
        }
    }
}