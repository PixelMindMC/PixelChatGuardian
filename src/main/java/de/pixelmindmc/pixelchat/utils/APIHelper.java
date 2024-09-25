/*
 * This file is part of PixelChatGuardian.
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

public class APIHelper {
    private final String aiModel;
    private final String apiUrl;
    private final String apiKey;
    private final String sysPrompt;

    // Constructor for the APIHelper
    public APIHelper(PixelChat plugin) {
        aiModel = plugin.getConfig().getString(ConfigConstants.AI_MODEL);
        apiKey = plugin.getConfig().getString(ConfigConstants.API_KEY);
        sysPrompt = plugin.getConfig().getString(ConfigConstants.SYSTEM_PROMPT);
        apiUrl = plugin.getConfig().getString(ConfigConstants.API_ENDPOINT);
    }

    public MessageClassification classifyMessage(String prompt) throws MessageClassificationException {
        try {
            HttpURLConnection connection = createConnection();
            sendRequest(connection, prompt);
            int responseCode = connection.getResponseCode(); // HTTP code of the response

            if (responseCode >= 200 && responseCode < 300) {
                String jsonResponse = decodeResponse(connection); // Entire JSON response
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

    private HttpURLConnection createConnection() throws IOException, URISyntaxException {
        URL url = new URI(apiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        return connection;
    }

    private void sendRequest(HttpURLConnection connection, String prompt) throws IOException {
        Map<String, Object> json = Map.of(
                "model", aiModel,
                "messages", new Map[]{
                        Map.of("role", "system", APIConstants.CONTENT_KEY, sysPrompt),
                        Map.of("role", "user", APIConstants.CONTENT_KEY, prompt)
                },
                "response_format", Map.of("type", "json_object")
        );

        String jsonInputString = new Gson().toJson(json);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private MessageClassification processResponse(String jsonResponse) {
        // Parse the outer JSON response
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Extract the content string from the first choice's message
        String contentString = jsonObject.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        // Parse the content string as a JSON object
        JsonObject message = new Gson().fromJson(contentString, JsonObject.class);

        // Extract fields from the parsed content
        boolean block = message.has(APIConstants.BLOCK_KEY) && !message.get(APIConstants.BLOCK_KEY).isJsonNull() && message.get(APIConstants.BLOCK_KEY).getAsBoolean();
        String reason = message.has(APIConstants.REASON_KEY) && !message.get(APIConstants.REASON_KEY).isJsonNull() ? message.get(APIConstants.REASON_KEY).getAsString() : "No reason provided";

        return new MessageClassification(block, reason);
    }

    private String decodeResponse(HttpURLConnection connection) throws IOException {
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