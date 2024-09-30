/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.exceptions;

/**
 * This class provides constructors for passing custom error messages and wrapping underlying causes
 */
public class MessageClassificationException extends Exception {
    public MessageClassificationException(String message) {
        super(message);
    }

    public MessageClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}