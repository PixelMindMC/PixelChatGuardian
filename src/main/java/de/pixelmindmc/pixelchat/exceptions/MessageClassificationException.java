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