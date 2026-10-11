/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.compat;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.epic.exoticgarden.EpicExoticGarden;

/**
 * Stores and reads the EpicExoticGarden item id directly on tile-entity blocks (the ripe fruit
 * heads), via the block state's {@code PersistentDataContainer}.
 *
 * <p>The coordinate-based {@link com.epic.exoticgarden.core.Blocks} database can drift from the
 * world whenever a fruit head is moved independently of the plugin (pistons, external world
 * editors, a failed database write). Stamping the id onto the head itself makes the block
 * self-identifying: {@code Blocks.idAt} falls back to this tag and re-populates its cache, so the
 * head "heals" itself instead of turning into a dead, texture-only skull. Block-state PDC exists
 * on 1.16+; on older servers every call is a no-op and the database remains the sole source.</p>
 */
public final class BlockIds {

    private BlockIds() {}

    private static final String KEY = "id";

    private static NamespacedKey namespacedKey;
    private static Object stringType;
    private static Method pdcGet;
    private static Method pdcSet;
    private static Method pdcRemove;
    private static Method getContainer;
    private static Method updateQuiet;

    static {
        try {
            Class<?> pdcClass = Class.forName("org.bukkit.persistence.PersistentDataContainer");
            Class<?> dataTypeClass = Class.forName("org.bukkit.persistence.PersistentDataType");

            stringType = dataTypeClass.getField("STRING").get(null);
            // TileState#getPersistentDataContainer was introduced in 1.16.
            getContainer = BlockState.class.getMethod("getPersistentDataContainer");
            pdcGet = pdcClass.getMethod("get", NamespacedKey.class, dataTypeClass);
            pdcSet = pdcClass.getMethod("set", NamespacedKey.class, dataTypeClass, Object.class);
            try {
                pdcRemove = pdcClass.getMethod("remove", NamespacedKey.class);
            } catch (NoSuchMethodException ignored) {
                // 1.16 lacks PDC#remove; an empty string is used as the cleared marker instead.
            }
            updateQuiet = BlockState.class.getMethod("update", boolean.class, boolean.class);
        } catch (Throwable ignored) {
            // 1.12-1.15: no block-state PDC. All calls below become no-ops.
        }
    }

    private static NamespacedKey key() {
        if (namespacedKey == null) {
            namespacedKey = new NamespacedKey(EpicExoticGarden.getInstance(), KEY);
        }
        return namespacedKey;
    }

    /**
     * Stamps the id onto the block's tile state and applies it without running physics.
     *
     * @return {@code true} when the tag was persisted, {@code false} on unsupported versions
     */
    public static boolean setId(@Nonnull Block block, @Nonnull String id) {
        if (getContainer == null) {
            return false;
        }
        try {
            BlockState state = block.getState();
            Object container = getContainer.invoke(state);
            pdcSet.invoke(container, key(), stringType, id);
            updateQuiet.invoke(state, true, false);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Reads the id carried by the block's tile state, or {@code null} when absent/unsupported.
     */
    @Nullable
    public static String getId(@Nonnull Block block) {
        if (getContainer == null) {
            return null;
        }
        try {
            BlockState state = block.getState();
            Object container = getContainer.invoke(state);
            Object value = pdcGet.invoke(container, key(), stringType);
            if (value instanceof String && !((String) value).isEmpty()) {
                return (String) value;
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Removes the id tag from the block's tile state.
     *
     * @return {@code true} when the removal was applied
     */
    public static boolean removeId(@Nonnull Block block) {
        if (getContainer == null) {
            return false;
        }
        try {
            BlockState state = block.getState();
            Object container = getContainer.invoke(state);
            if (pdcRemove != null) {
                pdcRemove.invoke(container, key());
            } else {
                pdcSet.invoke(container, key(), stringType, "");
            }
            updateQuiet.invoke(state, true, false);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
