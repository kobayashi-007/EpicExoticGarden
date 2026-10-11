/*
 * Derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: adapted for standalone PDC tag system.
 */
package com.epic.exoticgarden.plant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.item.Items;

/**
 * Represents a berry, vegetable or magical crop that can be grown and harvested.
 *
 * <p>Ported from the original ExoticGarden; item lookups now go through the standalone
 * {@link Items} registry instead of Slimefun.</p>
 */
public class Berry {

    private final ItemStack item;
    private final String id;
    private final String texture;
    private final PlantType type;

    public Berry(@Nonnull String id, @Nonnull PlantType type, @Nonnull String texture) {
        this(null, id, type, texture);
    }

    public Berry(@Nullable ItemStack item, @Nonnull String id, @Nonnull PlantType type, @Nonnull String texture) {
        this.item = item;
        this.id = id;
        this.texture = texture;
        this.type = type;
    }

    @Nonnull
    public String getID() {
        return this.id;
    }

    /**
     * Returns the produce item stack. Magical crops carry an explicit essence item; everything
     * else resolves its fruit stack from the registry.
     */
    @Nonnull
    public ItemStack getItem() {
        if (type == PlantType.ORE_PLANT) {
            return item.clone();
        }
        return Items.get(id);
    }

    @Nonnull
    public String getTexture() {
        return this.texture;
    }

    @Nonnull
    public PlantType getType() {
        return type;
    }

    /**
     * Returns the id of the unripe (sapling-stage) form of this plant.
     */
    @Nonnull
    public String toBush() {
        return type == PlantType.ORE_PLANT ? this.id.replace("_ESSENCE", "_PLANT") : this.id + "_BUSH";
    }

    public boolean isSoil(@Nonnull Material type) {
        return type == Materials.GRASS_BLOCK.material || type == Materials.DIRT.material;
    }

}
