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
 * Constant class for holding constant values that are used for the permissions
 */
public final class PermissionConstants {
    /**
     * Grants all permissions for PixelChat
     */
    public static final String FULL_PERMISSIONS = "pixelchat.*";

    private PermissionConstants() {
        // Prevent instantiation
    }

    /**
     * Permissions for administrative or command-related actions
     */
    public static final class Commands {
        /**
         * Allows using /pixelchat version
         */
        public static final String VERSION = "pixelchat.version";

        /**
         * Allows using /pixelchat reload
         */
        public static final String RELOAD = "pixelchat.reload";

        private Commands() {
        }
    }

    /**
     * Permissions related to modules
     */
    public static final class Modules {
        /**
         * Allows using emojis
         */
        public static final String EMOJIS = "pixelchat.emojis";

        /**
         * Allows using chat codes
         */
        public static final String CHAT_CODES = "pixelchat.chat-codes";

        private Modules() {
        }
    }

    /**
     * Permissions related to chat moderation
     */
    public static final class Moderation {
        /**
         * Allows issuing a strike to a player
         */
        public static final String STRIKE_PLAYER = "pixelchat.strike-player";

        /**
         * Allows removing strikes from a player
         */
        public static final String REMOVE_PLAYER_STRIKES = "pixelchat.remove-player-strikes";

        /**
         * Allows to receive in-game notifications about player strikes
         */
        public static final String STRIKE_NOTIFY = "pixelchat.strike-notify";

        /**
         * Allows bypassing all chat moderation filters
         */
        public static final String BYPASS_CHAT_MODERATION = "pixelchat.bypass-chat-moderation";

        private Moderation() {
        }
    }
}