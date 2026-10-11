/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.epic.exoticgarden.core.Lang;

/**
 * SpigotMC update checker. Asynchronously queries the legacy update API; when a newer version
 * exists it prints a colored notice to the console (ConsoleSender supports color codes, the
 * logger does not) and reminds admins (OP or epicexoticgarden.reload) when they join. Both
 * messages are language-driven via the {@code _cmd_update_*} keys.
 */
public class UpdateChecker implements Listener {

    private final EpicExoticGarden plugin;
    private final int resourceId;

    /** Written by the async lookup thread, read on the main thread (join event). */
    private volatile boolean updateAvailable;

    public UpdateChecker(@Nonnull EpicExoticGarden plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Starts the async version lookup and prints the console notice when an update exists. */
    public void checkForUpdates() {
        String current = plugin.getDescription().getVersion();
        getVersion(version -> {
            if (version == null || version.isEmpty()) {
                return;
            }
            updateAvailable = isNewerVersion(version, current);
            if (updateAvailable) {
                Bukkit.getConsoleSender().sendMessage(Lang.cmd("_cmd_update_console",
                        color("&7[&bEpicExoticGarden&7] &aUpdate available: &b{0}"), getResourceUrl()));
            }
        });
    }

    /** Reminds joining operators once a newer version was detected (config: update-notify-join). */
    @EventHandler
    public void onPlayerJoin(@Nonnull PlayerJoinEvent event) {
        if (!updateAvailable || !plugin.getConfig().getBoolean("update-notify-join", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isOp()) {
            return;
        }
        player.sendMessage(Lang.cmd("_cmd_update_join",
                color("&7[&bEpicExoticGarden&7] &aUpdate available:")));
        player.sendMessage(Lang.cmd("_cmd_update_url", color("&b{0}"), getResourceUrl()));
    }

    public String getResourceUrl() {
        return "https://www.spigotmc.org/resources/" + resourceId;
    }

    /**
     * Semantic-ish comparison: the remote version counts as newer only when its numeric part is
     * higher, or the numeric parts are equal and the remote drops a pre-release suffix (e.g.
     * "1.0.3" outranks "1.0.3-BETA-4"). Equal or older remote versions never trigger a notice.
     */
    static boolean isNewerVersion(@Nonnull String remote, @Nonnull String current) {
        String[] remoteParts = splitVersion(remote);
        String[] currentParts = splitVersion(current);
        int[] remoteNums = numericParts(remoteParts[0]);
        int[] currentNums = numericParts(currentParts[0]);
        int length = Math.max(remoteNums.length, currentNums.length);
        for (int i = 0; i < length; i++) {
            int r = i < remoteNums.length ? remoteNums[i] : 0;
            int c = i < currentNums.length ? currentNums[i] : 0;
            if (r != c) {
                return r > c;
            }
        }
        // Same numeric base: a plain release outranks a pre-release of that base version.
        boolean remotePre = remoteParts.length > 1;
        boolean currentPre = currentParts.length > 1;
        if (remotePre != currentPre) {
            return currentPre;
        }
        return remotePre && remoteParts[1].compareToIgnoreCase(currentParts[1]) > 0;
    }

    /** Strips a leading "v" and splits off the first "-" pre-release separator. */
    @Nonnull
    private static String[] splitVersion(@Nonnull String version) {
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        return v.split("-", 2);
    }

    @Nonnull
    private static int[] numericParts(@Nonnull String base) {
        String[] tokens = base.split("\\.");
        int[] numbers = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                numbers[i] = Integer.parseInt(tokens[i].replaceAll("[^0-9].*$", ""));
            } catch (NumberFormatException e) {
                numbers[i] = 0;
            }
        }
        return numbers;
    }

    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String apiUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
            try (InputStream is = new URL(apiUrl).openStream();
                 Scanner scanner = new Scanner(is)) {
                consumer.accept(scanner.hasNext() ? scanner.next().trim() : null);
            } catch (IOException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
                consumer.accept(null);
            }
        });
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
