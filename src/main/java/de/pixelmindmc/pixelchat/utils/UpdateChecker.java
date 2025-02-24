/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A utility class for checking updates for the plugin by querying the GitHub API
 */
public class UpdateChecker {
    private final PixelChat plugin;
    private final URL url;

    /**
     * Constructs a UpdateChecker object
     *
     * @param plugin The plugin instance
     * @param apiUrl The URL pointing to the GitHub API endpoint for checking updates
     */
    public UpdateChecker(@NotNull PixelChat plugin, @NotNull URL apiUrl) {
        this.plugin = plugin;
        this.url = apiUrl;
    }

    /**
     * Fetches the latest release version from the GitHub API
     *
     * @return The JSON response of the request
     * @throws IOException If any issue happens, an exception is thrown
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

            return JsonParser.parseString(response.toString()).getAsJsonObject();
        } else
            throw new IOException(plugin.getConfigHelperLanguage().getString(LangConstants.UNABLE_CHECK_FOR_UPDATES) + " " + responseCode);
    }

    /**
     * Checks for updates to the plugin
     *
     * @return A string denoting whether the plugin has an update available or not
     */
    public String checkForUpdates() throws IOException {
        String currentVersion = plugin.getDescription().getVersion();

        try {
            JsonObject latestRelease = getLatestReleaseFromGitHub();
            String latestVersion = latestRelease.get("tag_name").getAsString();
            boolean isPreRelease = latestRelease.get("prerelease").getAsBoolean();

            if (!isPreRelease && isNewerVersion(currentVersion, latestVersion)) {
                return plugin.getConfigHelperLanguage().getString(LangConstants.UPDATE_AVAILABLE) +
                        " https://modrinth.com/plugin/pixelchatguardian/";
            } else return plugin.getConfigHelperLanguage().getString(LangConstants.NO_UPDATE_AVAILABLE);
        } catch (Exception e) {
            throw new IOException(plugin.getConfigHelperLanguage().getString(LangConstants.UNABLE_CHECK_FOR_UPDATES) + " " + e);
        }
    }

    /**
     * Compares two version strings
     *
     * @param currentVersion The current version string
     * @param latestVersion  The latest version string
     * @return A boolean that indicates whether the last version is newer than the current version
     */
    private boolean isNewerVersion(@NotNull String currentVersion, @NotNull String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        for (int i = 0; i < Math.min(currentParts.length, latestParts.length); i++) {
            int currentPart = Integer.parseInt(currentParts[i]);
            int latestPart = Integer.parseInt(latestParts[i]);

            if (currentPart < latestPart) {
                return true; // Current version is older
            } else if (currentPart > latestPart) {
                return false; // Current version is newer
            }
        }

        // If versions are the same length and no difference was found
        return currentParts.length < latestParts.length; // Newer if latest has more sub-parts
    }
}