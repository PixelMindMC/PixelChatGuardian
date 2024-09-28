/*
 * This file is part of PixelChatGuardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingHelper {
    final Logger logger;

    // Constructor that takes the plugin instance for getting the logger
    public LoggingHelper(PixelChat plugin) {
        this.logger = plugin.getLogger();
        setLogLevel(Objects.requireNonNull(plugin.getConfig().getString(ConfigConstants.LOG_LEVEL)));
    }

    // Info level logging
    public void info(String message) {
        if (isLogLevel(Level.INFO)) {
            log(Level.INFO, message);
        }
    }

    // Warning level logging
    public void warning(String message) {
        if (isLogLevel(Level.WARNING)) {
            log(Level.WARNING, message);
        }
    }

    // Error level logging
    public void error(String message) {
        if (isLogLevel(Level.SEVERE)) {
            log(Level.SEVERE, message);
        }
    }

    // Debug level logging
    public void debug(String message) {
        if (isLogLevel(Level.CONFIG)) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    // Sets the log level
    public void setLogLevel(String level) {
        Level logLevel = switch (level.toUpperCase()) {
            case "DEBUG" -> Level.CONFIG;
            case "ERROR" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            default -> Level.INFO;
        };
        logger.setLevel(logLevel);
    }

    // Generic method for logging with a specified level
    private void log(Level level, String message) {
        logger.log(level, message);
    }

    // Check if the given log level is enabled
    private boolean isLogLevel(Level level) {
        return logger.isLoggable(level);
    }
}