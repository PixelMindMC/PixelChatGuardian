/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 Gaming12846
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

// Utility class for managing configuration files
public class ConfigHelper {
    private final PixelChat plugin;
    private final String path;
    private FileConfiguration fileConfiguration;
    private File file;

    // Constructor for the ConfigHelper
    public ConfigHelper(PixelChat plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        saveDefaultConfig();
        loadConfig();
    }

    // Method to save the default language config if it doesn't exist
    public void saveDefaultConfig() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) plugin.saveResource(path, false);
    }

    // Method to load or reload the language config file
    public void loadConfig() {
        file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) saveDefaultConfig();

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    // Method to save the language config back to the file
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(plugin.getConfigHelperLanguage().getString("failed-to-save-config") + " " + e);
        }
    }

    // Retrieves the FileConfiguration
    public FileConfiguration getConfig() {
        return fileConfiguration;
    }

    // Retrieve a message from the language config
    public String getString(String path) {
        // Get the value from the current language config
        String message = fileConfiguration.getString(path);

        // If the message is null or empty, replace it with the default value
        if (message == null || message.trim().isEmpty()) {
            // Load the default config from the plugin's jar
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), path));

            // Get the default message from the default config
            String defaultMessage = defaultConfig.getString(path, "Message not found: " + path);

            // Update the live config with the default message and save the changes
            fileConfiguration.set(path, defaultMessage);
            saveConfig(); // Save the updated config

            // Return the default message
            return defaultMessage;
        }

        return message;
    }
}