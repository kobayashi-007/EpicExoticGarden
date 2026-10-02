package io.github.thebusybiscuit.exoticgarden.items;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import io.github.thebusybiscuit.exoticgarden.MaterialCompat;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun5.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.ItemUtils;

/**
 * Grass Seeds can be planted on dirt to grow grass.
 *
 * @author TheBusyBiscuit
 */
public class GrassSeeds extends SimpleSlimefunItem<ItemUseHandler> {

    @ParametersAreNonnullByDefault
    public GrassSeeds(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        setGuideType("decoration");
    }

    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            if (e.getClickedBlock().isPresent()) {
                Block b = e.getClickedBlock().get();

                if (b.getType() == MaterialCompat.safe(XMaterial.DIRT)) {
                    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        ItemUtils.consumeItem(e.getItem(), false);
                    }

                    b.setType(MaterialCompat.safe(XMaterial.GRASS_BLOCK));

                    if (b.getRelative(BlockFace.UP).getType() == MaterialCompat.safe(XMaterial.AIR)) {
                        b.getRelative(BlockFace.UP).setType(MaterialCompat.safe(XMaterial.SHORT_GRASS));
                    }

                    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.SHORT_GRASS));
                }
            }
        };
    }

}

