/*
 * This file is part of PixelChat Guardian.
 * Copyright (C) $today.year PixelMindMC
 *
 * PixelChatGuardian is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelChatGuardian is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.pixelmindmc.pixelchat;

import de.pixelmindmc.pixelchat.commands.PixelChatCommand;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.listener.AsyncPlayerChatListener;
import de.pixelmindmc.pixelchat.utils.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Set;

/**
 * The main class for the PixelChat Guardian plugin
 */
public final class PixelChat extends JavaPlugin {
    private final LoggingHelper loggingHelper = new LoggingHelper(this);
    public String updateChecker;

    // ConfigHelper instances
    private ConfigHelper configHelper;
    private ConfigHelper configHelperPlayerStrikes;
    private ConfigHelper configHelperLangCustom;
    private ConfigHelper configHelperLangGerman;
    private ConfigHelper configHelperLangEnglish;
    private ConfigHelper configHelperLangSpanish;
    private ConfigHelper configHelperLangFrench;
    private ConfigHelper configHelperLangDutch;
    private ConfigHelper configHelperLangSimplifiedChinese;
    private ConfigHelper configHelperLangTraditionalChinese;

    private AsyncPlayerChatListener asyncPlayerChatListener;

    private APIHelper apiHelper;

    // Called when the plugin is first enabled
    @Override
    public void onEnable() {
        loadConfigs();
        registerAPIHelper();
        registerListeners(getServer().getPluginManager());
        registerCommands();
        registerTabCompleter(new TabCompleter());
        initializeMetrics();
        try {
            checkForUpdates();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the plugin's configuration files and checks their versions
     */
    private void loadConfigs() {
        // Debug logger message
        getLoggingHelper().debug("Loading configurations");

        configHelper = new ConfigHelper(this, "config.yml");
        configHelperPlayerStrikes = new ConfigHelper(this, "player_strikes.yml");
        configHelperLangCustom = new ConfigHelper(this, "locale/locale_custom.yml");
        configHelperLangGerman = new ConfigHelper(this, "locale/locale_de.yml");
        configHelperLangEnglish = new ConfigHelper(this, "locale/locale_en.yml");
        configHelperLangSpanish = new ConfigHelper(this, "locale/locale_es.yml");
        configHelperLangFrench = new ConfigHelper(this, "locale/locale_fr.yml");
        configHelperLangDutch = new ConfigHelper(this, "locale/locale_nl.yml");
        configHelperLangSimplifiedChinese = new ConfigHelper(this, "locale/locale_zh-cn.yml");
        configHelperLangTraditionalChinese = new ConfigHelper(this, "locale/locale_zh-tw.yml");

        // Check config versions
        String version = getDescription().getVersion();
        if (!version.equalsIgnoreCase(getConfigHelper().getString(ConfigConstants.CONFIG_VERSION)))
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.CONFIG_OUTDATED));

        if (!version.equalsIgnoreCase(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_VERSION)))
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_OUTDATED));

        // Check if it is the first time using this plugin
        if (!getConfigHelper().getFileExist())
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.FIRST_TIME_MESSAGE));

        // Reset the strike count of every player if enabled
        if (getConfigHelper().getBoolean(ConfigConstants.CHATGUARD_CLEAR_STRIKES_ON_SERVER_RESTART))
            resetPlayerStrikesOnServerStart();
    }

    /**
     * Retrieves the plugin configuration
     *
     * @return The {@code ConfigHelper}
     */
    public ConfigHelper getConfigHelper() {
        return configHelper;
    }

    /**
     * Retrieves the player strikes configuration
     *
     * @return The {@code ConfigHelper}
     */
    public ConfigHelper getConfigHelperPlayerStrikes() {
        return configHelperPlayerStrikes;
    }

    /**
     * Retrieves the appropriate language configuration based on the plugin's config setting
     *
     * @return The config helper for the language set in the configuration
     */
    public ConfigHelper getConfigHelperLanguage() {
        String language = getConfigHelper().getString(ConfigConstants.LANGUAGE);

        switch (language.toLowerCase()) {
            case "custom" -> {
                return configHelperLangCustom;
            }
            case "de" -> {
                return configHelperLangGerman;
            }
            case "es" -> {
                return configHelperLangSpanish;
            }
            case "fr" -> {
                return configHelperLangFrench;
            }
            case "nl" -> {
                return configHelperLangDutch;
            }
            case "zh-cn" -> {
                return configHelperLangSimplifiedChinese;
            }
            case "zh-tw" -> {
                return configHelperLangTraditionalChinese;
            }
            default -> {
                return configHelperLangEnglish;
            }
        }
    }

    /**
     * Resets the strike count of every player to 0 on server start
     */
    private void resetPlayerStrikesOnServerStart() {
        ConfigHelper configHelperPlayerStrikes = getConfigHelperPlayerStrikes();

        // Get all the top-level keys in the config (assuming these are player UUIDs)
        Set<String> playerUUIDs = configHelperPlayerStrikes.getKeys("");

        // Iterate through each player UUID and reset the strike count
        for (String playerUUID : playerUUIDs) {
            // Check if the player has a strikes entry in the config
            if (configHelperPlayerStrikes.contains(playerUUID + ".strikes")) {
                // Set the strike count to 0
                configHelperPlayerStrikes.set(playerUUID + ".strikes", 0);

                // Debug logger message
                getLoggingHelper().debug("Reset strikes for player with UUID: " + playerUUID);
            }
        }

        // Log the completion of strike reset
        getLoggingHelper().info(getConfigHelperLanguage().getString(LangConstants.CLEARED_STRIKES_ON_SERVER_RESTART));
    }

    /**
     * Configures and registers the {@link APIHelper}
     */
    private void registerAPIHelper() {
        // Debug logger message
        getLoggingHelper().debug("Register API helper");

        String apiKey = getConfigHelper().getString(ConfigConstants.API_KEY);
        if (!getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) return;
        if (getConfigHelper().getFileExist() && Objects.equals(apiKey, "API-KEY") || apiKey == null) {
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.NO_API_KEY_SET));
            return;
        }
        apiHelper = new APIHelper(this);
    }

    /**
     * Retries the API helper
     *
     * @return The plugin's APIHelper
     */
    public APIHelper getAPIHelper() {
        return apiHelper;
    }

    /**
     * Registers events with the Bukkit plugin manager
     *
     * @param pluginManager The plugin manager to register the events to
     */
    private void registerListeners(PluginManager pluginManager) {
        // Debug logger message
        getLoggingHelper().debug("Register listeners");

        pluginManager.registerEvents(asyncPlayerChatListener = new AsyncPlayerChatListener(this), this);
    }


    /**
     * Registers commands with their respective executors
     */
    private void registerCommands() {
        // Debug logger message
        getLoggingHelper().debug("Register commands");

        Objects.requireNonNull(getCommand("pixelchat")).setExecutor(new PixelChatCommand(this));
    }

    /**
     * Registers tabcompleter for the plugin's commands
     *
     * @param tabCompleter The {@code TabCompleter}
     */
    private void registerTabCompleter(TabCompleter tabCompleter) {
        // Debug logger message
        getLoggingHelper().debug("Register tabcompleter");

        Objects.requireNonNull(getCommand("pixelchat")).setTabCompleter(tabCompleter);
    }

    /**
     * Initializes the bStats metrics for the plugin
     */
    private void initializeMetrics() {
        if (getConfig().getBoolean(ConfigConstants.METRICS_ENABLED)) {
            getLoggingHelper().info(getConfigHelperLanguage().getString(LangConstants.METRICS_ENABLED));
            new Metrics(this, 23371);
        }
    }

    /**
     * Checks for updates to the plugin and logs the result
     *
     * @throws URISyntaxException    If the set URL is invalid
     * @throws MalformedURLException If the set URL is invalid
     */
    private void checkForUpdates() throws URISyntaxException, IOException {
        if (getConfig().getBoolean(ConfigConstants.CHECK_FOR_UPDATES)) {
            getLoggingHelper().info(getConfigHelperLanguage().getString(LangConstants.CHECKING_FOR_UPDATES));
            updateChecker = new UpdateChecker(this, new URI("https://api.github.com/repos/PixelMindMC/PixelChatGuardian/releases/latest").toURL()).checkForUpdates();
            getLoggingHelper().info(updateChecker);
        }
    }

    // Retrieves the string updateChecker
    public String updateChecker() {
        return updateChecker;
    }

    // Retrieves the LoggingHelper instance
    public LoggingHelper getLoggingHelper() {
        return loggingHelper;
    }

}