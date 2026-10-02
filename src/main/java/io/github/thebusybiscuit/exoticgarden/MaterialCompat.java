package io.github.thebusybiscuit.exoticgarden;

import javax.annotation.Nonnull;

import org.bukkit.Material;

import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;

/**
 * Resolves {@link XMaterial} constants to a {@link Material} that exists on the running server.
 * Keeps ExoticGarden loadable on legacy versions (e.g. 1.8) where modern flattening constants like
 * {@code PLAYER_HEAD}, {@code OAK_SAPLING} or {@code SWEET_BERRIES} are absent from the {@link Material}
 * enum and a direct reference would throw {@link NoSuchFieldError}.
 *
 * @author TheBusyBiscuit
 */
public final class MaterialCompat {

    private MaterialCompat() {}

    // Sensible legacy substitutes for materials that don't exist on older servers (e.g. 1.8).
    private static final java.util.Map<XMaterial, XMaterial> LEGACY_SUBSTITUTES = buildLegacySubstitutes();

    private static java.util.Map<XMaterial, XMaterial> buildLegacySubstitutes() {
        java.util.Map<XMaterial, XMaterial> m = new java.util.EnumMap<>(XMaterial.class);
        m.put(XMaterial.NETHERITE_BLOCK, XMaterial.DIAMOND_BLOCK);
        m.put(XMaterial.NETHERITE_INGOT, XMaterial.DIAMOND);
        m.put(XMaterial.NETHERITE_SCRAP, XMaterial.IRON_NUGGET);
        m.put(XMaterial.ANCIENT_DEBRIS, XMaterial.NETHERRACK);
        m.put(XMaterial.BEEHIVE, XMaterial.DISPENSER);
        m.put(XMaterial.BEE_NEST, XMaterial.DISPENSER);
        m.put(XMaterial.HONEY_BLOCK, XMaterial.SLIME_BLOCK);
        m.put(XMaterial.BARREL, XMaterial.CHEST);
        m.put(XMaterial.BLAST_FURNACE, XMaterial.FURNACE);
        m.put(XMaterial.SMOKER, XMaterial.FURNACE);
        m.put(XMaterial.CAMPFIRE, XMaterial.NETHERRACK);
        m.put(XMaterial.SMITHING_TABLE, XMaterial.CRAFTING_TABLE);
        m.put(XMaterial.CARTOGRAPHY_TABLE, XMaterial.CRAFTING_TABLE);
        m.put(XMaterial.FLETCHING_TABLE, XMaterial.CRAFTING_TABLE);
        m.put(XMaterial.LOOM, XMaterial.CRAFTING_TABLE);
        m.put(XMaterial.STONECUTTER, XMaterial.CRAFTING_TABLE);
        m.put(XMaterial.GRINDSTONE, XMaterial.ANVIL);
        m.put(XMaterial.LANTERN, XMaterial.GLOWSTONE);
        m.put(XMaterial.COMPOSTER, XMaterial.CHEST);
        m.put(XMaterial.MAGMA_BLOCK, XMaterial.NETHERRACK);
        m.put(XMaterial.LODESTONE, XMaterial.IRON_BLOCK);
        m.put(XMaterial.BLACKSTONE, XMaterial.COBBLESTONE);
        m.put(XMaterial.OBSERVER, XMaterial.PISTON);
        return m;
    }

    private static Material substitute(XMaterial xMaterial) {
        XMaterial sub = LEGACY_SUBSTITUTES.get(xMaterial);
        return sub != null ? sub.parseMaterial() : null;
    }

    @Nonnull
    public static Material safe(@Nonnull XMaterial material) {
        Material resolved = material.parseMaterial();
        if (resolved == null) {
            resolved = substitute(material);
        }
        return resolved != null ? resolved : Material.STONE;
    }
}
