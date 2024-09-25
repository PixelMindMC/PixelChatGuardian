/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class for managing configuration files
 */
public class ConfigHelper {
    private final PixelChat plugin;
    private final String path;
    private FileConfiguration fileConfiguration;
    private File file;

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
        String logLevel = fileConfiguration.getString(ConfigConstants.LOG_LEVEL);
        if (!"config.yml".equals(path))
            return;
        if (logLevel == null) {
            logLevel = String.valueOf(Level.INFO);
        }
        if (logLevel.equals(Level.FINEST.toString()) || logLevel.equals(Level.FINER.toString()) || logLevel.equals(Level.FINE.toString()) || logLevel.equals(Level.CONFIG.toString())) {
            logLevel = String.valueOf(Level.INFO);
        }
        plugin.getLogger().setLevel(Level.parse(logLevel));
    }

    /**
     * Method to save the config back to the file
     */
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(plugin.getConfigHelperLanguage().getString(LangConstants.FAILED_TO_SAVE_CONFIG) + " " + e);
        }
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