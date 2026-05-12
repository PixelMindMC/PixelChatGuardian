/*
 * Copyright (C) 2024 - 2026 PixelMindMC
 *
 * This file is part of PixelChat Guardian.
 *
 * PixelChat Guardian is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * PixelChat Guardian is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with PixelChat Guardian.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package de.pixelmindmc.pixelchat.utils;

import de.pixelmindmc.pixelchat.PixelChat;
import de.pixelmindmc.pixelchat.constants.ConfigConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Handles automatic migration of configuration keys between plugin versions.
 * Each supported version transition has its own dedicated method; new migrations
 * should be added as additional private methods following the same pattern.
 */
public class ConfigMigrationHelper {
    private final @NotNull PixelChat plugin;
    private final @NotNull ConfigHelper configHelper;
    private final @NotNull LoggingHelper loggingHelper;

    /**
     * Constructs a ConfigMigrationHelper
     *
     * @param plugin The plugin instance
     */
    public ConfigMigrationHelper(@NotNull PixelChat plugin) {
        this.plugin = plugin;
        this.configHelper = plugin.getConfigHelper();
        this.loggingHelper = plugin.getLoggingHelper();
    }

    /**
     * Runs all applicable migrations for the current config file.
     * Migration is skipped entirely when the stored config version already matches
     * the running plugin version. Otherwise only the steps relevant to the detected
     * version range are applied.
     */
    public void migrate() {
        String pluginVersion = plugin.getDescription().getVersion();
        String configVersion = configHelper.getString(ConfigConstants.CONFIG_VERSION);

        // Config is already at the current version — nothing to migrate
        if (pluginVersion.equals(configVersion)) return;

        boolean migrated = false;

        // v1.2.0 → v1.3.0
        if (isConfigVersionOlderThan(configVersion, "1.3.0")) {
            migrated |= migrateTo1_3_0();
        }

        if (migrated) {
            loggingHelper.info("Configuration migrated to the " + pluginVersion + " format.");
        }
    }

    /**
     * Applies the migrations introduced in v1.3.0:
     * <ul>
     *   <li>{@code chatguard.notify-user} (flat key) is moved to {@code chatguard.notify.user} (nested)</li>
     *   <li>{@code chatguard.notify.admins} is added with a default of {@code true} when absent</li>
     * </ul>
     *
     * @return {@code true} if any change was written to the config
     */
    private boolean migrateTo1_3_0() {
        boolean changed = false;

        // chatguard.notify-user (flat) → chatguard.notify.user (nested)
        if (configHelper.contains("chatguard.notify-user")) {
            boolean notifyUser = configHelper.getBoolean("chatguard.notify-user");
            configHelper.set(ConfigConstants.ChatGuard.Notify.USER, notifyUser);
            configHelper.set("chatguard.notify-user", null);
            changed = true;
        }

        // chatguard.notify.admins is a new key; set default if absent
        if (!configHelper.contains(ConfigConstants.ChatGuard.Notify.ADMINS)) {
            configHelper.set(ConfigConstants.ChatGuard.Notify.ADMINS, true);
            changed = true;
        }

        return changed;
    }

    /**
     * Returns {@code true} if {@code configVersion} is strictly older than {@code targetVersion},
     * or if {@code configVersion} is {@code null}/empty (pre-dates version tracking).
     *
     * @param configVersion The version string stored in the config file
     * @param targetVersion The version to compare against
     * @return {@code true} if the config predates the target version
     */
    private boolean isConfigVersionOlderThan(String configVersion, @NotNull String targetVersion) {
        if (configVersion == null || configVersion.isEmpty()) return true;

        String[] configParts = configVersion.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        for (int i = 0; i < Math.min(configParts.length, targetParts.length); i++) {
            try {
                int configPart = Integer.parseInt(configParts[i]);
                int targetPart = Integer.parseInt(targetParts[i]);
                if (configPart < targetPart) return true;
                if (configPart > targetPart) return false;
            } catch (NumberFormatException e) {
                // unparseable version segment — log and assume up-to-date to avoid re-migrating
                loggingHelper.warning("Could not parse config version '" + configVersion + "' for migration check.");
                return false;
            }
        }

        // All shared parts are equal; treat trailing ".0" segments as equal (e.g. "1.3" == "1.3.0")
        for (int i = Math.min(configParts.length, targetParts.length); i < targetParts.length; i++) {
            try {
                if (Integer.parseInt(targetParts[i]) > 0) return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
