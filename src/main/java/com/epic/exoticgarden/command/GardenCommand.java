/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.core.Blocks;
import com.epic.exoticgarden.core.Lang;
import com.epic.exoticgarden.gui.EpicGuide;
import com.epic.exoticgarden.gui.GuideBook;
import com.epic.exoticgarden.item.Items;
import com.epic.exoticgarden.recipe.Recipes;

/**
 * {@code /epicgarden} (alias {@code /eeg}): help, guide, guidebook, give, reload, debug.
 */
public class GardenCommand implements CommandExecutor, TabCompleter {

    private static final String PERM_GUIDE = "epicexoticgarden.guide";
    private static final String PERM_GIVE = "epicexoticgarden.give";
    private static final String PERM_RELOAD = "epicexoticgarden.reload";
    private static final String PERM_DEBUG = "epicexoticgarden.debug";

    private final EpicExoticGarden plugin;

    public GardenCommand(EpicExoticGarden plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "guide" : args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "help":
                return help(sender);
            case "guide":
                return openGuide(sender);
            case "guidebook":
                return guidebook(sender);
            case "give":
                return give(sender, args);
            case "reload":
                return reload(sender);
            case "debug":
                return debug(sender);
            default:
                return help(sender);
        }
    }

    /**
     * Lists the subcommands the sender may actually use. All lines come from the language files
     * ({@code _gui_help_*} keys) so they follow the configured language.
     */
    private boolean help(CommandSender sender) {
        sender.sendMessage(Lang.gui("_gui_help_title", color("&6===== &eEpicExoticGarden Commands &6=====")));
        if (sender.hasPermission(PERM_GUIDE)) {
            sender.sendMessage(Lang.gui("_gui_help_guide", color("&e/eeg guide &7- Open the Exotic Garden Guide")));
            sender.sendMessage(Lang.gui("_gui_help_guidebook",
                    color("&e/eeg guidebook &7- Get the guide book (right-click to use)")));
        }
        if (sender.hasPermission(PERM_GIVE)) {
            sender.sendMessage(Lang.gui("_gui_help_give",
                    color("&e/eeg give <item id> [amount] [player] &7- Give items")));
        }
        if (sender.hasPermission(PERM_RELOAD)) {
            sender.sendMessage(Lang.gui("_gui_help_reload",
                    color("&e/eeg reload &7- Reload config, languages and recipes")));
        }
        if (sender.hasPermission(PERM_DEBUG)) {
            sender.sendMessage(Lang.gui("_gui_help_debug", color("&e/eeg debug &7- Show debug info")));
        }
        sender.sendMessage(Lang.gui("_gui_help_help", color("&e/eeg help &7- Show this help")));
        return true;
    }

    private boolean openGuide(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(onlyPlayers());
            return true;
        }
        if (!sender.hasPermission(PERM_GUIDE)) {
            sender.sendMessage(noPermission());
            return true;
        }
        EpicGuide.openMain((Player) sender);
        return true;
    }

    /**
     * Gives the player the physical guide book; right-clicking it opens the guide.
     */
    private boolean guidebook(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(onlyPlayers());
            return true;
        }
        if (!sender.hasPermission(PERM_GUIDE)) {
            sender.sendMessage(noPermission());
            return true;
        }
        Player player = (Player) sender;
        player.getInventory().addItem(GuideBook.create()).values().forEach(rest ->
                player.getWorld().dropItemNaturally(player.getLocation(), rest));
        player.sendMessage(Lang.cmd("_cmd_guidebook_received",
                color("&aGuide book received. Hold it and &eright-click &ato open the guide.")));
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_GIVE)) {
            sender.sendMessage(noPermission());
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Lang.cmd("_cmd_give_usage",
                    color("&6Usage: /epicgarden give <item id> [amount] [player]")));
            return true;
        }

        String id = args[1].toUpperCase(Locale.ROOT);
        ItemStack item = Items.get(id);
        if (item == null) {
            sender.sendMessage(Lang.cmd("_cmd_give_unknown", color("&cUnknown item id: &f{0}"), id));
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                sender.sendMessage(Lang.cmd("_cmd_give_invalid_amount", color("&cAmount must be a number.")));
                return true;
            }
        }

        Player target;
        if (args.length >= 4) {
            target = Bukkit.getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(Lang.cmd("_cmd_give_player_offline", color("&cPlayer is not online: &f{0}"), args[3]));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(Lang.cmd("_cmd_give_console",
                    color("&6Console must specify a player: /epicgarden give <item id> [amount] <player>")));
            return true;
        }

        item.setAmount(amount);
        target.getInventory().addItem(item).values().forEach(rest ->
                target.getWorld().dropItemNaturally(target.getLocation(), rest));
        // {0} amount, {1} localized item name, {2} target player.
        sender.sendMessage(Lang.cmd("_cmd_give_success",
                color("&aGave &f{0}x {1} &ato &f{2}"), amount, Lang.name(id), target.getName()));
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission(PERM_RELOAD)) {
            sender.sendMessage(noPermission());
            return true;
        }
        plugin.reload();
        sender.sendMessage(Lang.cmd("_cmd_reload_done", color("&aEpicExoticGarden configuration reloaded.")));
        return true;
    }

    private boolean debug(CommandSender sender) {
        if (!sender.hasPermission(PERM_DEBUG)) {
            sender.sendMessage(noPermission());
            return true;
        }
        sender.sendMessage(Lang.cmd("_cmd_debug_title", color("&6EpicExoticGarden Debug Info")));
        sender.sendMessage(Lang.cmd("_cmd_debug_items", color("&7Items: &f{0}"), Items.count()));
        sender.sendMessage(Lang.cmd("_cmd_debug_blocks", color("&7Stored blocks: &f{0}"), Blocks.count()));
        sender.sendMessage(Lang.cmd("_cmd_debug_recipes", color("&7Recipe specs: &f{0}"), Recipes.count()));
        sender.sendMessage(Lang.cmd("_cmd_debug_language", color("&7Language: &f{0}"), Lang.getLanguage()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("help", "guide", "guidebook", "give", "reload", "debug"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission(PERM_GIVE)) {
                return Collections.emptyList();
            }
            List<String> ids = new ArrayList<>();
            for (com.epic.exoticgarden.item.GardenItem item : Items.all()) {
                ids.add(item.getId());
            }
            return filter(ids, args[1]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[3]);
        }

        return Collections.emptyList();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }

    private static String noPermission() {
        return Lang.cmd("_cmd_no_permission", color("&cYou do not have permission."));
    }

    private static String onlyPlayers() {
        return Lang.cmd("_cmd_player_only", color("&cOnly players can use this command."));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
