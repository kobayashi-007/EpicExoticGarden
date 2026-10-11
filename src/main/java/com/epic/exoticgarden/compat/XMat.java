/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.compat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;

/**
 * Version-independent material reference.
 *
 * <p>Constants are resolved by <em>name</em> at class-load time, so referencing a modern constant
 * (e.g. {@code OAK_SAPLING}) never throws {@link NoSuchFieldError} on a 1.12 server where the
 * flattened Material enum does not contain it. On legacy servers the corresponding pre-flattening
 * name (and optional data value) is used instead.</p>
 *
 * <p>This is the standalone replacement for Slimefun's bundled XMaterial; the core never
 * references modern Material enum constants directly.</p>
 */
public final class XMat {

    /** Whether the running server uses pre-flattening (1.12.x) material names. */
    public static final boolean LEGACY = Material.getMaterial("OAK_SAPLING") == null;

    @Nonnull
    public final Material material;
    /** Legacy data/durability value (pre-1.13), always {@code 0} on modern servers. */
    public final byte data;

    private XMat(@Nonnull Material material, int data) {
        this.material = material;
        this.data = (byte) data;
    }

    /**
     * Resolves a material which has the same enum name on all supported versions.
     */
    @Nonnull
    public static XMat of(@Nonnull String modern) {
        return of(modern, modern, 0);
    }

    /**
     * Resolves a material, falling back to a legacy name/data on 1.12 servers.
     */
    @Nonnull
    public static XMat of(@Nonnull String modern, @Nonnull String legacy, int legacyData) {
        Material m = Material.getMaterial(modern);
        if (m != null) {
            return new XMat(m, 0);
        }

        m = Material.getMaterial(legacy);
        return new XMat(m != null ? m : Material.STONE, legacyData);
    }

    /**
     * Returns {@code null} when neither the modern nor the legacy name exists on this server.
     * Used for content introduced after the 1.12 floor (sweet berries, copper, netherite, ...).
     */
    @Nullable
    public static XMat optional(@Nonnull String modern, @Nullable String legacy, int legacyData) {
        Material m = Material.getMaterial(modern);
        if (m != null) {
            return new XMat(m, 0);
        }

        if (legacy != null) {
            m = Material.getMaterial(legacy);
        }

        return m != null ? new XMat(m, (byte) legacyData) : null;
    }

    @Nullable
    public static XMat optional(@Nonnull String modern) {
        return optional(modern, modern, 0);
    }

    @Override
    public String toString() {
        return "XMat{" + material.name() + (LEGACY && data != 0 ? ":" + data : "") + "}";
    }
}
