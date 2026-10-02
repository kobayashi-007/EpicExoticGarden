package io.github.thebusybiscuit.exoticgarden.items;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.exoticgarden.ExoticGardenRecipeTypes;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;

/**
 * Represents a custom food item with a configurable hunger restoration value.
 *
 * @author TheBusyBiscuit
 */
public class CustomFood extends ExoticGardenFruit {

    private final int food;

    @ParametersAreNonnullByDefault
    public CustomFood(ItemGroup itemGroup, SlimefunItemStack item, ItemStack[] recipe, int food) {
        super(itemGroup, item, ExoticGardenRecipeTypes.KITCHEN, true, recipe);
        this.food = food;
    }

    @ParametersAreNonnullByDefault
    public CustomFood(ItemGroup itemGroup, SlimefunItemStack item, int amount, ItemStack[] recipe, int food) {
        super(itemGroup, item, ExoticGardenRecipeTypes.KITCHEN, true, recipe, withAmount(item.item(), amount));
        this.food = food;
    }

    private static ItemStack withAmount(ItemStack stack, int amount) {
        ItemStack copy = stack.clone();
        copy.setAmount(amount);
        return copy;
    }

    @Override
    public int getFoodValue() {
        return food;
    }

}

