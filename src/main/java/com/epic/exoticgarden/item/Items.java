/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.epic.exoticgarden.compat.ItemTags;
import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.SkullUtil;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.core.CustomModels;
import com.epic.exoticgarden.core.Lang;

/**
 * The global item registry.
 *
 * <p>This is the standalone replacement for Slimefun's item registry ({@code SlimefunItem.getById},
 * {@code SlimefunItemStack}, ...). Every EpicExoticGarden item is created here once, stamped with
 * its unique id and localized display name, then cloned on demand.</p>
 */
public final class Items {

    private Items() {}

    private static final Map<String, GardenItem> REGISTRY = new LinkedHashMap<>();

    /**
     * ItemFlag hiding the potion-effect tooltip line on drinks (the hunger lore already says
     * what a drink restores). Renamed to HIDE_ADDITIONAL_TOOLTIP in 1.20.5.
     */
    @Nullable
    private static final ItemFlag HIDE_EFFECTS_FLAG = hideEffectsFlag();

    @Nullable
    private static ItemFlag hideEffectsFlag() {
        for (String name : new String[] {"HIDE_ADDITIONAL_TOOLTIP", "HIDE_POTION_EFFECTS"}) {
            try {
                return ItemFlag.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // Not present on this server version.
            }
        }
        return null;
    }

    // ---- Factory methods used by content registration ----

    /**
     * Registers a plain-material custom item.
     */
    @Nonnull
    public static GardenItem register(@Nonnull String id, @Nonnull Category category, @Nonnull XMat material) {
        return register(id, category, Materials.stack(material), false, 0, false);
    }

    /**
     * Registers a custom item from an explicitly built stack (intermediate ingredients, tools, ...).
     */
    @Nonnull
    public static GardenItem register(@Nonnull String id, @Nonnull Category category, @Nonnull ItemStack stack,
                                      boolean placeable) {
        return register(id, category, stack, false, 0, placeable);
    }

    /**
     * Registers a player-head item with a textures.minecraft.net skin hash.
     */
    @Nonnull
    public static GardenItem registerHead(@Nonnull String id, @Nonnull Category category, @Nonnull String texture) {
        return register(id, category, SkullUtil.head(texture), false, 0, false);
    }

    /**
     * Registers an edible player-head food with a hunger-restoration value.
     */
    @Nonnull
    public static GardenItem registerFood(@Nonnull String id, @Nonnull Category category,
                                          @Nonnull String texture, int foodValue) {
        return register(id, category, SkullUtil.head(texture), true, foodValue, false);
    }

    /**
     * Registers an edible food based on a vanilla material.
     */
    @Nonnull
    public static GardenItem registerFood(@Nonnull String id, @Nonnull Category category,
                                          @Nonnull XMat material, int foodValue) {
        return register(id, category, Materials.stack(material), true, foodValue, false);
    }

    /**
     * Registers a placeable plant block (sapling/bush lookalike).
     */
    @Nonnull
    public static GardenItem registerPlantable(@Nonnull String id, @Nonnull Category category) {
        return register(id, category, Materials.stack(Materials.OAK_SAPLING), false, 0, true);
    }

    /**
     * Registers a colored potion drink (juice/smoothie/tea) carrying a potion effect.
     * Drinks are consumed through the vanilla potion-drinking mechanic.
     *
     * <p>Vanilla Saturation is an <em>instant</em> effect: on drink it applies once as
     * {@code eat(amplifier + 1, 1.0)} and the duration is ignored entirely. The potency is
     * therefore encoded in the amplifier (amplifier = food points - 1), never in the duration.</p>
     */
    @Nonnull
    public static GardenItem registerDrink(@Nonnull String id, @Nonnull Category category,
                                           @Nonnull Color color, @Nonnull PotionEffect effect) {
        ItemStack stack = Materials.stack(Materials.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.setDisplayName(Lang.name(id));
        // PotionMeta#setColor was added in 1.11; absent on older APIs.
        try {
            meta.setColor(color);
        } catch (NoSuchMethodError ignored) {
            // No custom flask color on this version.
        }
        meta.addCustomEffect(effect, true);
        // Hide the "Saturation VI" tooltip line; the generated hunger lore communicates it.
        if (HIDE_EFFECTS_FLAG != null) {
            meta.addItemFlags(HIDE_EFFECTS_FLAG);
        }
        stack.setItemMeta(meta);
        // drinkFood = duration * (amplifier + 1); callers use duration 1 so it equals the food points.
        int drinkFood = effect.getType().equals(PotionEffectType.SATURATION)
                ? Math.max(0, effect.getDuration() * (effect.getAmplifier() + 1))
                : 0;
        return register(id, category, stack, false, drinkFood, false);
    }

    @Nonnull
    public static GardenItem register(@Nonnull String id, @Nonnull Category category, @Nonnull ItemStack stack,
                                      boolean edible, int foodValue, boolean placeable) {
        CustomModels.ensureKnown(id);
        applyPresentation(stack, id, edible, foodValue);
        ItemTags.setId(stack, id);

        GardenItem item = new GardenItem(id, category, stack, edible, foodValue, placeable);
        REGISTRY.put(id, item);
        return item;
    }

    /**
     * Stamps the localized display name and - for edible items - the auto-generated hunger lore.
     * Must run before {@link ItemTags#setId}, so on 1.12/1.13 the legacy id marker stays the final
     * lore line.
     */
    private static void applyPresentation(@Nonnull ItemStack stack, @Nonnull String id,
                                          boolean edible, int foodValue) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.hasDisplayName()) {
            meta.setDisplayName(Lang.name(id));
        }
        if (foodValue >= 1) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(Lang.foodLore(foodValue));
            meta.setLore(lore);
        }
        appendItemLore(meta, id);
        if (id.endsWith("_ESSENCE")) {
            appendSharedLore(meta, Lang.essenceLore());
        }
        CustomModels.applyTo(meta, id);
        stack.setItemMeta(meta);
    }

    /** Appends the language-defined extra lore lines ({@code lore:} list) for an item, if any. */
    private static void appendItemLore(@Nonnull ItemMeta meta, @Nonnull String id) {
        appendSharedLore(meta, Lang.itemLore(id));
    }

    /** Appends a shared lore template (e.g. essence lore), inserting a blank line when lore exists. */
    private static void appendSharedLore(@Nonnull ItemMeta meta, @Nullable List<String> extra) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        if (!lore.isEmpty()) {
            lore.add("");
        }
        lore.addAll(extra);
        meta.setLore(lore);
    }

    // ---- Lookups ----

    @Nullable
    public static GardenItem getDef(@Nullable String id) {
        return id == null ? null : REGISTRY.get(id);
    }

    public static boolean exists(@Nullable String id) {
        return id != null && REGISTRY.containsKey(id);
    }

    /**
     * Returns a fresh item stack for the id, or {@code null} if it is not registered.
     */
    @Nullable
    public static ItemStack get(@Nullable String id) {
        GardenItem item = getDef(id);
        return item == null ? null : item.create();
    }

    /**
     * Identifies the given stack and returns a fresh canonical stack, or {@code null} when the
     * stack is not an EpicExoticGarden item.
     */
    @Nullable
    public static ItemStack canonical(@Nullable ItemStack stack) {
        return get(ItemTags.getId(stack));
    }

    @Nonnull
    public static Collection<GardenItem> all() {
        return new ArrayList<>(REGISTRY.values());
    }

    @Nonnull
    public static List<GardenItem> byCategory(@Nonnull Category category) {
        List<GardenItem> items = new ArrayList<>();
        for (GardenItem item : REGISTRY.values()) {
            if (item.getCategory() == category) {
                items.add(item);
            }
        }
        return items;
    }

    public static int count() {
        return REGISTRY.size();
    }

    /**
     * Applies hunger values read from the editable recipe files, then re-stamps prototypes so
     * the food lore reflects the new values. Values for unknown ids, tools and plants are ignored.
     * Drinks (non-edible potions) are supported too: their Saturation effect amplifier is synced
     * to the new value, since instant Saturation restores (amplifier + 1) hunger points.
     */
    public static void applyHunger(@Nonnull Map<String, Integer> hunger) {
        for (Map.Entry<String, Integer> entry : hunger.entrySet()) {
            GardenItem item = REGISTRY.get(entry.getKey());
            if (item == null) {
                continue;
            }
            int value = Math.max(0, entry.getValue());
            if (item.isEdible()) {
                item.setFoodValue(value);
            } else if (value >= 1 && item.getFoodValue() >= 1) {
                // A non-edible item carrying a food value is a drink.
                item.setFoodValue(value);
                syncDrinkEffect(item, value);
            }
        }
        restampPresentation();
    }

    /**
     * Rewrites the drink prototype's Saturation effect so the configured hunger value is what
     * drinking restores. Saturation is instant (duration ignored), so the value is encoded as
     * amplifier = value - 1. Color and other effects are preserved.
     */
    private static void syncDrinkEffect(@Nonnull GardenItem item, int value) {
        ItemStack stack = item.getPrototype();
        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof PotionMeta)) {
            return;
        }
        PotionMeta potion = (PotionMeta) meta;
        List<PotionEffect> effects = new ArrayList<>(potion.getCustomEffects());
        boolean changed = false;
        for (PotionEffect effect : effects) {
            if (effect.getType().equals(PotionEffectType.SATURATION)) {
                potion.removeCustomEffect(PotionEffectType.SATURATION);
                potion.addCustomEffect(new PotionEffect(PotionEffectType.SATURATION,
                        1, Math.max(0, value - 1)), true);
                changed = true;
            }
        }
        if (changed) {
            stack.setItemMeta(potion);
        }
    }

    /**
     * Re-stamps every registered prototype with its localized display name after a language
     * reload. The unique-id marker lives in the PDC/lore and is left untouched, so identity is
     * preserved. Items already held by players keep their old name until re-obtained.
     */
    public static void applyLanguageNames() {
        restampPresentation();
    }

    /**
     * Rebuilds display name and generated food lore on every prototype from current Lang/food data.
     */
    private static void restampPresentation() {
        for (GardenItem item : REGISTRY.values()) {
            ItemStack stack = item.getPrototype();
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                continue;
            }
            meta.setDisplayName(Lang.name(item.getId()));

            List<String> lore = new ArrayList<>();
            if (item.getFoodValue() >= 1) {
                lore.add("");
                lore.add(Lang.foodLore(item.getFoodValue()));
            }
            List<String> extra = Lang.itemLore(item.getId());
            if (extra != null && !extra.isEmpty()) {
                if (!lore.isEmpty()) {
                    lore.add("");
                }
                lore.addAll(extra);
            }
            if (item.getId().endsWith("_ESSENCE")) {
                List<String> essenceLines = Lang.essenceLore();
                if (essenceLines != null && !essenceLines.isEmpty()) {
                    if (!lore.isEmpty()) {
                        lore.add("");
                    }
                    lore.addAll(essenceLines);
                }
            }
            meta.setLore(lore.isEmpty() ? null : lore);
            if (!CustomModels.applyTo(meta, item.getId())) {
                CustomModels.clearFrom(meta, item.getId());
            }
            stack.setItemMeta(meta);

            // Re-append the legacy 1.12/1.13 id marker (no-op for PDC-based versions).
            ItemTags.setId(stack, item.getId());
        }
    }
}
