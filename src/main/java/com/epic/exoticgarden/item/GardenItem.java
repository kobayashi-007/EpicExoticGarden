/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.item;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

/**
 * Immutable definition of a registered EpicExoticGarden item.
 *
 * <p>Every item owns a unique id ({@link com.epic.exoticgarden.compat.ItemTags}); the prototype
 * stored here is already stamped, named and ready to clone. This is the standalone replacement
 * for Slimefun's {@code SlimefunItem}/{@code SlimefunItemStack} pair.</p>
 */
public class GardenItem {

    private final String id;
    private final Category category;
    private final ItemStack prototype;
    private final boolean edible;
    /** Mutable so the recipe-file hunger override can be applied after registration. */
    private int foodValue;
    /** Bushes, saplings and magical plants can be placed as blocks; heads/foods cannot. */
    private final boolean placeable;

    public GardenItem(@Nonnull String id, @Nonnull Category category, @Nonnull ItemStack prototype,
                      boolean edible, int foodValue, boolean placeable) {
        this.id = id;
        this.category = category;
        this.prototype = prototype;
        this.edible = edible;
        this.foodValue = foodValue;
        this.placeable = placeable;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Category getCategory() {
        return category;
    }

    @Nonnull
    public ItemStack getPrototype() {
        return prototype;
    }

    /**
     * Returns a fresh copy of this item.
     */
    @Nonnull
    public ItemStack create() {
        return prototype.clone();
    }

    @Nonnull
    public ItemStack create(int amount) {
        ItemStack stack = prototype.clone();
        stack.setAmount(amount);
        return stack;
    }

    public boolean isEdible() {
        return edible;
    }

    public int getFoodValue() {
        return foodValue;
    }

    /** Updates the configured hunger value (from the editable recipe files). */
    public void setFoodValue(int foodValue) {
        this.foodValue = Math.max(0, foodValue);
    }

    public boolean isPlaceable() {
        return placeable;
    }
}
