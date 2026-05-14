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

package de.pixelmindmc.pixelchat.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A utility class for checking updates for the plugin by querying the GitHub API
 */
public class UpdateChecker {
    private final @NotNull PixelChat plugin;
    private final @NotNull ConfigHelper configHelperLanguage;
    private final @NotNull URL url;

    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/pixelchatguardian/";

    /**
     * Constructs an UpdateChecker object
     *
     * @param plugin The plugin instance
     * @param apiUrl The URL pointing to the GitHub API endpoint for checking updates
     */
    public UpdateChecker(@NotNull PixelChat plugin, @NotNull URL apiUrl) {
        this.plugin = plugin;
        this.configHelperLanguage = plugin.getConfigHelperLanguage();

        this.url = apiUrl;
    }

    /**
     * Fetches the latest release version from the GitHub API
     *
     * @return The JSON response of the request
     * @throws IOException If an I/O error occurs while contacting the GitHub API
     */
    private JsonObject getLatestReleaseFromGitHub() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            connection.disconnect();
            return JsonParser.parseString(response.toString()).getAsJsonObject();
        } else {
            throw new IOException(configHelperLanguage.getString(LangConstants.Global.UNABLE_TO_CHECK_FOR_UPDATES) + " " + responseCode);
        }
    }

    /**
     * Checks for plugin updates by querying the GitHub API for the latest release
     *
     * @return A string denoting whether the plugin has an update available or not
     */
    public @NotNull String checkForUpdates() throws IOException {
        String currentVersion = plugin.getDescription().getVersion();

        try {
            JsonObject latestRelease = getLatestReleaseFromGitHub();
            String latestVersion = latestRelease.get("tag_name").getAsString();
            boolean isPreRelease = latestRelease.get("prerelease").getAsBoolean();

            if (!isPreRelease && isNewerVersion(currentVersion, latestVersion)) {
                return configHelperLanguage.getString(LangConstants.Global.UPDATE_AVAILABLE) + " " + DOWNLOAD_URL;
            } else {
                return configHelperLanguage.getString(LangConstants.Global.NO_UPDATE_AVAILABLE);
            }
        } catch (IOException e) {
            throw new IOException(configHelperLanguage.getString(LangConstants.Global.UNABLE_TO_CHECK_FOR_UPDATES), e);
        }
    }

    /**
     * Compares two version strings
     *
     * @param currentVersion The current version string
     * @param latestVersion  The latest version string
     * @return True if the latest version is newer than the current version, false otherwise
     */
    private boolean isNewerVersion(@NotNull String currentVersion, @NotNull String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        for (int i = 0; i < java.lang.Math.min(currentParts.length, latestParts.length); i++) {
            int currentPart = Integer.parseInt(currentParts[i]);
            int latestPart = Integer.parseInt(latestParts[i]);

            if (currentPart < latestPart) {
                return true; // Current version is older
            } else if (currentPart > latestPart) {
                return false; // Current version is newer
            }
        }

        // All corresponding numeric parts are equal; consider longer version newer.
        return currentParts.length < latestParts.length; // Newer if latest has more sub-parts
    }
}