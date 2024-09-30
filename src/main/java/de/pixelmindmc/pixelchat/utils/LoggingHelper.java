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

/**
 * A utility class for handling logging
 */
public class LoggingHelper {
    final Logger logger;

    /**
     * Constructs a LoggingHelper object
     *
     * @param plugin The plugin instance
     * @throws NullPointerException if the log level in the configuration is null
     */
    public LoggingHelper(PixelChat plugin) {
        this.logger = plugin.getLogger();
        setLogLevel(Objects.requireNonNull(plugin.getConfig().getString(ConfigConstants.LOG_LEVEL)));
    }

    /**
     * Logs a message at the INFO level, if the current log level allows INFO logging
     *
     * @param message The message to log
     */
    public void info(String message) {
        if (isLogLevel(Level.INFO)) {
            log(Level.INFO, message);
        }
    }

    /**
     * Logs a message at the WARNING level, if the current log level allows WARNING logging
     *
     * @param message The message to log
     */
    public void warning(String message) {
        if (isLogLevel(Level.WARNING)) {
            log(Level.WARNING, message);
        }
    }

    /**
     * Logs a message at the ERROR (SEVERE) level, if the current log level allows SEVERE logging
     *
     * @param message The message to log
     */
    public void error(String message) {
        if (isLogLevel(Level.SEVERE)) {
            log(Level.SEVERE, message);
        }
    }

    /**
     * Logs a message at the DEBUG (CONFIG) level, if the current log level allows DEBUG logging
     * Debug messages are prefixed with "[DEBUG]" to distinguish them
     *
     * @param message The message to log
     */
    public void debug(String message) {
        if (isLogLevel(Level.CONFIG)) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    /**
     * Sets the log level for the logger based on a string value
     * Supported values: DEBUG, ERROR, WARNING, and INFO
     *
     * @param level The log level as a string
     */
    public void setLogLevel(String level) {
        Level logLevel = switch (level.toUpperCase()) {
            case "DEBUG" -> Level.CONFIG;
            case "ERROR" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            default -> Level.INFO;
        };
        logger.setLevel(logLevel);
    }

    /**
     * Logs a message with the specified log level
     *
     * @param level   The log level at which to log the message
     * @param message The message to log
     */
    private void log(Level level, String message) {
        logger.log(level, message);
    }

    /**
     * Checks whether the given log level is enabled in the current logger configuration
     *
     * @param level The log level to check
     * @return True if the given log level is enabled, false otherwise
     */
    private boolean isLogLevel(Level level) {
        return logger.isLoggable(level);
    }
}