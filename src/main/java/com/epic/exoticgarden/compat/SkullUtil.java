/*
 * EpicExoticGarden - Copyright (C) 2026 LinChangQing, Kobayashi, quantifyingepic
 * Licensed under GPLv3. See LICENSE.
 */
package com.epic.exoticgarden.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.epic.exoticgarden.EpicExoticGarden;

/**
 * Applies custom skins to player heads, both on {@link ItemStack}s and placed {@link Block}s.
 *
 * <p>Standalone replacement for Slimefun's {@code VersionedPlayerHead}. On 1.18.1+ servers the
 * Bukkit {@code PlayerProfile}/{@code PlayerTextures} API is used; on older servers (down to 1.12)
 * an {@code com.mojang.authlib.GameProfile} with a {@code textures} property is injected
 * reflectively into the CraftBukkit skull meta/state. No NMS classes are referenced directly.</p>
 */
public final class SkullUtil {

    private SkullUtil() {}

    private static final String TEXTURE_URL = "http://textures.minecraft.net/texture/";

    private static final boolean MODERN_PROFILES;
    private static Method createProfile;
    private static Class<?> playerProfileClass;
    private static Method getTextures;
    private static Method setSkin;
    private static Method setTextures;
    private static Method metaSetOwnerProfile;
    private static Method stateSetOwnerProfile;

    private static final ConcurrentHashMap<String, Object> PROFILE_CACHE = new ConcurrentHashMap<>();

    static {
        boolean modern = false;
        try {
            playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
            createProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);
            getTextures = playerProfileClass.getMethod("getTextures");
            setTextures = playerProfileClass.getMethod("setTextures", texturesClass);
            setSkin = texturesClass.getMethod("setSkin", URL.class);
            metaSetOwnerProfile = SkullMeta.class.getMethod("setOwnerProfile", playerProfileClass);
            Class<?> skullState = Class.forName("org.bukkit.block.Skull");
            stateSetOwnerProfile = skullState.getMethod("setOwnerProfile", playerProfileClass);
            modern = true;
        } catch (Throwable ignored) {
            // Legacy server (1.12-1.17): fall back to GameProfile reflection.
        }
        MODERN_PROFILES = modern;
    }

    /**
     * Converts a textures.minecraft.net texture hash into the base64 profile-property value.
     */
    @Nonnull
    public static String hashToBase64(@Nonnull String hash) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + TEXTURE_URL + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Creates a textured player-head item.
     */
    @Nonnull
    public static ItemStack head(@Nonnull String hash) {
        ItemStack item = Materials.stack(Materials.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta) {
            applyProfile(meta, hash);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Skins an already placed player-head block.
     *
     * @param update whether physics/neighbours should be updated after setting the skin
     */
    public static void setSkin(@Nonnull Block block, @Nonnull String hash, boolean update) {
        org.bukkit.block.BlockState state = block.getState();
        try {
            Object profile = profile(hash);

            if (MODERN_PROFILES && stateSetOwnerProfile != null) {
                stateSetOwnerProfile.invoke(state, profile);
                state.update(true, update);
                return;
            }

            Field field = findField(state.getClass(), "profile");
            if (field != null) {
                field.setAccessible(true);
                field.set(state, profile);
                state.update(true, update);
            }
        } catch (Throwable t) {
            EpicExoticGarden.getInstance().getLogger().log(Level.FINE, "Could not set skull skin on block", t);
        }
    }

    private static void applyProfile(@Nonnull ItemMeta meta, @Nonnull String hash) {
        try {
            Object profile = profile(hash);

            if (MODERN_PROFILES && metaSetOwnerProfile != null) {
                metaSetOwnerProfile.invoke(meta, profile);
                return;
            }

            Field field = findField(meta.getClass(), "profile");
            if (field != null) {
                field.setAccessible(true);
                field.set(meta, profile);
            }
        } catch (Throwable t) {
            EpicExoticGarden.getInstance().getLogger().log(Level.FINE, "Could not apply skull profile", t);
        }
    }

    /**
     * Builds (and caches) the skin profile object for a texture hash.
     */
    @Nonnull
    private static Object profile(@Nonnull String hash) throws Exception {
        Object cached = PROFILE_CACHE.get(hash);
        if (cached != null) {
            return cached;
        }

        UUID uuid = UUID.nameUUIDFromBytes(("eeg:" + hash).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Object profile;

        if (MODERN_PROFILES) {
            profile = createProfile.invoke(null, uuid, "eeg");
            Object textures = getTextures.invoke(profile);
            setSkin.invoke(textures, new URL(TEXTURE_URL + hash));
            setTextures.invoke(profile, textures);
        } else {
            // com.mojang.authlib.GameProfile + Property (bundled with every CraftBukkit/Spigot/Paper server)
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, "eeg");
            Object property = propertyClass.getConstructor(String.class, String.class)
                    .newInstance("textures", hashToBase64(hash));

            Object propertyMap = gameProfileClass.getMethod("getProperties").invoke(profile);
            propertyMap.getClass().getMethod("put", Object.class, Object.class)
                    .invoke(propertyMap, "textures", property);
        }

        PROFILE_CACHE.putIfAbsent(hash, profile);
        return profile;
    }

    private static Field findField(@Nonnull Class<?> type, @Nonnull String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
