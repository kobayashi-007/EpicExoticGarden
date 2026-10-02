package io.github.thebusybiscuit.exoticgarden.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Bukkit;
import io.github.thebusybiscuit.exoticgarden.MaterialCompat;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCategory;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCompat;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.exoticgarden.ExoticGarden;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun5.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;

/**
 * The Kitchen is a multi-block machine used to craft food items.
 *
 * @author TheBusyBiscuit
 */
public class Kitchen extends MultiBlockMachine {

    private final ExoticGarden plugin;

    @ParametersAreNonnullByDefault
    public Kitchen(ExoticGarden plugin, ItemGroup itemGroup) {
        super(itemGroup, new SlimefunItemStack("KITCHEN", MaterialCompat.safe(XMaterial.CAULDRON)), new ItemStack[] { CustomItemStack.create(MaterialCompat.safe(XMaterial.BRICK_STAIRS), "\u00a7oBrick Stairs (upside down)"), CustomItemStack.create(MaterialCompat.safe(XMaterial.BRICK_STAIRS), "\u00a7oBrick Stairs (upside down)"), new ItemStack(MaterialCompat.safe(XMaterial.BRICKS)), new ItemStack(MaterialCompat.safe(XMaterial.STONE_PRESSURE_PLATE)), new ItemStack(MaterialCompat.safe(XMaterial.IRON_TRAPDOOR)), new ItemStack(MaterialCompat.safe(XMaterial.BOOKSHELF)), new ItemStack(MaterialCompat.safe(XMaterial.FURNACE)), new ItemStack(MaterialCompat.safe(XMaterial.DISPENSER)), new ItemStack(MaterialCompat.safe(XMaterial.CRAFTING_TABLE)) }, new ItemStack[0], BlockFace.SELF);

        this.plugin = plugin;
        setGuideType("machines");
    }

    @Override
    public void onInteract(Player p, Block b) {
        Block dispenser = b.getRelative(BlockFace.DOWN);

        Furnace furnace = locateFurnace(dispenser);
        FurnaceInventory furnaceInventory = furnace.getInventory();

        Inventory inv = ((Dispenser) dispenser.getState()).getInventory();
        List<ItemStack[]> inputs = RecipeType.getRecipeInputList(this);

        recipe:
        for (ItemStack[] input : inputs) {
            for (int i = 0; i < inv.getContents().length; i++) {
                if (!SlimefunUtils.isItemSimilar(inv.getContents()[i], input[i], true))
                    continue recipe;
            }

            ItemStack output = RecipeType.getRecipeOutputList(this, input);
            SlimefunItem outputItem = SlimefunItem.getByItem(output);

            if (outputItem == null || outputItem.canUse(p, true)) {
                boolean canFit = furnaceInventory.getResult() == null || (furnaceInventory.getResult().getAmount() + output.getAmount() <= 64 && SlimefunUtils.isItemSimilar(furnaceInventory.getResult(), output, true));

                if (!canFit) {
                    Slimefun.getLocalization().sendMessage(p, "machines.full-inventory", true);
                    return;
                }

                for (int i = 0; i < inv.getContents().length; i++) {
                    ItemStack item = inv.getItem(i);

                    if (item != null) {
                        ItemUtils.consumeItem(item, item.getType() == MaterialCompat.safe(XMaterial.MILK_BUCKET));
                    }
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> SoundCompat.playAt(furnace.getLocation(), "BLOCK_LAVA_EXTINGUISH", SoundCategory.BLOCKS, 1F, 1F), 55L);

                for (int i = 1; i < 7; i++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> SoundCompat.playAt(furnace.getLocation(), "BLOCK_METAL_PLACE", SoundCategory.BLOCKS, 7F, 1F), i * 5L);
                }

                if (furnaceInventory.getResult() == null) {
                    furnaceInventory.setResult(output);
                } else {
                    furnaceInventory.getResult().setAmount(furnaceInventory.getResult().getAmount() + output.getAmount());
                }
            }

            return;
        }

        Slimefun.getLocalization().sendMessage(p, "machines.pattern-not-found", true);
    }

    @Nonnull
    private static Furnace locateFurnace(@Nonnull Block b) {
        if (b.getRelative(BlockFace.EAST).getType() == MaterialCompat.safe(XMaterial.FURNACE)) {
            return (Furnace) PaperLib.getBlockState(b.getRelative(BlockFace.EAST), false).getState();
        } else if (b.getRelative(BlockFace.WEST).getType() == MaterialCompat.safe(XMaterial.FURNACE)) {
            return (Furnace) PaperLib.getBlockState(b.getRelative(BlockFace.WEST), false).getState();
        } else if (b.getRelative(BlockFace.NORTH).getType() == MaterialCompat.safe(XMaterial.FURNACE)) {
            return (Furnace) PaperLib.getBlockState(b.getRelative(BlockFace.NORTH), false).getState();
        } else {
            return (Furnace) PaperLib.getBlockState(b.getRelative(BlockFace.SOUTH), false).getState();
        }
    }
}

