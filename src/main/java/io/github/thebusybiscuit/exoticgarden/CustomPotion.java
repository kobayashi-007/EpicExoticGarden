package io.github.thebusybiscuit.exoticgarden;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Color;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import io.github.thebusybiscuit.slimefun5.libraries.dough.common.ChatColors;

/**
 * A custom potion {@link ItemStack} used for juice and drink recipes.
 *
 * @author TheBusyBiscuit
 */
public final class CustomPotion extends ItemStack {

    @ParametersAreNonnullByDefault
    public CustomPotion(String name, Color color, PotionEffect effect, String... lore) {
        super(MaterialCompat.safe(XMaterial.POTION));

        PotionMeta meta = (PotionMeta) getItemMeta();
        List<String> list = new ArrayList<>();

        for (String line : lore) {
            list.add(ChatColors.color(line));
        }

        meta.setDisplayName(ChatColors.color(name));
        meta.setLore(list);
        // PotionMeta.setColor is 1.11+; absent on 1.8.
        try { meta.setColor(color); } catch (NoSuchMethodError ignored) { }
        meta.addCustomEffect(effect, true);

        setItemMeta(meta);
    }

}

