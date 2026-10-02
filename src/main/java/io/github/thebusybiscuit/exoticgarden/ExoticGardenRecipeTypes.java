package io.github.thebusybiscuit.exoticgarden;

import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;

import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;

public final class ExoticGardenRecipeTypes {

    private ExoticGardenRecipeTypes() {}

    public static final RecipeType KITCHEN = new RecipeType(new NamespacedKey(ExoticGarden.instance, "kitchen"), new SlimefunItemStack("KITCHEN", MaterialCompat.safe(XMaterial.CAULDRON)), "", "\u00a7rThis item must be made", "\u00a7rin a Kitchen");
    public static final RecipeType BREAKING_GRASS = new RecipeType(new NamespacedKey(ExoticGarden.instance, "breaking_grass"), CustomItemStack.create(MaterialCompat.safe(XMaterial.SHORT_GRASS), "\u00a77Breaking Grass"));
    public static final RecipeType HARVEST_TREE = new RecipeType(new NamespacedKey(ExoticGarden.instance, "harvest_tree"), CustomItemStack.create(MaterialCompat.safe(XMaterial.OAK_LEAVES), "\u00a7aHarvesting a Tree", "", "\u00a7rYou can obtain this Item by", "\u00a7rharvesting the shown Tree"));
    public static final RecipeType HARVEST_BUSH = new RecipeType(new NamespacedKey(ExoticGarden.instance, "harvest_bush"), CustomItemStack.create(MaterialCompat.safe(XMaterial.OAK_LEAVES), "\u00a7aHarvesting a Bush", "", "\u00a7rYou can obtain this Item by", "\u00a7rharvesting the shown Bush"));

}



