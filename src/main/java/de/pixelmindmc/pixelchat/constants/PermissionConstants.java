/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2025 PixelMindMC
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
         * Allows bypassing all chat moderation filters
         */
        public static final String BYPASS_CHAT_MODERATION = "pixelchat.bypass-chat-moderation";

        private Moderation() {
        }
    }
}