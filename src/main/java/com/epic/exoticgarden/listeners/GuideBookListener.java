/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.gui.EpicGuide;
import com.epic.exoticgarden.gui.GuideBook;

/**
 * Opens the EpicGuide when a player right-clicks while holding the guide book.
 */
public class GuideBookListener implements Listener {

    public GuideBookListener(EpicExoticGarden plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!GuideBook.isGuideBook(e.getItem())) {
            return;
        }
        // Never trigger the vanilla use action (e.g. interacting with a block) for the book.
        e.setCancelled(true);

        Player player = e.getPlayer();
        if (!player.hasPermission("epicexoticgarden.guide")) {
            return;
        }
        EpicGuide.openMain(player);
    }
}
