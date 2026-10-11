/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.item;

import javax.annotation.Nonnull;

import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.core.Lang;

/**
 * Guide categories for EpicExoticGarden content.
 * Replaces Slimefun's ItemGroup/NestedItemGroup hierarchy.
 */
public enum Category {

    PLANTS_AND_FRUITS("_gui_category_plants", "Plants & Fruits", Materials.CARROT),
    MISC("_gui_category_ingredients", "Ingredients & Tools", Materials.WHEAT),
    FOOD("_gui_category_dishes", "Dishes", Materials.COOKED_BEEF),
    DRINKS("_gui_category_drinks", "Drinks", Materials.POTION),
    MAGICAL("_gui_category_magical", "Magical Crops", Materials.BLAZE_POWDER);

    private final String langKey;
    private final String fallbackName;
    private final XMat icon;

    Category(@Nonnull String langKey, @Nonnull String fallbackName, @Nonnull XMat icon) {
        this.langKey = langKey;
        this.fallbackName = fallbackName;
        this.icon = icon;
    }

    @Nonnull
    public String getDisplayName() {
        return Lang.gui(langKey, fallbackName);
    }

    @Nonnull
    public XMat getIcon() {
        return icon;
    }
}
