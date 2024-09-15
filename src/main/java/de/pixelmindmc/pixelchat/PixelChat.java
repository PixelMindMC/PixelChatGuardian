/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) 2024 PixelMindMC
 */

package de.pixelmindmc.pixelchat;

import de.pixelmindmc.pixelchat.commands.PixelChatCommand;
import de.pixelmindmc.pixelchat.listener.AsyncPlayerChatListener;
import de.pixelmindmc.pixelchat.utils.APIHelper;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import de.pixelmindmc.pixelchat.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public final class PixelChat extends JavaPlugin {
    public static final String PLUGIN_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.RED + ChatColor.BOLD + "Pixel" + ChatColor.BLUE + "Chat" + ChatColor.RESET + ChatColor.DARK_GRAY + "]" + ChatColor.RESET + " ";
    public final String configVersion = "1.0";
    public final String languageConfigVersion = "1.0";
    public String updateCheckerLog;
    private ConfigHelper configHelper;
    private ConfigHelper configHelperLangCustom;
    private ConfigHelper configHelperLangEN;
    private APIHelper apiHelper;

    @Override
    public void onEnable() {
        loadConfigs();
        registerAPIHelper();
        registerListeners(getServer().getPluginManager());
        registerCommands();
        initializeMetrics();
        //checkForUpdates();
    }

    // Loads the plugin's configuration files and checks their versions
    private void loadConfigs() {
        configHelper = new ConfigHelper(this, "config.yml");
        configHelperLangCustom = new ConfigHelper(this, "languages/lang_custom.yml");
        configHelperLangEN = new ConfigHelper(this, "languages/lang_en.yml");

        // Check config versions
        if (!configVersion.equalsIgnoreCase(getConfigHelper().getString("version")))
            getLogger().warning(getConfigHelperLanguage().getConfig().getString("config-outdated"));

        if (!languageConfigVersion.equalsIgnoreCase(getConfigHelperLanguage().getString("version")))
            getLogger().warning(getConfigHelperLanguage().getConfig().getString("language-config-outdated"));
    }

    // Retrieves the plugin configuration
    public ConfigHelper getConfigHelper() {
        return configHelper;
    }

    // Retrieves the appropriate language configuration based on the plugin's config setting
    public ConfigHelper getConfigHelperLanguage() {
        String language = getConfigHelper().getString("language");

        switch (language.toLowerCase()) {
            case "custom" -> {
                return configHelperLangCustom;
            }
            default -> {
                return configHelperLangEN;
            }
        }
    }

    // Registers the API helper
    private void registerAPIHelper() {
        String apiKey = getConfigHelper().getString("api-key");
        if (!getConfigHelper().getConfig().getBoolean("modules.chatguard")) return;
        if (Objects.equals(apiKey, "API-KEY") || apiKey == null) {
            getLogger().warning(getConfigHelperLanguage().getString("no-api-key-set"));
            return;
        }
        apiHelper = new APIHelper(this);
    }

    // Retries the API helper
    public APIHelper getAPIHelper() {
        return apiHelper;
    }

    // Registers events with the Bukkit plugin manager
    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new AsyncPlayerChatListener(this), this);
    }

    // Registers commands with their respective executors
    private void registerCommands() {
        Objects.requireNonNull(getCommand("pixelchat")).setExecutor(new PixelChatCommand(this));
    }

    // Initializes the bStats metrics for the plugin
    private void initializeMetrics() {
        if (getConfig().getBoolean("metrics-enabled", true)) {
            getLogger().info(getConfigHelperLanguage().getString("metrics-enabled"));
            new Metrics(this, 23371);
        }
    }

    // Checks for updates to the plugin and logs the result
    private void checkForUpdates() throws MalformedURLException {
        if (getConfig().getBoolean("check-for-updates", true)) {
            getLogger().info(getConfigHelperLanguage().getString("checking-updates"));
            updateCheckerLog = new UpdateChecker(this, new URL("https://api.github.com/repos/Gaming12846/PixelChatGuardian/releases/latest")).checkForUpdates();
            getLogger().info(updateCheckerLog);
        }
    }
}