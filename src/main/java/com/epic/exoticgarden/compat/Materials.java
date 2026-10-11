/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * Central registry of all {@link XMat} constants used by EpicExoticGarden.
 *
 * <p>Replaces the Slimefun-bundled XMaterial constants. Every material the plugin touches is
 * declared here once so the 1.12 &harr; modern name mapping lives in a single place.</p>
 */
public final class Materials {

    private Materials() {}

    // ---- Terrain / plants ----
    public static final XMat AIR = XMat.of("AIR");
    public static final XMat CAVE_AIR = XMat.of("CAVE_AIR", "AIR", 0);
    public static final XMat SNOW = XMat.of("SNOW");
    public static final XMat DIRT = XMat.of("DIRT");
    public static final XMat GRASS_BLOCK = XMat.of("GRASS_BLOCK", "GRASS", 0);
    public static final XMat SHORT_GRASS = XMat.of("SHORT_GRASS", "LONG_GRASS", 1);
    public static final XMat WATER = XMat.of("WATER");
    public static final XMat SEAGRASS = XMat.optional("SEAGRASS");
    public static final XMat TALL_SEAGRASS = XMat.optional("TALL_SEAGRASS");
    public static final XMat SAND = XMat.of("SAND");
    public static final XMat ICE = XMat.of("ICE");
    public static final XMat OAK_SAPLING = XMat.of("OAK_SAPLING", "SAPLING", 0);
    public static final XMat SPRUCE_SAPLING = XMat.of("SPRUCE_SAPLING", "SAPLING", 1);
    public static final XMat BIRCH_SAPLING = XMat.of("BIRCH_SAPLING", "SAPLING", 2);
    public static final XMat JUNGLE_SAPLING = XMat.of("JUNGLE_SAPLING", "SAPLING", 3);
    public static final XMat ACACIA_SAPLING = XMat.of("ACACIA_SAPLING", "SAPLING", 4);
    public static final XMat DARK_OAK_SAPLING = XMat.of("DARK_OAK_SAPLING", "SAPLING", 5);
    public static final XMat OAK_LEAVES = XMat.of("OAK_LEAVES", "LEAVES", 0);
    public static final XMat SPRUCE_LEAVES = XMat.of("SPRUCE_LEAVES", "LEAVES", 1);
    public static final XMat BIRCH_LEAVES = XMat.of("BIRCH_LEAVES", "LEAVES", 2);
    public static final XMat JUNGLE_LEAVES = XMat.of("JUNGLE_LEAVES", "LEAVES", 3);
    public static final XMat ACACIA_LEAVES = XMat.of("ACACIA_LEAVES", "LEAVES_2", 0);
    public static final XMat DARK_OAK_LEAVES = XMat.of("DARK_OAK_LEAVES", "LEAVES_2", 1);
    public static final XMat OAK_LOG = XMat.of("OAK_LOG", "LOG", 0);
    public static final XMat SPRUCE_LOG = XMat.of("SPRUCE_LOG", "LOG", 1);
    public static final XMat BIRCH_LOG = XMat.of("BIRCH_LOG", "LOG", 2);
    public static final XMat JUNGLE_LOG = XMat.of("JUNGLE_LOG", "LOG", 3);
    public static final XMat ACACIA_LOG = XMat.of("ACACIA_LOG", "LOG_2", 0);
    public static final XMat DARK_OAK_LOG = XMat.of("DARK_OAK_LOG", "LOG_2", 1);
    public static final XMat PLAYER_HEAD = XMat.of("PLAYER_HEAD", "SKULL_ITEM", 3);

    // ---- Vanilla ingredients ----
    public static final XMat STICK = XMat.of("STICK");
    public static final XMat WHEAT_SEEDS = XMat.of("WHEAT_SEEDS", "SEEDS", 0);
    public static final XMat PUMPKIN_SEEDS = XMat.of("PUMPKIN_SEEDS");
    public static final XMat MELON_SEEDS = XMat.of("MELON_SEEDS");
    public static final XMat EGG = XMat.of("EGG");
    public static final XMat SUGAR = XMat.of("SUGAR");
    public static final XMat MILK_BUCKET = XMat.of("MILK_BUCKET");
    public static final XMat WATER_BUCKET = XMat.of("WATER_BUCKET");
    public static final XMat BREAD = XMat.of("BREAD");
    public static final XMat WHEAT = XMat.of("WHEAT");
    public static final XMat BEETROOT_SEEDS = XMat.of("BEETROOT_SEEDS");
    public static final XMat BEETROOT = XMat.of("BEETROOT");
    public static final XMat SUGAR_CANE = XMat.of("SUGAR_CANE", "REEDS", 0);
    public static final XMat BAKED_POTATO = XMat.of("BAKED_POTATO");
    public static final XMat POTATO = XMat.of("POTATO");
    public static final XMat CARROT = XMat.of("CARROT");
    public static final XMat BOWL = XMat.of("BOWL");
    public static final XMat COOKED_CHICKEN = XMat.of("COOKED_CHICKEN");
    public static final XMat COOKED_COD = XMat.of("COOKED_COD", "COOKED_FISH", 0);
    public static final XMat COOKED_BEEF = XMat.of("COOKED_BEEF");
    public static final XMat COOKED_PORKCHOP = XMat.of("COOKED_PORKCHOP", "GRILLED_PORK", 0);
    public static final XMat PUMPKIN = XMat.of("PUMPKIN");
    public static final XMat COCOA_BEANS = XMat.of("COCOA_BEANS", "INK_SACK", 3);
    public static final XMat GOLDEN_CARROT = XMat.of("GOLDEN_CARROT");
    public static final XMat SWEET_BERRIES = XMat.optional("SWEET_BERRIES");
    // Standalone pantry lookalikes (formerly Slimefun items): white dust / yellow lump.
    public static final XMat WHITE_DYE = XMat.of("WHITE_DYE", "INK_SACK", 15);
    public static final XMat HONEYCOMB = XMat.optional("HONEYCOMB", "GOLD_NUGGET", 0);
    public static final XMat SPONGE = XMat.of("SPONGE");

    // ---- Ores / resources (magical crops) ----
    public static final XMat COAL = XMat.of("COAL");
    public static final XMat COAL_ORE = XMat.of("COAL_ORE");
    public static final XMat IRON_INGOT = XMat.of("IRON_INGOT");
    public static final XMat IRON_BLOCK = XMat.of("IRON_BLOCK");
    public static final XMat GOLD_INGOT = XMat.of("GOLD_INGOT");
    public static final XMat GOLD_BLOCK = XMat.of("GOLD_BLOCK");
    public static final XMat COPPER_INGOT = XMat.optional("COPPER_INGOT");
    public static final XMat REDSTONE = XMat.of("REDSTONE");
    public static final XMat REDSTONE_BLOCK = XMat.of("REDSTONE_BLOCK");
    public static final XMat LAPIS_LAZULI = XMat.of("LAPIS_LAZULI", "INK_SACK", 4);
    public static final XMat LAPIS_ORE = XMat.of("LAPIS_ORE");
    public static final XMat ENDER_PEARL = XMat.of("ENDER_PEARL");
    public static final XMat QUARTZ = XMat.of("QUARTZ");
    public static final XMat NETHER_QUARTZ_ORE = XMat.of("NETHER_QUARTZ_ORE", "QUARTZ_ORE", 0);
    public static final XMat DIAMOND = XMat.of("DIAMOND");
    public static final XMat EMERALD = XMat.of("EMERALD");
    public static final XMat NETHERITE_INGOT = XMat.optional("NETHERITE_INGOT");
    public static final XMat NETHERITE_BLOCK = XMat.optional("NETHERITE_BLOCK");
    public static final XMat GLOWSTONE_DUST = XMat.of("GLOWSTONE_DUST");
    public static final XMat GLOWSTONE = XMat.of("GLOWSTONE");
    public static final XMat OBSIDIAN = XMat.of("OBSIDIAN");
    public static final XMat SLIME_BALL = XMat.of("SLIME_BALL");
    public static final XMat BLAZE_POWDER = XMat.of("BLAZE_POWDER");

    // ---- GUI ----
    public static final XMat ARROW = XMat.of("ARROW");
    public static final XMat BOW = XMat.of("BOW");
    public static final XMat FEATHER = XMat.of("FEATHER");
    public static final XMat BOOK = XMat.of("BOOK");
    public static final XMat BARRIER = XMat.of("BARRIER");
    public static final XMat BLACK_STAINED_GLASS_PANE = XMat.of("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 15);
    public static final XMat GRAY_STAINED_GLASS_PANE = XMat.of("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 7);

    // ---- Tools / blocks ----
    public static final XMat WOODEN_HOE = XMat.of("WOODEN_HOE", "WOOD_HOE", 0);
    public static final XMat STONE_HOE = XMat.of("STONE_HOE", "STONE_HOE", 0);
    public static final XMat POTION = XMat.of("POTION");
    public static final XMat CAULDRON = XMat.of("CAULDRON");
    public static final XMat FURNACE = XMat.of("FURNACE");
    public static final XMat DISPENSER = XMat.of("DISPENSER");
    public static final XMat CRAFTING_TABLE = XMat.of("CRAFTING_TABLE", "WORKBENCH", 0);
    public static final XMat BOOKSHELF = XMat.of("BOOKSHELF");
    public static final XMat IRON_TRAPDOOR = XMat.of("IRON_TRAPDOOR");
    public static final XMat STONE_PRESSURE_PLATE = XMat.of("STONE_PRESSURE_PLATE", "STONE_PLATE", 0);
    public static final XMat BRICKS = XMat.of("BRICKS", "BRICK", 0);
    public static final XMat BRICK_STAIRS = XMat.of("BRICK_STAIRS");

    // ---- Token resolution for the editable recipe files ----

    /** Modern constant name -> XMat, built from the declared constants above. */
    private static final Map<String, XMat> BY_MODERN = new HashMap<>();
    /** Runtime material name + legacy data -> modern constant name (for writing recipe files). */
    private static final Map<String, String> RUNTIME_TO_MODERN = new HashMap<>();

    static {
        for (Field field : Materials.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != XMat.class) {
                continue;
            }
            try {
                XMat xmat = (XMat) field.get(null);
                if (xmat == null) {
                    continue;
                }
                BY_MODERN.put(field.getName(), xmat);
                RUNTIME_TO_MODERN.put(xmat.material.name() + ":" + (xmat.data & 0xFF), field.getName());
            } catch (IllegalAccessException ignored) {
                // Public constants are always accessible; nothing to do.
            }
        }
    }

    /**
     * Resolves a vanilla material token (modern name preferred, legacy name fallback) used in the
     * recipe YAML files, or {@code null} when the material does not exist on this server version.
     */
    @Nullable
    public static XMat byModernName(@Nonnull String name) {
        XMat known = BY_MODERN.get(name);
        if (known != null) {
            return known;
        }
        Material direct = Material.getMaterial(name);
        return direct == null || direct == Material.AIR ? null : XMat.of(name);
    }

    /**
     * Returns the stable modern token for a runtime stack material, mapping legacy data values
     * back to the declared constant name. Used when recipe files are generated.
     */
    @Nonnull
    public static String modernToken(@Nonnull Material material, short data) {
        String token = RUNTIME_TO_MODERN.get(material.name() + ":" + (data & 0xFF));
        return token != null ? token : material.name();
    }

    /**
     * Builds a single {@link ItemStack} for the given reference, applying the legacy
     * data/durability value on pre-flattening servers.
     */
    @Nonnull
    public static ItemStack stack(@Nonnull XMat m) {
        if (XMat.LEGACY && m.data != 0) {
            return new ItemStack(m.material, 1, (short) m.data);
        }
        return new ItemStack(m.material);
    }

    /**
     * Sets a block's type, applying legacy block data where required.
     */
    public static void setBlock(@Nonnull Block block, @Nonnull XMat m) {
        block.setType(m.material);
        if (XMat.LEGACY && m.data != 0) {
            setData(block, m.data);
        }
    }

    /**
     * Applies legacy block data reflectively. {@code Block#setData(byte)} was removed from modern
     * APIs, so direct calls would fail to compile; on 1.12 the method still exists at runtime.
     */
    public static void setData(@Nonnull Block block, byte data) {
        try {
            Block.class.getMethod("setData", byte.class).invoke(block, data);
        } catch (Throwable ignored) {
            // Modern server: variant data lives in BlockData and XMat#data is always 0.
        }
    }
}
