/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.compat;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Material;

/**
 * Material groupings that Slimefun's XSeries {@code Tag} used to provide, resolved by
 * material name so they work with every wood type on every version (1.12 &harr; modern).
 */
public final class BlockTags {

    private BlockTags() {}

    private static final Set<String> UNBREAKABLE = new HashSet<>();

    static {
        UNBREAKABLE.add("BEDROCK");
        UNBREAKABLE.add("BARRIER");
        UNBREAKABLE.add("COMMAND");
        UNBREAKABLE.add("COMMAND_CHAIN");
        UNBREAKABLE.add("COMMAND_REPEATING");
        UNBREAKABLE.add("STRUCTURE_BLOCK");
        UNBREAKABLE.add("JIGSAW");
        UNBREAKABLE.add("END_PORTAL");
        UNBREAKABLE.add("END_PORTAL_FRAME");
        UNBREAKABLE.add("END_GATEWAY");
        UNBREAKABLE.add("MOVING_PISTON");
        UNBREAKABLE.add("REINFORCED_DEEPSLATE");
        // 1.12 names
        UNBREAKABLE.add("COMMAND");
        UNBREAKABLE.add("COMMAND_CHAIN");
        UNBREAKABLE.add("COMMAND_REPEATING");
    }

    public static boolean isSapling(@Nonnull Material material) {
        String name = material.name();
        if ("SAPLING".equals(name)) {
            return true; // legacy: all six saplings share one Material
        }
        return name.endsWith("_SAPLING");
    }

    public static boolean isLeaves(@Nonnull Material material) {
        String name = material.name();
        if ("LEAVES".equals(name) || "LEAVES_2".equals(name)) {
            return true; // legacy
        }
        return name.endsWith("_LEAVES");
    }

    public static boolean isUnbreakable(@Nonnull Material material) {
        return UNBREAKABLE.contains(material.name());
    }
}
