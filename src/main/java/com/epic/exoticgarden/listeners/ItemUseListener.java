/*
 * Derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: adapted for standalone PDC system, added legacy 1.12 support,
 *                STONE_CROOK handling, and paired block safety checks.
 */
package com.epic.exoticgarden.listeners;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.compat.BlockTags;
import com.epic.exoticgarden.compat.ItemTags;
import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.Sounds;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.item.GardenItem;
import com.epic.exoticgarden.item.Items;

/**
 * Handles EpicExoticGarden <em>item</em> interactions: eating custom head-foods, the Crook's
 * extra sapling drops, and Grass Seeds turning dirt into grass. Drinks are potions and therefore
 * use the vanilla drinking mechanic.
 */
public class ItemUseListener implements Listener {

    private final EpicExoticGarden plugin;

    public ItemUseListener(EpicExoticGarden plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (isOffHand(e)) {
            return;
        }

        ItemStack stack = e.getItem();
        String id = ItemTags.getId(stack);
        if (id == null) {
            return;
        }
        GardenItem def = Items.getDef(id);
        if (def == null) {
            return;
        }

        Player player = e.getPlayer();

        if ("GRASS_SEEDS".equals(id)) {
            useGrassSeeds(e, player, stack, action);
            return;
        }

        if ("CROOK".equals(id) || "STONE_CROOK".equals(id)) {
            // The hoe-based Crook tools have no vanilla right-click use.
            e.setCancelled(true);
            return;
        }

        if (def.isEdible()) {
            handleFood(e, player, stack, def, action);
            return;
        }

        // Pantry/ingredient skulls must not be placed as player-head blocks.
        if (!def.isPlaceable() && action == Action.RIGHT_CLICK_BLOCK && isSkull(stack)) {
            e.setCancelled(true);
        }
    }

    private void handleFood(PlayerInteractEvent e, Player player, ItemStack stack, GardenItem def, Action action) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = e.getClickedBlock();
            // Let containers and other interactable blocks be used normally (unless sneaking).
            if (clicked != null && !player.isSneaking() && isInteractable(clicked.getType())) {
                return;
            }
        }

        // Never let edible player-heads be placed as blocks, even when the hunger bar is full.
        if (isSkull(stack)) {
            e.setCancelled(true);
        }

        if (player.getFoodLevel() >= 20) {
            return;
        }

        int foodValue = Math.max(1, def.getFoodValue());
        player.setFoodLevel(Math.min(player.getFoodLevel() + foodValue, 20));
        player.setSaturation(player.getSaturation() + foodValue);
        Sounds.play(player.getWorld(), player.getEyeLocation(), SoundCategory.PLAYERS, 1f, 1f,
                "ENTITY_PLAYER_EAT", "ENTITY_GENERIC_EAT");

        if (player.getGameMode() != GameMode.CREATIVE) {
            stack.setAmount(stack.getAmount() - 1);
        }
    }

    private void useGrassSeeds(PlayerInteractEvent e, Player player, ItemStack stack, Action action) {
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block target = e.getClickedBlock();
        if (target == null || target.getType() != Materials.DIRT.material) {
            return;
        }

        e.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            stack.setAmount(stack.getAmount() - 1);
        }

        Materials.setBlock(target, Materials.GRASS_BLOCK);
        Block above = target.getRelative(BlockFace.UP);
        if (above.getType() == Materials.AIR.material) {
            Materials.setBlock(above, Materials.SHORT_GRASS);
        }
        target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, Materials.SHORT_GRASS.material);
    }

    /**
     * Crook on leaves: a 25% chance for an extra sapling of the matching wood type.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrookLeaves(BlockBreakEvent e) {
        ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
        String toolId = ItemTags.getId(tool);
        if (!"CROOK".equals(toolId) && !"STONE_CROOK".equals(toolId)) {
            return;
        }

        Block block = e.getBlock();
        if (!BlockTags.isLeaves(block.getType()) || ThreadLocalRandom.current().nextInt(100) >= 25) {
            return;
        }

        ItemStack sapling = saplingForLeaves(block);
        if (sapling != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), sapling);
        }
    }

    private static ItemStack saplingForLeaves(Block block) {
        Material type = block.getType();

        if (XMat.LEGACY) {
            // Legacy worlds pack all wood variants into one Material distinguished by data.
            XMat[] oakToJungle = {
                Materials.OAK_SAPLING, Materials.SPRUCE_SAPLING, Materials.BIRCH_SAPLING, Materials.JUNGLE_SAPLING
            };
            XMat[] acaciaToDarkOak = { Materials.ACACIA_SAPLING, Materials.DARK_OAK_SAPLING };

            String name = type.name();
            if ("LEAVES".equals(name)) {
                return Materials.stack(oakToJungle[block.getData() & 3]);
            }
            if ("LEAVES_2".equals(name)) {
                return Materials.stack(acaciaToDarkOak[block.getData() & 1]);
            }
            return null;
        }

        String name = type.name();
        if (name.endsWith("_LEAVES")) {
            Material sapling = Material.getMaterial(name.substring(0, name.length() - "_LEAVES".length()) + "_SAPLING");
            return sapling == null ? null : new ItemStack(sapling);
        }
        return null;
    }

    // The blocks a custom food should defer to instead of being eaten (ported from the addon).
    private static boolean isInteractable(Material material) {
        switch (material.name()) {
            case "ANVIL":
            case "BREWING_STAND":
            case "BREWING_STAND_BLOCK":
            case "CAKE":
            case "CHEST":
            case "HOPPER":
            case "TRAPPED_CHEST":
            case "ENDER_CHEST":
            case "CAULDRON":
            case "SHULKER_BOX":
            case "BARREL":
                return true;
            default:
                return material.name().endsWith("_SHULKER_BOX");
        }
    }

    private static boolean isSkull(ItemStack stack) {
        return stack.getType() == Materials.PLAYER_HEAD.material;
    }

    // PlayerInteractEvent#getHand() is 1.9+; on 1.12 only the main hand exists.
    private static boolean isOffHand(PlayerInteractEvent e) {
        try {
            Object hand = PlayerInteractEvent.class.getMethod("getHand").invoke(e);
            return hand != null && !"HAND".equals(((Enum<?>) hand).name());
        } catch (Throwable ex) {
            return false;
        }
    }
}
