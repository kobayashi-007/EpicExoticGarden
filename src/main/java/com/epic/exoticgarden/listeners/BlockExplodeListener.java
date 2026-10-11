/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

/**
 * Handles bed/crystal/tnt {@link BlockExplodeEvent}s the same way entity explosions are handled in
 * {@link PlantsListener}. This event only exists on 1.9+, so the class is loaded conditionally via
 * {@code Class.forName} and must never be referenced from classes that load on 1.12.
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
