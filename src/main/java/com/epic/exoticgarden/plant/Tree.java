/*
 * Derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: replaced Slimefun dependency with standalone PDC item lookup.
 */
package com.epic.exoticgarden.plant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.item.Items;
import com.epic.exoticgarden.schematics.Schematic;

/**
 * A custom fruit tree grown from a sapling and built from a bundled schematic.
 */
public class Tree {

    private final String sapling;
    private final String texture;
    private final String fruit;
    private final List<Material> soils;

    private Schematic schematic;

    public Tree(@Nonnull String fruit, @Nonnull String texture, @Nonnull Material... soil) {
        this.sapling = fruit + "_SAPLING";
        this.texture = texture;
        this.fruit = fruit;
        this.soils = Arrays.asList(soil);
    }

    @Nonnull
    public Schematic getSchematic() throws IOException {
        if (schematic == null) {
            schematic = Schematic.loadSchematic(new File(EpicExoticGarden.getInstance().getSchematicsFolder(), fruit + "_TREE.schematic"));
        }
        return schematic;
    }

    @Nonnull
    public ItemStack getItem() {
        return Items.get(sapling);
    }

    @Nonnull
    public String getTexture() {
        return this.texture;
    }

    @Nonnull
    public ItemStack getFruit() {
        return Items.get(fruit);
    }

    @Nonnull
    public String getFruitID() {
        return fruit;
    }

    @Nonnull
    public String getSapling() {
        return this.sapling;
    }

    public boolean isSoil(@Nonnull Material material) {
        return soils.contains(material);
    }

}
