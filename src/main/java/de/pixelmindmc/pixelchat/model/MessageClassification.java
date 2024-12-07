/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the AI-Classification of a message
 *
 * @param isOffensiveLanguage Whether the message contains offensive language, severe insults, hate speech, slurs, real-world crime references, or other forms of harmful language
 * @param isUsername          Whether the message contains a possible username
 * @param isPassword          Whether the message contains a possible password
 * @param isHomeAddress       Whether the message contains a possible home address
 * @param isEmailAddress      Whether the message contains a possible email address
 * @param isWebsite           Whether the message contains a possible website
 * @param reason              The reason why the message should be blocked
 */
public record MessageClassification(@NotNull boolean isOffensiveLanguage, @NotNull boolean isUsername, @NotNull boolean isPassword,
                                    @NotNull boolean isHomeAddress, @NotNull boolean isEmailAddress, @NotNull boolean isWebsite,
                                    @NotNull String reason) {
}