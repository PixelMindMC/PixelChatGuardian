/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.model;

/**
 * Represents the AI-Classification of a message
 *
 * @param block  Whether the message should be blocked or not
 * @param reason The reason why the message should be blocked
 */
public record MessageClassification(boolean block, String reason) {
}