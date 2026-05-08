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

package de.pixelmindmc.pixelchat.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the AI-Classification of a message
 *
 * @param isOffensiveLanguage Whether the message contains offensive language, severe insults, hate speech, slurs,
 *                            real-world crime references, or other forms of harmful language
 * @param isUsername          Whether the message contains a possible username
 * @param isPassword          Whether the message contains a possible password
 * @param isHomeAddress       Whether the message contains a possible home address
 * @param isEmailAddress      Whether the message contains an email address
 * @param isWebsite           Whether the message contains a website
 * @param isSexualContent     Whether the message contains sexual content
 * @param reason              The reason why the message should be blocked
 */
public record MessageClassification(boolean isOffensiveLanguage, boolean isUsername, boolean isPassword,
                                    boolean isHomeAddress, boolean isEmailAddress, boolean isWebsite,
                                    boolean isSexualContent, @NotNull String reason) {
}