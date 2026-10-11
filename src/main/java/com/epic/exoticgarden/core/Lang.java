/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.epic.exoticgarden.EpicExoticGarden;

/**
 * Loads item display names from {@code languages/<locale>/items.yml}.
 *
 * <p>The configured language is used for physical item names; missing keys fall back to English
 * and finally to a humanized version of the id. No display strings are hardcoded in Java.</p>
 */
public final class Lang {

    private Lang() {}

    /** Meta key (not an item id) holding the edible-item lore template; {@code {0}} = hunger value. */
    private static final String FOOD_LORE_KEY = "_food_lore";

    /** Meta key holding the shared multi-line lore template applied to every magical essence. */
    private static final String ESSENCE_LORE_KEY = "_essence_lore";

    /** Prefix for GUI-text meta keys (titles, category names); the value is a plain scalar. */
    private static final String GUI_PREFIX = "_gui_";

    /** Prefix for command-message meta keys (/eeg feedback lines); the value is a plain scalar. */
    private static final String CMD_PREFIX = "_cmd_";

    /** Prefix for console-log meta keys (startup/reload summaries); plain text, no color codes. */
    private static final String LOG_PREFIX = "_log_";

    /** Prefix for generated-file content keys (recipe/models.yml comments); plain text. */
    private static final String FILE_PREFIX = "_file_";

    private static final Map<String, String> PRIMARY = new HashMap<>();
    private static final Map<String, String> ENGLISH = new HashMap<>();

    /** Admin-defined extra lore lines per item id ({@code lore:} list under the item entry). */
    private static final Map<String, List<String>> LORE_PRIMARY = new HashMap<>();
    private static final Map<String, List<String>> LORE_ENGLISH = new HashMap<>();

    /** Shared list-valued meta templates (currently {@link #ESSENCE_LORE_KEY}). */
    private static final Map<String, List<String>> LISTS_PRIMARY = new HashMap<>();
    private static final Map<String, List<String>> LISTS_ENGLISH = new HashMap<>();
    @Nullable
    private static String foodLorePrimary;
    @Nullable
    private static String foodLoreEnglish;
    private static String language = "en";
    @Nullable
    private static File languagesFolder;

    public static void init(@Nonnull String configuredLanguage, @Nonnull File diskLanguagesFolder) {
        language = configuredLanguage;
        languagesFolder = diskLanguagesFolder;
        ENGLISH.clear();
        PRIMARY.clear();
        LORE_ENGLISH.clear();
        LORE_PRIMARY.clear();
        LISTS_ENGLISH.clear();
        LISTS_PRIMARY.clear();
        foodLoreEnglish = loadLanguage("en", ENGLISH, LORE_ENGLISH, LISTS_ENGLISH);
        foodLorePrimary = null;
        if (!"en".equalsIgnoreCase(configuredLanguage)) {
            foodLorePrimary = loadLanguage(configuredLanguage, PRIMARY, LORE_PRIMARY, LISTS_PRIMARY);
        }
        EpicExoticGarden.getInstance().getLogger().info(
                log("_log_language_loaded", "Language loaded: {0} ({1} localized entries, {2} English entries)",
                        language, PRIMARY.size(), ENGLISH.size()));
    }

    /**
     * Loads one locale: the jar-bundled copy supplies the defaults, then the extracted file on
     * disk overlays them (so old installs automatically gain keys added by plugin updates while
     * admin edits still win).
     *
     * @return the loaded {@code _food_lore} template, if a source defines one
     */
    @Nullable
    private static String loadLanguage(@Nonnull String locale, @Nonnull Map<String, String> target,
                                        @Nonnull Map<String, List<String>> loreTarget,
                                        @Nonnull Map<String, List<String>> listsTarget) {
        String template = null;

        // ClassLoader resource names are absolute and must NOT start with '/' (that rule only
        // applies to Class#getResource); a leading slash silently returns null.
        InputStream jarIn = EpicExoticGarden.getInstance().getResource("languages/" + locale + "/items.yml");
        if (jarIn != null) {
            try {
                String fromJar = parseLanguage(jarIn, target, loreTarget, listsTarget);
                if (fromJar != null) {
                    template = fromJar;
                }
            } catch (Exception e) {
                EpicExoticGarden.getInstance().getLogger().log(java.util.logging.Level.SEVERE,
                        "Failed to load bundled language " + locale, e);
            } finally {
                try {
                    jarIn.close();
                } catch (Exception ignored) {
                    // best effort
                }
            }
        } else {
            EpicExoticGarden.getInstance().getLogger().warning("Missing bundled language file for locale: " + locale);
        }

        // Overlay the extracted, admin-editable file on disk, if present.
        File diskFile = languagesFolder == null ? null : new File(new File(languagesFolder, locale), "items.yml");
        if (diskFile != null && diskFile.isFile()) {
            InputStream diskIn = null;
            try {
                diskIn = new FileInputStream(diskFile);
                String fromDisk = parseLanguage(diskIn, target, loreTarget, listsTarget);
                if (fromDisk != null) {
                    template = fromDisk;
                }
            } catch (Exception e) {
                EpicExoticGarden.getInstance().getLogger().log(java.util.logging.Level.SEVERE,
                        "Failed to load disk language " + locale, e);
            } finally {
                if (diskIn != null) {
                    try {
                        diskIn.close();
                    } catch (Exception ignored) {
                        // best effort
                    }
                }
            }
        }
        return template;
    }

    /**
     * Reads one language source into {@code target} (existing entries are overlaid).
     * Item ids carry a {@code .name} sub-key; {@code _food_lore} and {@code _gui_*} meta keys
     * are plain scalars.
     */
    @Nullable
    private static String parseLanguage(@Nonnull InputStream in, @Nonnull Map<String, String> target,
                                        @Nonnull Map<String, List<String>> loreTarget,
                                        @Nonnull Map<String, List<String>> listsTarget)
            throws java.io.IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        String template = null;
        for (String id : yaml.getKeys(false)) {
            if (FOOD_LORE_KEY.equals(id)) {
                String raw = yaml.getString(id);
                if (raw != null) {
                    template = ChatColor.translateAlternateColorCodes('&', raw);
                }
            } else if (ESSENCE_LORE_KEY.equals(id)) {
                putColoredList(listsTarget, id, yaml.getStringList(id));
            } else if (id.startsWith(GUI_PREFIX) || id.startsWith(CMD_PREFIX) || id.startsWith(LOG_PREFIX)
                    || id.startsWith(FILE_PREFIX)) {
                String raw = yaml.getString(id);
                if (raw != null) {
                    target.put(id, ChatColor.translateAlternateColorCodes('&', raw));
                }
            } else {
                String name = yaml.getString(id + ".name");
                if (name != null) {
                    target.put(id, ChatColor.translateAlternateColorCodes('&', name));
                }
                List<String> loreLines = yaml.getStringList(id + ".lore");
                putColoredList(loreTarget, id, loreLines);
            }
        }
        return template;
    }

    /** Stores a color-translated copy of a string list under {@code key} when it is non-empty. */
    private static void putColoredList(@Nonnull Map<String, List<String>> target, @Nonnull String key,
                                       @Nonnull List<String> rawLines) {
        if (rawLines.isEmpty()) {
            return;
        }
        List<String> colored = new ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            colored.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        target.put(key, colored);
    }

    /**
     * Resolves a localized GUI text (title/category name). Configured language wins, then
     * English, then the hardcoded fallback.
     */
    @Nonnull
    public static String gui(@Nonnull String key, @Nonnull String fallback) {
        String text = PRIMARY.get(key);
        if (text == null) {
            text = ENGLISH.get(key);
        }
        return text != null ? text : fallback;
    }

    /**
     * Same as {@link #gui(String, String)} but substitutes {@code {0}}, {@code {1}}, ... with
     * the supplied arguments.
     */
    @Nonnull
    public static String gui(@Nonnull String key, @Nonnull String fallback, @Nonnull Object... args) {
        String text = gui(key, fallback);
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return text;
    }

    /**
     * Resolves a localized command-feedback message ({@code _cmd_*} keys). Same lookup order as
     * {@link #gui(String, String)}: configured language, then English, then the fallback.
     */
    @Nonnull
    public static String cmd(@Nonnull String key, @Nonnull String fallback) {
        return gui(key, fallback);
    }

    /**
     * Same as {@link #cmd(String, String)} but substitutes {@code {0}}, {@code {1}}, ... with
     * the supplied arguments.
     */
    @Nonnull
    public static String cmd(@Nonnull String key, @Nonnull String fallback, @Nonnull Object... args) {
        return gui(key, fallback, args);
    }

    /**
     * Resolves a localized console-log message ({@code _log_*} keys). Same lookup order as
     * {@link #gui(String, String)}. Values should be plain text without {@code &} color codes:
     * the server logger does not render them.
     */
    @Nonnull
    public static String log(@Nonnull String key, @Nonnull String fallback, @Nonnull Object... args) {
        return gui(key, fallback, args);
    }

    /**
     * Resolves a localized string written into generated config files ({@code _file_*} keys),
     * e.g. the comment headers of recipe/*.yml and models.yml. Values are plain text;
     * color codes are not supported there.
     */
    @Nonnull
    public static String file(@Nonnull String key, @Nonnull String fallback, @Nonnull Object... args) {
        return gui(key, fallback, args);
    }

    /**
     * Returns the shared lore lines applied to every magical essence ({@code _essence_lore}
     * template), or {@code null} when no source defines them. Lookup order: configured
     * language, then English.
     */
    @Nullable
    public static List<String> essenceLore() {
        List<String> lines = LISTS_PRIMARY.get(ESSENCE_LORE_KEY);
        return lines != null ? lines : LISTS_ENGLISH.get(ESSENCE_LORE_KEY);
    }

    /**
     * Returns the admin-defined extra lore lines for an item ({@code lore:} list under its
     * language entry), or {@code null} when the item defines none. Lookup order: configured
     * language, then English.
     */
    @Nullable
    public static List<String> itemLore(@Nonnull String id) {
        List<String> lines = LORE_PRIMARY.get(id);
        return lines != null ? lines : LORE_ENGLISH.get(id);
    }

    /**
     * Returns the localized GUI text for {@code key}, or {@code null} when neither the configured
     * language nor English defines it. Used for numbered key families (e.g. book lore lines)
     * where the caller stops at the first missing index.
     */
    @Nullable
    public static String guiOrNull(@Nonnull String key) {
        String text = PRIMARY.get(key);
        return text != null ? text : ENGLISH.get(key);
    }

    /**
     * Returns the localized, colored lore line describing how much hunger an edible restores.
     * {@code value} is Bukkit food points (0-20 scale); the displayed number is the number of
     * hunger shanks (drumstick icons), i.e. points / 2, so a 14-point drink shows "7". Odd
     * values render as x.5. The configured language wins, then English, then a built-in neutral
     * fallback.
     */
    @Nonnull
    public static String foodLore(int value) {
        String template = foodLorePrimary != null ? foodLorePrimary : foodLoreEnglish;
        if (template == null) {
            template = "&7Restores &b{0} &7Hunger";
        }
        String shanks = value % 2 == 0 ? String.valueOf(value / 2) : (value / 2) + ".5";
        return template.replace("{0}", shanks);
    }

    /**
     * Resolves the localized (colored) display name of an item id.
     */
    @Nonnull
    public static String name(@Nonnull String id) {
        String name = PRIMARY.get(id);
        if (name == null) {
            name = ENGLISH.get(id);
        }
        if (name == null) {
            name = humanize(id);
        }
        return name;
    }

    /**
     * Best-effort localized name for the given player locale, used by GUIs.
     */
    @Nonnull
    public static String name(@Nonnull String id, @Nullable String playerLocale) {
        // Physical items carry a single name (configured language); GUI lookup uses the same source
        // for consistency, as only the shipped language files are available.
        return name(id);
    }

    @Nonnull
    public static String humanize(@Nonnull String id) {
        String[] parts = id.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    @Nonnull
    public static String getLanguage() {
        return language;
    }
}
