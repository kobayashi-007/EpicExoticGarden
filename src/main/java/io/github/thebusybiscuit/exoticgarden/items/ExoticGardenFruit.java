package io.github.thebusybiscuit.exoticgarden.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCategory;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCompat;

/**
 * Represents an edible fruit or plant item from ExoticGarden.
 *
 * @author TheBusyBiscuit
 */
public class ExoticGardenFruit extends SimpleSlimefunItem<ItemUseHandler> {

    private final boolean edible;

    @ParametersAreNonnullByDefault
    public ExoticGardenFruit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, boolean edible, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.edible = edible;
        // Fruits and dishes use skull textures, so the material heuristic misses them.
        setGuideType("food");
    }

    @ParametersAreNonnullByDefault
    public ExoticGardenFruit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, boolean edible, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
        this.edible = edible;
        setGuideType("food");
    }

    @Override
    public boolean useVanillaBlockBreaking() {
        return true;
    }

    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            Optional<Block> block = e.getClickedBlock();

            if (block.isPresent()) {
                Material material = block.get().getType();

                if (e.getPlayer().isSneaking() || !isInteractable(material)) {
                    e.cancel();
                } else {
                    return;
                }
            }

            if (edible && e.getPlayer().getFoodLevel() < 20) {
                restoreHunger(e.getPlayer());
                ItemUtils.consumeItem(e.getItem(), false);
            }
        };
    }

    private boolean isInteractable(@Nonnull Material material) {
        // We cannot rely on Material#isInteractable() sadly
        // as it would allow the placement of this block on strange items like stairs...
        switch (material) {
            case ANVIL:
            case BREWING_STAND:
            case CAKE:
            case CHEST:
            case HOPPER:
            case TRAPPED_CHEST:
            case ENDER_CHEST:
            case CAULDRON:
            case SHULKER_BOX:
                return true;
            default:
                return material.name().equals("BARREL") || material.name().endsWith("_SHULKER_BOX");
        }
    }

    protected int getFoodValue() {
        return 2;
    }

    private void restoreHunger(@Nonnull Player p) {
        int level = p.getFoodLevel() + getFoodValue();
        SoundCompat.playFor(p, p.getEyeLocation(), "ENTITY_GENERIC_EAT", SoundCategory.PLAYERS, 1, 1);
        p.setFoodLevel(Math.min(level, 20));
        p.setSaturation(p.getSaturation() + getFoodValue());
    }

}

