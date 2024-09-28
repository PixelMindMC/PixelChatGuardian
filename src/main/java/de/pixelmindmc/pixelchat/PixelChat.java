/*
 * This file is part of PixelChatGuardian.
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public final class PixelChat extends JavaPlugin {
    private final LoggingHelper loggingHelper = new LoggingHelper(this);
    public boolean updateAvailable = false;

    // ConfigHelper instances
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
        registerTabCompleters(new TabCompleter());
        initializeMetrics();
        /*try {
            checkForUpdates();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }*/
    }

    /**
     * Loads the plugin's configuration files and checks their versions
     */
    private void loadConfigs() {
        // Debug logger message
        getLoggingHelper().debug("Loading configurations");

        configHelper = new ConfigHelper(this, "config.yml");
        configHelperLangCustom = new ConfigHelper(this, "languages/lang_custom.yml");
        configHelperLangEN = new ConfigHelper(this, "languages/lang_en.yml");

        // Check config versions
        String version = getDescription().getVersion();
        if (!version.equalsIgnoreCase(getConfigHelper().getString(ConfigConstants.CONFIG_VERSION)))
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.CONFIG_OUTDATED));

        if (!version.equalsIgnoreCase(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_VERSION)))
            getLoggingHelper().warning(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_OUTDATED));
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
     * Retrieves the appropriate language configuration based on the plugin's config setting
     *
     * @return The config helper for the language set in the configuation
     */
    public ConfigHelper getConfigHelperLanguage() {
        String language = getConfigHelper().getString(ConfigConstants.LANGUAGE);

        switch (language.toLowerCase()) {
            case "custom" -> {
                return configHelperLangCustom;
            }
            default -> {
                return configHelperLangEN;
            }
        }
    }

    /**
     * Configures and registers the {@link APIHelper}
     */
    private void registerAPIHelper() {
        // Debug logger message
        getLoggingHelper().debug("Register API helper");

        String apiKey = getConfigHelper().getString(ConfigConstants.API_KEY);
        if (!getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) return;
        if (Objects.equals(apiKey, "API-KEY") || apiKey == null) {
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

        pluginManager.registerEvents(new AsyncPlayerChatListener(this), this);
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
     * Registers tab completers for the plugin's commands
     *
     * @param tabCompleter The {@code TabCompleter}
     */
    private void registerTabCompleters(TabCompleter tabCompleter) {
        // Debug logger message
        getLoggingHelper().debug("Register tabcompleters");

        Objects.requireNonNull(getCommand("pixelchat")).setTabCompleter(tabCompleter);
    }

    /**
     * Initializes the bStats metrics for the plugin
     */
    private void initializeMetrics() {
        if (getConfig().getBoolean(ConfigConstants.METRICS_ENABLED, true)) {
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
    private void checkForUpdates() throws URISyntaxException, MalformedURLException {
        if (getConfig().getBoolean(ConfigConstants.CHECK_FOR_UPDATES, true)) {
            getLoggingHelper().info(getConfigHelperLanguage().getString(LangConstants.CHECKING_FOR_UPDATES));
            String updateChecker = new UpdateChecker(this, new URI("https://api.github.com/repos/PixelMindMC/PixelChatGuardian/releases/latest").toURL()).checkForUpdates();
            getLoggingHelper().info(updateChecker);
            if (updateChecker.equals(LangConstants.UPDATE_AVAILABLE)) updateAvailable = true;
        }
    }

    // Retrieves the LoggingHelper instance
    public LoggingHelper getLoggingHelper() {
        return loggingHelper;
    }
}