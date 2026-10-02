package io.github.thebusybiscuit.exoticgarden.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

/**
 * Handles {@link BlockExplodeEvent}, which only exists from Minecraft 1.9 onwards. It is kept in its
 * own listener class so {@link PlantsListener} stays loadable on 1.8, where this event is absent.
 *
 * @author TheBusyBiscuit
 */
public class BlockExplodeListener implements Listener {

    private final PlantsListener parent;

    public BlockExplodeListener(PlantsListener parent) {
        this.parent = parent;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeAll(parent.getAffectedBlocks(e.blockList()));
    }
}
