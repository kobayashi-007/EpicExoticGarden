/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.core;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.item.Category;
import com.epic.exoticgarden.item.GardenItem;
import com.epic.exoticgarden.item.Items;

/**
 * Admin-editable CustomModelData overrides ({@code plugins/EpicExoticGarden/models.yml}).
 *
 * <p>Every registered item id gets an entry; a value greater than 0 is stamped onto the item's
 * {@code ItemMeta} so a resource pack can swap the model via an {@code overrides} predicate.
 * This is a pure presentation map with one entry per item, so it lives in its own file instead
 * of config.yml.</p>
 *
 * <p>{@code ItemMeta#setCustomModelData(Integer)} exists since 1.14; on older servers the file
 * is still read but the values are never applied (a warning is logged when overrides exist).</p>
 */
public final class CustomModels {

    private CustomModels() {}

    /** Whether this server API supports {@code ItemMeta#setCustomModelData} (1.14+). */
    public static final boolean SUPPORTED;

    private static Method setCustomModelData;

    /** id -> configured model data (only values > 0). */
    private static final Map<String, Integer> OVERRIDES = new LinkedHashMap<>();
    /** Top-level keys the file already carried when it was read. */
    private static final List<String> fileIds = new ArrayList<>();
    /** Registered ids missing from the file; appended on {@link #flush}. */
    private static final List<String> missing = new ArrayList<>();
    private static boolean filePresent;

    static {
        Method method = null;
        boolean supported = false;
        try {
            method = ItemMeta.class.getMethod("setCustomModelData", Integer.class);
            supported = true;
        } catch (Throwable ignored) {
            // 1.12/1.13: no custom model data.
        }
        setCustomModelData = method;
        SUPPORTED = supported;
    }

    /**
     * Reads models.yml if it exists. Must run before content registration so prototypes are
     * stamped with model data as they are created, and so newly added item ids can be detected
     * for {@link #flush}.
     */
    public static void init(@Nonnull EpicExoticGarden plugin) {
        OVERRIDES.clear();
        fileIds.clear();
        missing.clear();

        File file = file(plugin);
        filePresent = file.isFile();
        if (filePresent) {
            read(file);
        }
        logLoaded(plugin);
    }

    /** Reloads the overrides from disk (runtime {@code /eeg reload}). */
    public static void reload(@Nonnull EpicExoticGarden plugin) {
        OVERRIDES.clear();
        fileIds.clear();

        File file = file(plugin);
        filePresent = file.isFile();
        if (filePresent) {
            read(file);
        }
        logLoaded(plugin);
    }

    /** Marks an item id during registration; ids absent from the file are appended on flush. */
    public static void ensureKnown(@Nonnull String id) {
        if (!fileIds.contains(id) && !missing.contains(id)) {
            missing.add(id);
        }
    }

    /**
     * Writes the file after content registration: a full file grouped by category on first
     * start, or an appended section with just the new ids when the admin's file predates a
     * plugin update. Existing entries and comments are never rewritten.
     */
    public static void flush(@Nonnull EpicExoticGarden plugin) {
        for (String id : OVERRIDES.keySet()) {
            if (!Items.exists(id)) {
                plugin.getLogger().warning(Lang.log("_log_models_unknown_item", "Unknown item ID in models.yml, ignored: {0}", id));
            }
        }

        if (!filePresent) {
            writeFull(plugin, file(plugin));
            filePresent = true;
            fileIds.addAll(missing);
            missing.clear();
            return;
        }
        if (!missing.isEmpty()) {
            appendMissing(plugin, file(plugin));
            fileIds.addAll(missing);
            missing.clear();
        }
    }

    /** Number of active overrides (entries with a value greater than 0). */
    public static int count() {
        return OVERRIDES.size();
    }

    /** The configured model data for an item id, or {@code null} when unset. */
    @Nullable
    public static Integer dataFor(@Nonnull String id) {
        return OVERRIDES.get(id);
    }

    /**
     * Applies the configured model data to the meta. Returns whether a value was applied.
     * No-op on servers without CustomModelData support (pre-1.14).
     */
    public static boolean applyTo(@Nonnull ItemMeta meta, @Nonnull String id) {
        if (!SUPPORTED) {
            return false;
        }
        Integer data = OVERRIDES.get(id);
        if (data == null) {
            return false;
        }
        try {
            setCustomModelData.invoke(meta, data);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Removes any CustomModelData on this meta when the id is no longer configured.
     * No-op when the server API does not support the feature.
     */
    public static void clearFrom(@Nonnull ItemMeta meta, @Nonnull String id) {
        if (!SUPPORTED || OVERRIDES.containsKey(id)) {
            return;
        }
        try {
            setCustomModelData.invoke(meta, (Integer) null);
        } catch (Throwable ignored) {
            // Some early 1.14 builds may not accept null.
        }
    }

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    @Nonnull
    private static File file(@Nonnull EpicExoticGarden plugin) {
        return new File(plugin.getDataFolder(), "models.yml");
    }

    private static void read(@Nonnull File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
            for (String key : yaml.getKeys(false)) {
                fileIds.add(key);
                int value = yaml.getInt(key, 0);
                if (value > 0) {
                    OVERRIDES.put(key, value);
                }
            }
        } catch (Exception e) {
            EpicExoticGarden.getInstance().getLogger()
                    .log(java.util.logging.Level.WARNING,
                            Lang.log("_log_models_read_failed", "Failed to read custom model config: {0}", file.getName()), e);
        }
    }

    private static void logLoaded(@Nonnull EpicExoticGarden plugin) {
        if (OVERRIDES.isEmpty()) {
            return;
        }
        if (!SUPPORTED) {
            plugin.getLogger().warning(Lang.log("_log_models_unsupported",
                    "Detected {0} CustomModelData entries, but the server is below 1.14; they will not be applied.",
                    OVERRIDES.size()));
        } else {
            plugin.getLogger().info(Lang.log("_log_models_loaded",
                    "Loaded {0} custom model (CustomModelData) entries", OVERRIDES.size()));
        }
    }

    // ------------------------------------------------------------------
    // Writing
    // ------------------------------------------------------------------

    private static void writeFull(@Nonnull EpicExoticGarden plugin, @Nonnull File file) {
        StringBuilder builder = new StringBuilder();
        builder.append(header());
        for (Category category : Category.values()) {
            List<GardenItem> items = Items.byCategory(category);
            if (items.isEmpty()) {
                continue;
            }
            builder.append("# =====================================================================\n");
            builder.append("# ").append(categoryLabel(category)).append('\n');
            builder.append("# =====================================================================\n");
            for (GardenItem item : items) {
                appendEntry(builder, item.getId(), item.getPrototype().getType().name());
            }
            builder.append('\n');
        }
        writeText(plugin, file, builder.toString());
        plugin.getLogger().info(Lang.log("_log_models_generated",
                "Generated custom model config: models.yml ({0} items)", Items.count()));
    }

    /** Appends entries introduced by a plugin update to the admin's existing file. */
    private static void appendMissing(@Nonnull EpicExoticGarden plugin, @Nonnull File file) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
                builder.append('\n');
            }
            builder.append("\n# =====================================================================\n");
            builder.append("# " + Lang.file("_file_models_appended_header", "New items (appended automatically after a plugin update)") + "\n");
            builder.append("# =====================================================================\n");
            for (String id : missing) {
                GardenItem item = Items.getDef(id);
                appendEntry(builder, id, item != null ? item.getPrototype().getType().name() : "UNKNOWN");
            }
            writeText(plugin, file, builder.toString());
            plugin.getLogger().info(Lang.log("_log_models_appended", "Appended {0} new item entries to models.yml", missing.size()));
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    Lang.log("_log_models_append_failed", "Failed to append custom model entries: {0}", "models.yml"), e);
        }
    }

    private static void appendEntry(@Nonnull StringBuilder builder, @Nonnull String id,
                                    @Nonnull String material) {
        builder.append(id).append(": 0  # ").append(Lang.file("_file_models_material", "Material"))
                .append(": ").append(material).append('\n');
    }

    @Nonnull
    private static String header() {
        return Lang.file("_file_models_header",
                "# =====================================================================\n"
                        + "# EpicExoticGarden CustomModelData configuration\n"
                        + "#\n"
                        + "# Assign a CustomModelData value to an item so a resource pack can show custom art:\n"
                        + "#   ITEM_ID: 10001   -> applies CustomModelData = 10001\n"
                        + "#   ITEM_ID: 0       -> unchanged (default, vanilla appearance)\n"
                        + "#\n"
                        + "# Notes:\n"
                        + "#   - Requires server version 1.14+; older versions ignore this file\n"
                        + "#   - The \"Material\" comment after each entry is the item's vanilla base material;\n"
                        + "#     target it with overrides in your resource pack\n"
                        + "#   - Head items (berries/fruits etc.) get their item-form art from the skin;\n"
                        + "#     CustomModelData generally only affects their placed block form\n"
                        + "#   - Run /eeg reload to apply changes; items already in inventories stay unchanged\n"
                        + "# =====================================================================")
                + "\n\n";
    }

    @Nonnull
    private static String categoryLabel(@Nonnull Category category) {
        switch (category) {
            case PLANTS_AND_FRUITS:
                return Lang.file("_file_models_cat_plants_and_fruits", "Plants & Fruits");
            case MISC:
                return Lang.file("_file_models_cat_misc", "Ingredients & Tools");
            case FOOD:
                return Lang.file("_file_models_cat_food", "Dishes");
            case DRINKS:
                return Lang.file("_file_models_cat_drinks", "Drinks");
            case MAGICAL:
                return Lang.file("_file_models_cat_magical", "Magical Crops");
            default:
                return category.name();
        }
    }

    private static void writeText(@Nonnull EpicExoticGarden plugin, @Nonnull File file, @Nonnull String text) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().warning(Lang.log("_log_dir_create_failed", "Failed to create directory: {0}", parent.getPath()));
                return;
            }
            try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                writer.write(text);
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    Lang.log("_log_models_write_failed", "Failed to write custom model config: {0}", file.getName()), e);
        }
    }
}
