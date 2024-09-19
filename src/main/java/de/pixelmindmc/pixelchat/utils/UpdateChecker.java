/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.model.LangConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// A utility class for checking updates for the plugin by querying the GitHub API
public class UpdateChecker {
    private final PixelChat plugin;
    private final URL url;

    // Constructor for the UpdateChecker
    public UpdateChecker(PixelChat plugin, URL apiUrl) {
        this.plugin = plugin;
        url = apiUrl;
    }

    // Fetches the latest release version from the GitHub API
    public JsonObject getLatestReleaseFromGitHub() throws Exception {
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
            plugin.getLogger().warning(plugin.getConfigHelperLanguage().getString(LangConstants.UNABLE_CHECK_FOR_UPDATES) + " " + responseCode);
        return null;
    }

    // Checks for updates to the plugin
    public String checkForUpdates() {
        String currentVersion = plugin.getDescription().getVersion();

        try {
            JsonObject latestRelease = getLatestReleaseFromGitHub();
            String latestVersion = latestRelease.get("tag_name").getAsString();
            boolean isPreRelease = latestRelease.get("prerelease").getAsBoolean();

            if (!isPreRelease && !currentVersion.equals(latestVersion)) {
                return plugin.getConfigHelperLanguage().getString(LangConstants.UPDATE_AVAILABLE) + " https://modrinth.com/project/pixelchatguardian";
            } else return plugin.getConfigHelperLanguage().getString(LangConstants.NO_UPDATE_AVAILABLE);
        } catch (Exception e) {
            plugin.getLogger().warning(plugin.getConfigHelperLanguage().getString(LangConstants.UNABLE_CHECK_FOR_UPDATES) + " " + e);
        }
        return null;
    }
}