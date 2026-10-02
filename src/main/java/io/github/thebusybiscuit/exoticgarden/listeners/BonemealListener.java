package io.github.thebusybiscuit.exoticgarden.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

import io.github.thebusybiscuit.exoticgarden.MaterialCompat;
import io.github.thebusybiscuit.exoticgarden.items.BonemealableItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.ParticleCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCategory;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.SoundCompat;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/**
 * Handles {@link BlockFertilizeEvent} (bone meal) to honour the per-item "disable bonemeal" setting on
 * {@link BonemealableItem}s. {@link BlockFertilizeEvent} only exists from Minecraft 1.13 onwards, so this
 * lives in its own listener class that is only registered on supporting versions, keeping
 * {@link PlantsListener} loadable on 1.8.
 *
 * @author TheBusyBiscuit
 */
public class BonemealListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBonemealPlant(BlockFertilizeEvent e) {
        Block b = e.getBlock();

        if (b.getType() == MaterialCompat.safe(XMaterial.OAK_SAPLING)) {
            SlimefunItem item = BlockStorage.check(b);

            if (item instanceof BonemealableItem && ((BonemealableItem) item).isBonemealDisabled()) {
                e.setCancelled(true);
                ParticleCompat.spawn(b.getWorld(), resolveAngryVillagerParticle(), b.getLocation().clone().add(0.5, 0, 0.5), 4);
                SoundCompat.playAt(b.getLocation(), "ENTITY_VILLAGER_NO", SoundCategory.BLOCKS, 1, 1);
            }
        }
    }

    // The angry-villager particle is named VILLAGER_ANGRY (1.9-1.20.4) and ANGRY_VILLAGER (1.20.5+);
    // resolve it reflectively and return it as an opaque Object for ParticleCompat. Null on 1.8 (no particles).
    private static Object resolveAngryVillagerParticle() {
        for (String name : new String[] { "ANGRY_VILLAGER", "VILLAGER_ANGRY" }) {
            try {
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                return particleClass.getField(name).get(null);
            } catch (Throwable ignored) {
                // Try the next name / unsupported on this version.
            }
        }

        return null;
    }
}
