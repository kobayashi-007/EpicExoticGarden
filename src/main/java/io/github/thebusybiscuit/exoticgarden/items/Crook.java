package io.github.thebusybiscuit.exoticgarden.items;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.utils.compatibility.Tag;

import io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.attributes.DamageableItem;
import io.github.thebusybiscuit.slimefun5.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun5.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;

/**
 * The Crook is a tool that increases sapling drop rates from leaves.
 *
 * @author TheBusyBiscuit
 */
public class Crook extends SimpleSlimefunItem<ToolUseHandler> implements NotPlaceable, DamageableItem {

    private static final int CHANCE = 25;

    @ParametersAreNonnullByDefault
    public Crook(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onRightClick());
    }

    @Nonnull
    private ItemUseHandler onRightClick() {
        return PlayerRightClickEvent::cancel;
    }

    @Override
    public ToolUseHandler getItemHandler() {
        return (e, tool, fortune, drops) -> {
            damageItem(e.getPlayer(), tool);

            if (Tag.LEAVES.isTagged(e.getBlock().getType()) && ThreadLocalRandom.current().nextInt(100) < CHANCE) {
                ItemStack sapling = new ItemStack(Material.getMaterial(e.getBlock().getType().toString().replace("LEAVES", "SAPLING")));
                drops.add(sapling);
            }
        };
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

}

