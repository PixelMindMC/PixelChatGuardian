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
import de.pixelmindmc.pixelchat.listener.AsyncPlayerChatListener;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import de.pixelmindmc.pixelchat.constants.LangConstants;
import de.pixelmindmc.pixelchat.utils.APIHelper;
import de.pixelmindmc.pixelchat.utils.ConfigHelper;
import de.pixelmindmc.pixelchat.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;

import static de.pixelmindmc.pixelchat.constants.APIConstants.GITHUB_RELEASES_URL;

public final class PixelChat extends JavaPlugin {
    public String updateCheckerLog;
    private ConfigHelper configHelper;
    private ConfigHelper configHelperLangCustom;
    private ConfigHelper configHelperLangEN;
    private APIHelper apiHelper;

    @Override
    public void onEnable() {
        loadConfigs();
        // Set log level from config
        getLogger().setLevel(Level.parse(getConfigHelper().getString(ConfigConstants.LOG_LEVEL)));
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
        String version = getDescription().getVersion();
        if (!version.equalsIgnoreCase(getConfigHelper().getString(ConfigConstants.CONFIG_VERSION)) && getLogger().isLoggable(Level.WARNING))
            getLogger().warning(getConfigHelperLanguage().getString(LangConstants.CONFIG_OUTDATED));

        if (!version.equalsIgnoreCase(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_VERSION)) && getLogger().isLoggable(Level.WARNING))
            getLogger().warning(getConfigHelperLanguage().getString(LangConstants.LANGUAGE_CONFIG_OUTDATED));
    }

    // Retrieves the plugin configuration
    public ConfigHelper getConfigHelper() {
        return configHelper;
    }

    // Retrieves the appropriate language configuration based on the plugin's config setting
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

    // Registers the API helper
    private void registerAPIHelper() {
        String apiKey = getConfigHelper().getString(ConfigConstants.API_KEY);
        if (!getConfigHelper().getBoolean(ConfigConstants.MODULE_CHATGUARD)) return;
        if (Objects.equals(apiKey, "API-KEY") || apiKey == null) {
            if (getLogger().isLoggable(Level.INFO))
                getLogger().warning(getConfigHelperLanguage().getString(LangConstants.NO_API_KEY_SET));
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
        if (getConfig().getBoolean(ConfigConstants.METRICS_ENABLED, true)) {
            if (getLogger().isLoggable(Level.INFO))
                getLogger().info(getConfigHelperLanguage().getString(LangConstants.METRICS_ENABLED));
            new Metrics(this, 23371);
        }
    }

    // Checks for updates to the plugin and logs the result
    private void checkForUpdates() throws URISyntaxException, MalformedURLException {
        if (getConfig().getBoolean(ConfigConstants.CHECK_FOR_UPDATES, true)) {
            if (getLogger().isLoggable(Level.INFO))
                getLogger().info(getConfigHelperLanguage().getString(LangConstants.CHECKING_UPDATES));
            updateCheckerLog = new UpdateChecker(this, new URI(GITHUB_RELEASES_URL).toURL()).checkForUpdates();
            getLogger().info(updateCheckerLog);
        }
    }
}