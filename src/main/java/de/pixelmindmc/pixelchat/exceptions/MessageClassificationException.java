/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.exceptions;

public class MessageClassificationException extends Exception {
    public MessageClassificationException(String message) {
        super(message);
    }

    public MessageClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}