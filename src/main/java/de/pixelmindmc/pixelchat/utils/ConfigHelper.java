/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing configuration files
 */
public class ConfigHelper {
    private final PixelChat plugin;
    private final String path;
    private FileConfiguration fileConfiguration;
    private File file;

    /**
     * Constructs a ConfigHelper object
     *
     * @param plugin The plugin instance
     * @param path   The path of the configuration file
     */
    public ConfigHelper(PixelChat plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        saveDefaultConfig();
        loadConfig();
    }

    /**
     * Method to save the default config if it doesn't exist
     */
    public void saveDefaultConfig() {
        file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) plugin.saveResource(path, false);
    }

    /**
     * Method to load or reload the config file
     */
    public void loadConfig() {
        file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) saveDefaultConfig();

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Method to save the config back to the file
     */
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
            loadConfig();
        } catch (IOException e) {
            plugin.getLoggingHelper().error(plugin.getConfigHelperLanguage().getString(LangConstants.FAILED_TO_SAVE_CONFIG) + " " + e);
        }
    }

    /**
     * Method to set a specified path with the given value
     */
    public void set(String path, Object value) {
        fileConfiguration.set(path, value);
        saveConfig();
    }

    /**
     * Checks if the specified path exists in the file configuration
     *
     * @param path The path of the value
     * @return The value
     */
    public boolean contains(String path) {
        return fileConfiguration.contains(path);
    }

    /**
     * Retrieve a string from the config
     *
     * @param path The path of the value
     * @return The value, or a "Message not found" message
     */
    public String getString(String path) {
        // Get the value from the current language config
        String message = fileConfiguration.getString(path);

        // If the message is null or empty, replace it with the default value
        if (message == null || message.trim().isEmpty()) {
            // Load the default config from the plugin's jar
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), path));

            // Return the default message
            return defaultConfig.getString(path, "Message not found: " + path);
        }

        return message;
    }

    /**
     * Retrieve a boolean from the config
     *
     * @param path The path of the value
     * @return The value
     */
    public boolean getBoolean(String path) {
        return fileConfiguration.getBoolean(path);
    }

    /**
     * Retrieve an int from the config
     *
     * @param path The path of the value
     * @return The value
     */
    public int getInt(String path) {
        return fileConfiguration.getInt(path);
    }

    /**
     * Retrieve a string map from the config
     *
     * @param path The path of the value
     * @return The string map
     */
    public Map<String, String> getStringMap(String path) {
        Map<String, String> resultMap = new HashMap<>();
        ConfigurationSection section = fileConfiguration.getConfigurationSection(path);

        // If the section is not null, iterate over its keys and add them to the map
        if (section == null) {
            // Load the default config from the plugin's jar
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), path));
            section = defaultConfig.getConfigurationSection(path);

            assert section != null;
            for (String key : section.getKeys(false)) {
                // Get the value associated with the key
                String value = section.getString(key);
                // Put the key-value pair in the resultMap
                resultMap.put(key, value);
            }

            // Return the default message
            return resultMap;
        }

        for (String key : section.getKeys(false)) {
            // Get the value associated with the key
            String value = section.getString(key);
            // Put the key-value pair in the resultMap
            resultMap.put(key, value);
        }
        return resultMap;
    }
}