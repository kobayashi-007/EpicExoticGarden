package io.github.thebusybiscuit.exoticgarden.listeners;

import javax.annotation.Nonnull;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.github.thebusybiscuit.exoticgarden.ExoticGarden;
import io.github.thebusybiscuit.slimefun5.api.events.AndroidFarmEvent;

/**
 * Handles Android farming events for ExoticGarden plants.
 *
 * @author TheBusyBiscuit
 */
public class AndroidListener implements Listener {

    /**
     * Creates a new {@link AndroidListener} and registers it.
     *
     * @param plugin
     *            the ExoticGarden instance
     */
    public AndroidListener(@Nonnull ExoticGarden plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrow(@Nonnull AndroidFarmEvent e) {
        if (e.isAdvanced() && e.getDrop() == null) {
            e.setDrop(ExoticGarden.harvestPlant(e.getBlock()));
        }
    }
}

