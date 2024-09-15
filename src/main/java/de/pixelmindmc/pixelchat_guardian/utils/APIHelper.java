/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 Gaming12846
 */

package de.pixelmindmc.pixelchat_guardian.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.pixelmindmc.pixelchat_guardian.PixelChat_Guardian;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class APIHelper {
    private final PixelChat_Guardian plugin;
    private final String ai_model;
    private final String api_url;
    private final String api_key;
    private final String sys_prompt;

    // Constructor for the APIHelper
    public APIHelper(PixelChat_Guardian plugin) {
        this.plugin = plugin;
        ai_model = plugin.getConfig().getString("ai-model");
        api_key = plugin.getConfig().getString("api-key");
        sys_prompt = plugin.getConfig().getString("sys-prompt");
        api_url = plugin.getConfig().getString("api-endpoint");
    }

    public String makeApiCall(String prompt) throws Exception {
        URL url = new URL(api_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + api_key);
        connection.setDoOutput(true);

        /*String jsonInputString = String.format("{" +
                "\"model\": \"" + ai_model + "\"," +
                "\"messages\": [" +
                "{" +
                "\"role\": \"system\"," +
                "\"content\": \"%s\"" +
                "}," +
                "{" +
                "\"role\": \"user\"," +
                "\"content\": \"%s\"" +
                "}" +
                "]" +
                "}", sys_prompt.replace("\"", "\\\""), prompt.replace("\"", "\\\""));
        */

        Map<String, Object> json = Map.of(
                "model", ai_model,
                "messages", new Map[] {
                        Map.of("role", "system", "content", sys_prompt),
                        Map.of("role", "user", "content", prompt)
                },
                "response_format", Map.of("type", "json_object")
        );

        String jsonInputString = new Gson().toJson(json);


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode(); //HTTP-Code der Antwort

        if (responseCode >= 200 && responseCode < 300) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            String jsonResponse = response.toString(); //Ganze JSON-Antwort

            /* JSON parsen und Antwort rausziehen
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String content = jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
             */

            //NEU JSON vernünftig parsen
            // Parse the outer JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Extract the content string from the first choice's message
            String contentString = jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // Parse the content string as a JSON object
            JsonObject message = JsonParser.parseString(contentString).getAsJsonObject();

            plugin.getLogger().info("Ganze API Response: " + contentString);
            plugin.getLogger().info("Block?: " + message.get("block"));

            // Extract fields from the parsed content
            boolean block = message.has("block") && !message.get("block").isJsonNull() && message.get("block").getAsBoolean();
            String reason = message.has("reason") && !message.get("reason").isJsonNull() ? message.get("reason").getAsString() : "No reason provided";
            String action = message.has("action") && !message.get("action").isJsonNull() ? message.get("action").getAsString() : "No reason provided";

            plugin.getLogger().info("block: " + block);
            plugin.getLogger().info("reason: " + reason);
            plugin.getLogger().info("action: " + action);


            if (block)
                return "BLOCK";
            else
                return "ALLOW";

        } else {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
            }
            plugin.getLogger().warning("Error Response: " + errorResponse);
            throw new Exception("HTTP error code: " + responseCode + ", Error message: " + errorResponse);
        }
    }
}