/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.APIConstants;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.exceptions.MessageClassificationException;
import de.pixelmindmc.pixelchat.model.MessageClassification;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A collection of methods to aid with the AI requests to the AI API
 */
public class APIHelper {
    private final @NotNull PixelChat plugin;

    private final String aiModel;
    private final String apiUrl;
    private final String apiKey;
    private final String sysPrompt;

    /**
     * Constructs a APIHelper object
     *
     * @param plugin The plugin instance
     */
    public APIHelper(@NotNull PixelChat plugin) {
        this.plugin = plugin;
        this.aiModel = plugin.getConfig().getString(ConfigConstants.AI_MODEL);
        this.apiKey = plugin.getConfig().getString(ConfigConstants.API_KEY);
        this.sysPrompt = plugin.getConfig().getString(ConfigConstants.SYSTEM_PROMPT);
        this.apiUrl = plugin.getConfig().getString(ConfigConstants.API_ENDPOINT);
    }

    /**
     * Classifies player messages using AI
     *
     * @param message The message to classify
     * @return A {@link MessageClassification} object filled with the results of the AI-classification
     * @throws MessageClassificationException If the classification failed in any way
     */
    public @NotNull MessageClassification classifyMessage(@NotNull String message) throws MessageClassificationException {
        try {
            HttpURLConnection connection = createConnection();
            sendRequest(connection, message);
            int responseCode = connection.getResponseCode(); // HTTP code of the response

            if (responseCode >= 200 && responseCode < 300) {
                String jsonResponse = decodeResponse(connection); // Entire JSON response

                // Debug logger message
                plugin.getLoggingHelper().debug("Json response: " + jsonResponse);

                return processResponse(jsonResponse);
            } else {
                String errorResponse = decodeResponse(connection);
                throw new MessageClassificationException("HTTP error code: " + responseCode + ", Error message: " + errorResponse);
            }
        } catch (IOException e) {
            throw new MessageClassificationException("Failed to classify message due to an IO issue.", e);
        } catch (Exception e) {
            throw new MessageClassificationException("Failed to classify message due to a URL syntax issue.", e);
        }
    }

    /**
     * Sets up a connection to the API server and configures it
     *
     * @return The configured connection
     * @throws IOException        If any issue happens, an exception is thrown
     * @throws URISyntaxException Thrown if the set API URL isn't valid
     */
    private @NotNull HttpURLConnection createConnection() throws IOException, URISyntaxException {
        URL url = new URI(apiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * Sends an OpenAI-API-compliant API request to the given connection, pre-filled with the given user prompt
     *
     * @param connection The connection to send the request to
     * @param message    The user message
     * @throws IOException If any issue happens, an exception is thrown
     */
    private void sendRequest(@NotNull HttpURLConnection connection, @NotNull String message) throws IOException {
        Map<String, Object> json = Map.of("model", aiModel, "messages", new Map[]{Map.of("role", "system", APIConstants.CONTENT_KEY,
                sysPrompt + "Language: " + plugin.getConfigHelper().getString(ConfigConstants.LANGUAGE)), Map.of("role", "user",
                APIConstants.CONTENT_KEY, message)}, "response_format", Map.of("type", "json_object"));

        String jsonInputString = new Gson().toJson(json);

        // Debug logger message
        plugin.getLoggingHelper().debug("Json request: " + jsonInputString);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    /**
     * Processes a JSON string and maps it to a {@link MessageClassification} object
     *
     * @param jsonResponse The raw JSON string to decode
     * @return The filled {@code} MessageClassification object
     */
    private @NotNull MessageClassification processResponse(@NotNull String jsonResponse) {
        // Parse the outer JSON response
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Extract the content string from the first choice's message
        String contentString = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content")
                .getAsString();

        // Parse the content string as a JSON object
        JsonObject message = new Gson().fromJson(contentString, JsonObject.class);

        // Extract fields from the parsed content
        boolean isOffensiveLanguage =
                message.has(APIConstants.ISOFFENSIVELANGUAGE_KEY) && !message.get(APIConstants.ISOFFENSIVELANGUAGE_KEY).isJsonNull() &&
                        message.get(APIConstants.ISOFFENSIVELANGUAGE_KEY).getAsBoolean();
        boolean isUsername = message.has(APIConstants.ISUSERNAME_KEY) && !message.get(APIConstants.ISUSERNAME_KEY).isJsonNull() &&
                message.get(APIConstants.ISUSERNAME_KEY).getAsBoolean();
        boolean isPassword = message.has(APIConstants.ISPASSWORD_KEY) && !message.get(APIConstants.ISPASSWORD_KEY).isJsonNull() &&
                message.get(APIConstants.ISPASSWORD_KEY).getAsBoolean();
        boolean isHomeAddress = message.has(APIConstants.ISHOMEADDRESS_KEY) && !message.get(APIConstants.ISHOMEADDRESS_KEY).isJsonNull() &&
                message.get(APIConstants.ISHOMEADDRESS_KEY).getAsBoolean();
        boolean isEmailAddress =
                message.has(APIConstants.ISEMAILADDRESS_KEY) && !message.get(APIConstants.ISEMAILADDRESS_KEY).isJsonNull() &&
                        message.get(APIConstants.ISEMAILADDRESS_KEY).getAsBoolean();
        boolean isWebsite = message.has(APIConstants.ISWEBSITE_KEY) && !message.get(APIConstants.ISWEBSITE_KEY).isJsonNull() &&
                message.get(APIConstants.ISWEBSITE_KEY).getAsBoolean();
        String reason = message.has(APIConstants.REASON_KEY) && !message.get(APIConstants.REASON_KEY).isJsonNull() ? message.get(
                APIConstants.REASON_KEY).getAsString() : "No reason provided";

        return new MessageClassification(isOffensiveLanguage, isUsername, isPassword, isHomeAddress, isEmailAddress, isWebsite, reason);
    }

    /**
     * Decodes an incoming response from the API into a JSON string
     *
     * @param connection The connection to read the response from
     * @return The final JSON string
     * @throws IOException If any issue appears, an exception is thrown
     */
    private @NotNull String decodeResponse(@NotNull HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
}