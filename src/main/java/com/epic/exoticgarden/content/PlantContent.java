/*
 * Item data derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: item registry, recipes and hunger values; added STONE_CROOK, pantry items.
 */
package com.epic.exoticgarden.content;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.item.Category;
import com.epic.exoticgarden.item.Items;
import com.epic.exoticgarden.plant.Berry;
import com.epic.exoticgarden.plant.PlantType;
import com.epic.exoticgarden.plant.Tree;
import com.epic.exoticgarden.recipe.DefaultRecipes;
import com.epic.exoticgarden.recipe.RecipeDef;
import com.epic.exoticgarden.recipe.RecipeGroup;
import com.epic.exoticgarden.recipe.Tokens;

/**
 * Registers every plant, bush, fruit tree, magical crop, pantry staple and tool, together with
 * their vanilla workbench recipes.
 *
 * <p>Ported 1:1 from the original ExoticGarden {@code registerItems()} routine, with all Slimefun
 * dependencies removed: custom machines became workbench recipes and Slimefun-provided ingredients
 * became ordinary EpicExoticGarden items.</p>
 */
public final class PlantContent {

    private PlantContent() {}

    private static final List<Berry> BERRIES = new ArrayList<>();
    private static final List<Tree> TREES = new ArrayList<>();
    private static final Map<String, ItemStack> GRASS_DROPS = new LinkedHashMap<>();

    private static final String PIE_TEXTURE = "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79";
    private static final String SANDWICH_TEXTURE = "8c8a939093ab1cde6677faf7481f311e5f17f63d58825f0e0c174631fb0439";

    @Nonnull
    public static List<Berry> getBerries() {
        return BERRIES;
    }

    @Nonnull
    public static List<Tree> getTrees() {
        return TREES;
    }

    @Nonnull
    public static Map<String, ItemStack> getGrassDrops() {
        return GRASS_DROPS;
    }

    private static ItemStack v(XMat m) {
        return m == null ? null : Materials.stack(m);
    }

    private static ItemStack get(String id) {
        return Items.get(id);
    }

    public static void register(@Nonnull FileConfiguration config) {
        registerPantry();
        registerBerriesAndPlants();
        registerTrees();
        registerMagicalPlants();
        registerTools();
        rebuildGrassDrops(config);
    }

    // ------------------------------------------------------------------
    // Pantry staples (formerly Slimefun items, recreated as vanilla items)
    // ------------------------------------------------------------------

    private static void registerPantry() {
        // Ice Cube: 4 ice -> 4 ice cubes (player head)
        Items.registerHead("ICE_CUBE", Category.MISC,
                "9340bef2c2c33d113bac4e6a1a84d5ffcecbbfab6b32fa7a7f76195442bd1a2");
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "ICE_CUBE", 0, 4, Tokens.fromStacks(
                v(Materials.ICE), v(Materials.ICE), v(Materials.ICE), v(Materials.ICE)));

        // Wheat flour looks like sugar; salt like bone meal/white dye.
        Items.register("WHEAT_FLOUR", Category.MISC, Materials.SUGAR);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "WHEAT_FLOUR", 0, 1,
                Tokens.fromStacks(v(Materials.WHEAT)));

        Items.register("SALT", Category.MISC, Materials.WHITE_DYE);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "SALT", 0, 1,
                Tokens.fromStacks(v(Materials.WATER_BUCKET)));

        // Heavy cream is a plain white potion-bottle item (no drinkable effect).
        ItemStack cream = Materials.stack(Materials.POTION);
        PotionMeta creamMeta = (PotionMeta) cream.getItemMeta();
        try {
            creamMeta.setColor(Color.WHITE);
        } catch (NoSuchMethodError ignored) {
            // 1.12 supports setColor anyway; this is just a defensive guard.
        }
        cream.setItemMeta(creamMeta);
        Items.register("HEAVY_CREAM", Category.MISC, cream, false);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "HEAVY_CREAM", 0, 1,
                Tokens.fromStacks(v(Materials.MILK_BUCKET)));

        // Butter/cheese use fitting vanilla block stand-ins.
        Items.register("BUTTER", Category.MISC, Materials.HONEYCOMB);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "BUTTER", 0, 1,
                Tokens.fromStacks(get("HEAVY_CREAM"), get("SALT")));

        Items.register("CHEESE", Category.MISC, Materials.SPONGE);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, "CHEESE", 0, 1,
                Tokens.fromStacks(v(Materials.MILK_BUCKET), get("SALT")));
    }

    // ------------------------------------------------------------------
    // Berries & crops
    // ------------------------------------------------------------------

    private static void registerBerriesAndPlants() {
        // Berries: bush + fruit + juice + smoothie + jelly sandwich + pie
        registerBerry("Grape", Color.RED, PlantType.BUSH,
                "6ee97649bd999955413fcbf0b269c91be4342b10d0755bad7a17e95fcefdab0");
        registerBerry("Blueberry", Color.BLUE, PlantType.BUSH,
                "a5a5c4a0a16dabc9b1ec72fc83e23ac15d0197de61b138babca7c8a29c820");
        registerBerry("Elderberry", Color.FUCHSIA, PlantType.BUSH,
                "1e4883a1e22c324e753151e2ac424c74f1cc646eec8ea0db3420f1dd1d8b");
        registerBerry("Raspberry", Color.FUCHSIA, PlantType.BUSH,
                "8262c445bc2dd1c5bbc8b93f248b09127f74f9dbef48a7245e1bdb361d4a568190d9b5");
        registerBerry("Blackberry", Color.GRAY, PlantType.BUSH,
                "2769f8b78c42e272a669d6e6d19ba8651b710ab76f6b46d909d6a3d482754");
        registerBerry("Cranberry", Color.FUCHSIA, PlantType.BUSH,
                "d5fe6c718fba719ff622237ed9ea6827d093effab814be2192e9643e3e3d7");
        registerBerry("Cowberry", Color.FUCHSIA, PlantType.BUSH,
                "a04e54bf255ab0b1c498ca3a0ceae5c7c45f18623a5a02f78a7912701a3249");
        registerBerry("Strawberry", Color.FUCHSIA, PlantType.FRUIT,
                "cbc826aaafb8dbf67881e68944414f13985064a3f8f044d8edfb4443e76ba");

        // Simple crops: bush (sapling look) + edible fruit
        registerPlant("Tomato", PlantType.FRUIT,
                "99172226d276070dc21b75ba25cc2aa5649da5cac745ba977695b59aebd");
        registerPlant("Lettuce", PlantType.FRUIT,
                "477dd842c975d8fb03b1add66db8377a18ba987052161f22591e6a4ede7f5");
        registerPlant("Tea Leaf", PlantType.DOUBLE_PLANT,
                "1514c8b461247ab17fe3606e6e2f4d363dccae9ed5bedd012b498d7ae8eb3");
        registerPlant("Cabbage", PlantType.FRUIT,
                "fcd6d67320c9131be85a164cd7c5fcf288f28c2816547db30a3187416bdc45b");
        registerPlant("Sweet Potato", PlantType.FRUIT,
                "3ff48578b6684e179944ab1bc75fec75f8fd592dfb456f6def76577101a66");
        registerPlant("Mustard Seed", PlantType.FRUIT,
                "ed53a42495fa27fb925699bc3e5f2953cc2dc31d027d14fcf7b8c24b467121f");
        registerPlant("Curry Leaf", PlantType.DOUBLE_PLANT,
                "32af7fa8bdf3252f69863b204559d23bfc2b93d41437ab1935f323a31f");
        registerPlant("Onion", PlantType.FRUIT,
                "6ce036e327cb9d4d8fef36897a89624b5d9b18f705384ce0d7ed1e1fc7f56");
        registerPlant("Garlic", PlantType.FRUIT,
                "3052d9c11848ebcc9f8340332577bf1d22b643c34c6aa91fe4c16d5a73f6d8");
        registerPlant("Cilantro", PlantType.DOUBLE_PLANT,
                "16149196f3a8d6d6f24e51b27e4cb71c6bab663449daffb7aa211bbe577242");
        registerPlant("Black Pepper", PlantType.DOUBLE_PLANT,
                "2342b9bf9f1f6295842b0efb591697b14451f803a165ae58d0dcebd98eacc");
        registerPlant("Corn", PlantType.DOUBLE_PLANT,
                "9bd3802e5fac03afab742b0f3cca41bcd4723bee911d23be29cffd5b965f1");
        registerPlant("Pineapple", PlantType.DOUBLE_PLANT,
                "d7eddd82e575dfd5b7579d89dcd2350c991f0483a7647cffd3d2c587f21");
        registerPlant("Red Bell Pepper", PlantType.DOUBLE_PLANT,
                "65f7810414a2cee2bc1de12ecef7a4c89fc9b38e9d0414a90991241a5863705f");
    }

    private static void registerBerry(String name, Color potionColor, PlantType type, String texture) {
        String id = name.toUpperCase(Locale.ROOT);
        BERRIES.add(new Berry(id, type, texture));

        Items.registerPlantable(id + "_BUSH", Category.PLANTS_AND_FRUITS);
        DefaultRecipes.addNoRecipe(RecipeGroup.PLANTS, id + "_BUSH", 0);
        Items.registerFood(id, Category.PLANTS_AND_FRUITS, texture, 2);
        DefaultRecipes.addNoRecipe(RecipeGroup.FRUITS, id, 2);

        // Saturation is instant: amplifier = hunger - 1 (juice 6 food, smoothie 10 food).
        Items.registerDrink(id + "_JUICE", Category.DRINKS, potionColor,
                new PotionEffect(PotionEffectType.SATURATION, 1, 5));
        DefaultRecipes.addShapeless(RecipeGroup.DRINKS, id + "_JUICE", 6, 1,
                Tokens.fromStacks(get(id)));

        Items.registerDrink(id + "_SMOOTHIE", Category.DRINKS, potionColor,
                new PotionEffect(PotionEffectType.SATURATION, 1, 9));
        DefaultRecipes.addShapeless(RecipeGroup.DRINKS, id + "_SMOOTHIE", 10, 1,
                Tokens.fromStacks(get(id + "_JUICE"), get("ICE_CUBE")));

        Items.registerFood(id + "_JELLY_SANDWICH", Category.FOOD, SANDWICH_TEXTURE, 16);
        DefaultRecipes.addShapeless(RecipeGroup.DISHES, id + "_JELLY_SANDWICH", 16, 1, Tokens.fromStacks(
                v(Materials.BREAD), get(id + "_JUICE"), v(Materials.BREAD)));

        Items.registerFood(id + "_PIE", Category.FOOD, PIE_TEXTURE, 13);
        DefaultRecipes.addShapeless(RecipeGroup.DISHES, id + "_PIE", 13, 1, Tokens.fromStacks(
                get(id), v(Materials.EGG), v(Materials.SUGAR), v(Materials.MILK_BUCKET), get("WHEAT_FLOUR")));
    }

    private static void registerPlant(String name, PlantType type, String texture) {
        String id = name.toUpperCase(Locale.ROOT).replace(' ', '_');
        BERRIES.add(new Berry(id, type, texture));

        Items.registerPlantable(id + "_BUSH", Category.PLANTS_AND_FRUITS);
        DefaultRecipes.addNoRecipe(RecipeGroup.PLANTS, id + "_BUSH", 0);
        Items.registerFood(id, Category.PLANTS_AND_FRUITS, texture, 2);
        DefaultRecipes.addNoRecipe(RecipeGroup.FRUITS, id, 2);
    }

    // ------------------------------------------------------------------
    // Fruit trees
    // ------------------------------------------------------------------

    private static void registerTrees() {
        registerTree("Oak Apple", "cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633",
                ChatColor.RED, Color.FUCHSIA, "Oak Apple Juice", true);
        registerTree("Coconut", "6d27ded57b94cf715b048ef517ab3f85bef5a7be69f14b1573e14e7e42e2e8",
                ChatColor.GOLD, Color.MAROON, "Coconut Milk", false, Materials.SAND);
        registerTree("Cherry", "c520766b87d2463c34173ffcd578b0e67d163d37a2d7c2e77915cd91144d40d1",
                ChatColor.RED, Color.FUCHSIA, "Cherry Juice", true);
        registerTree("Pomegranate", "cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633",
                ChatColor.DARK_RED, Color.RED, "Pomegranate Juice", true);
        registerTree("Lemon", "957fd56ca15978779324df519354b6639a8d9bc1192c7c3de925a329baef6c",
                ChatColor.YELLOW, Color.YELLOW, "Lemon Juice", true);
        registerTree("Plum", "69d664319ff381b4ee69a697715b7642b32d54d726c87f6440bf017a4bcd7",
                ChatColor.DARK_PURPLE, Color.RED, "Plum Juice", true);
        registerTree("Lime", "5a5153479d9f146a5ee3c9e218f5e7e84c4fa375e4f86d31772ba71f6468",
                ChatColor.GREEN, Color.LIME, "Lime Juice", true);
        registerTree("Orange", "65b1db547d1b7956d4511accb1533e21756d7cbc38eb64355a2626412212",
                ChatColor.GOLD, Color.ORANGE, "Orange Juice", true);
        registerTree("Peach", "d3ba41fe82757871e8cbec9ded9acbfd19930d93341cf8139d1dfbfaa3ec2a5",
                ChatColor.DARK_PURPLE, Color.RED, "Peach Juice", true);
        registerTree("Pear", "2de28df844961a8eca8efb79ebb4ae10b834c64a66815e8b645aeff75889664b",
                ChatColor.GREEN, Color.LIME, "Pear Juice", true);
        registerTree("Dragon Fruit", "847d73a91b52393f2c27e453fb89ab3d784054d414e390d58abd22512edd2b",
                ChatColor.LIGHT_PURPLE, Color.FUCHSIA, "Dragon Fruit Juice", true);
    }

    private static void registerTree(String name, String texture, ChatColor color, Color potionColor,
                                     String juice, boolean pie, XMat... extraSoil) {
        String id = name.toUpperCase(Locale.ROOT).replace(' ', '_');

        List<XMat> soils = new ArrayList<>();
        if (extraSoil.length == 0) {
            soils.add(Materials.DIRT);
            soils.add(Materials.GRASS_BLOCK);
        } else {
            for (XMat soil : extraSoil) {
                soils.add(soil);
            }
        }
        Material[] soilMaterials = new Material[soils.size()];
        for (int i = 0; i < soils.size(); i++) {
            soilMaterials[i] = soils.get(i).material;
        }
        TREES.add(new Tree(id, texture, soilMaterials));

        Items.registerPlantable(id + "_SAPLING", Category.PLANTS_AND_FRUITS);
        DefaultRecipes.addNoRecipe(RecipeGroup.PLANTS, id + "_SAPLING", 0);
        Items.registerFood(id, Category.PLANTS_AND_FRUITS, texture, 2);
        DefaultRecipes.addNoRecipe(RecipeGroup.FRUITS, id, 2);

        String juiceId = juice.toUpperCase(Locale.ROOT).replace(' ', '_');
        // Saturation is instant: amplifier = hunger - 1 (6 food).
        Items.registerDrink(juiceId, Category.DRINKS, potionColor,
                new PotionEffect(PotionEffectType.SATURATION, 1, 5));
        DefaultRecipes.addShapeless(RecipeGroup.DRINKS, juiceId, 6, 1,
                Tokens.fromStacks(get(id)));

        if (pie) {
            Items.registerFood(id + "_PIE", Category.FOOD, PIE_TEXTURE, 13);
            DefaultRecipes.addShapeless(RecipeGroup.DISHES, id + "_PIE", 13, 1, Tokens.fromStacks(
                    get(id), v(Materials.EGG), v(Materials.SUGAR), v(Materials.MILK_BUCKET), get("WHEAT_FLOUR")));
        }
    }

    // ------------------------------------------------------------------
    // Magical crops
    // ------------------------------------------------------------------

    private static void registerMagicalPlants() {
        registerMagicalPlant("Dirt", v(Materials.DIRT), 2, "1ab43b8c3d34f125e5a3f8b92cd43dfd14c62402c33298461d4d4d7ce2d3aea",
                ring(v(Materials.DIRT), v(Materials.WHEAT_SEEDS)));
        registerMagicalPlant("Coal", v(Materials.COAL), 2, "7788f5ddaf52c5842287b9427a74dac8f0919eb2fdb1b51365ab25eb392c47",
                ring(v(Materials.COAL_ORE), v(Materials.WHEAT_SEEDS)));
        registerMagicalPlant("Iron", v(Materials.IRON_INGOT), 1, "db97bdf92b61926e39f5cddf12f8f7132929dee541771e0b592c8b82c9ad52d",
                ring(v(Materials.IRON_BLOCK), get("COAL_PLANT")));
        // Gold dusts from Slimefun are replaced with vanilla gold ingots.
        registerMagicalPlant("Gold", v(Materials.GOLD_INGOT), 1, "e4df892293a9236f73f48f9efe979fe07dbd91f7b5d239e4acfd394f6eca",
                ring(v(Materials.GOLD_INGOT), get("IRON_PLANT")));
        // Copper only exists as a vanilla material on 1.17+.
        if (Materials.COPPER_INGOT != null) {
            registerMagicalPlant("Copper", v(Materials.COPPER_INGOT), 8,
                    "d4fc72f3d5ee66279a45ac9c63ac98969306227c3f4862e9c7c2a4583c097b8a",
                    ring(v(Materials.COPPER_INGOT), get("GOLD_PLANT")));
        }
        // Aluminum / Tin / Silver / Lead relied on Slimefun dusts and have no vanilla
        // equivalent on the 1.12 floor, so they are intentionally not registered.
        registerMagicalPlant("Redstone", v(Materials.REDSTONE), 8, "e8deee5866ab199eda1bdd7707bdb9edd693444f1e3bd336bd2c767151cf2",
                ring(v(Materials.REDSTONE_BLOCK), get("GOLD_PLANT")));
        registerMagicalPlant("Lapis", v(Materials.LAPIS_LAZULI), 16, "2aa0d0fea1afaee334cab4d29d869652f5563c635253c0cbed797ed3cf57de0",
                ring(v(Materials.LAPIS_ORE), get("REDSTONE_PLANT")));
        registerMagicalPlant("Ender", v(Materials.ENDER_PEARL), 4, "4e35aade81292e6ff4cd33dc0ea6a1326d04597c0e529def4182b1d1548cfe1",
                ring(v(Materials.ENDER_PEARL), get("LAPIS_PLANT")));
        registerMagicalPlant("Quartz", v(Materials.QUARTZ), 8, "26de58d583c103c1cd34824380c8a477e898fde2eb9a74e71f1a985053b96",
                ring(v(Materials.NETHER_QUARTZ_ORE), get("ENDER_PLANT")));
        registerMagicalPlant("Diamond", v(Materials.DIAMOND), 1, "f88cd6dd50359c7d5898c7c7e3e260bfcd3dcb1493a89b9e88e9cbecbfe45949",
                ring(v(Materials.DIAMOND), get("QUARTZ_PLANT")));
        registerMagicalPlant("Emerald", v(Materials.EMERALD), 1, "4fc495d1e6eb54a386068c6cb121c5875e031b7f61d7236d5f24b77db7da7f",
                ring(v(Materials.EMERALD), get("DIAMOND_PLANT")));
        if (Materials.NETHERITE_INGOT != null && Materials.NETHERITE_BLOCK != null) {
            registerMagicalPlant("Netherite", v(Materials.NETHERITE_INGOT), 1,
                    "27957f895d7bc53423a35aac59d584b41cc30e040269c955e451fe680a1cc049",
                    ring(v(Materials.NETHERITE_BLOCK), get("EMERALD_PLANT")));
        }
        registerMagicalPlant("Glowstone", v(Materials.GLOWSTONE_DUST), 8, "65d7bed8df714cea063e457ba5e87931141de293dd1d9b9146b0f5ab383866",
                ring(v(Materials.GLOWSTONE), get("REDSTONE_PLANT")));
        registerMagicalPlant("Obsidian", v(Materials.OBSIDIAN), 2, "7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5",
                ring(v(Materials.OBSIDIAN), get("LAPIS_PLANT")));
        registerMagicalPlant("Slime", v(Materials.SLIME_BALL), 8, "90e65e6e5113a5187dad46dfad3d3bf85e8ef807f82aac228a59c4a95d6f6a",
                ring(v(Materials.SLIME_BALL), get("ENDER_PLANT")));
    }

    /**
     * Builds the classic shaped "ring": four outer ingredients around a center item.
     */
    private static RecipeDef ring(ItemStack outer, ItemStack center) {
        String outerToken = Tokens.fromStack(outer);
        String centerToken = Tokens.fromStack(center);
        return RecipeDef.shaped(1,
                DefaultRecipes.rows(" O ", "OCO", " O "),
                DefaultRecipes.matrix("O", outerToken, "C", centerToken));
    }

    private static void registerMagicalPlant(String name, ItemStack result, int amount, String texture,
                                             RecipeDef plantRecipe) {
        if (result == null || result.getType() == null) {
            return;
        }

        String id = name.toUpperCase(Locale.ROOT).replace(' ', '_');

        // The plant is a placeable sapling-stage crop with a workbench recipe.
        Items.registerPlantable(id + "_PLANT", Category.MAGICAL);
        DefaultRecipes.add(RecipeGroup.MAGICAL_CROPS, id + "_PLANT", 0, plantRecipe);

        // The essence is what a ripe magical crop drops; 8 essences convert back to the resource.
        Items.register(id + "_ESSENCE", Category.MAGICAL, Materials.BLAZE_POWDER);
        DefaultRecipes.addNoRecipe(RecipeGroup.MAGICAL_CROPS, id + "_ESSENCE", 0);
        BERRIES.add(new Berry(get(id + "_ESSENCE"), id + "_ESSENCE", PlantType.ORE_PLANT, texture));

        // Conversion recipe keyed by the vanilla result's modern material token
        // (8 essences -> resource): IRON_INGOT, LAPIS_LAZULI, ENDER_PEARL, ...
        List<String> conversion = new ArrayList<>();
        conversion.add(id + "_ESSENCE:8");
        DefaultRecipes.addShapeless(RecipeGroup.MAGICAL_CROPS, Tokens.fromStack(result), 0, amount, conversion);
    }

    // ------------------------------------------------------------------
    // Tools
    // ------------------------------------------------------------------

    private static void registerTools() {
        // The Crook is a wooden hoe with a diagonal two-stick shaped recipe.
        Items.register("CROOK", Category.MISC, Materials.WOODEN_HOE);
        DefaultRecipes.addShaped(RecipeGroup.TOOLS, "CROOK", 0, 1,
                DefaultRecipes.rows("SS ", " S ", " S "),
                DefaultRecipes.matrix("S", "STICK", "S", "STICK"));

        // Stone Crook: harvests a 3x3 area of ripe plants and shares the Crook's sapling bonus.
        Items.register("STONE_CROOK", Category.MISC, Materials.STONE_HOE);
        DefaultRecipes.addShaped(RecipeGroup.TOOLS, "STONE_CROOK", 0, 1,
                DefaultRecipes.rows("CCC", "CXC", "CCC"),
                DefaultRecipes.matrix("C", "COBBLESTONE", "X", "CROOK"));

        // Grass seeds look like pumpkin seeds and turn dirt into grass on right click.
        Items.register("GRASS_SEEDS", Category.PLANTS_AND_FRUITS, Materials.PUMPKIN_SEEDS);
        DefaultRecipes.addNoRecipe(RecipeGroup.TOOLS, "GRASS_SEEDS", 0);
    }

    // ------------------------------------------------------------------
    // Tall-grass drop pool (config-driven)
    // ------------------------------------------------------------------

    /**
     * Rebuilds the tall-grass drop pool. Every pool entry gets a {@code grass-drops.<id>}
     * config default of {@code true}; entries disabled in the config are excluded.
     */
    public static void rebuildGrassDrops(FileConfiguration config) {
        Map<String, ItemStack> pool = new LinkedHashMap<>();

        pool.put("WHEAT_SEEDS", v(Materials.WHEAT_SEEDS));
        pool.put("PUMPKIN_SEEDS", v(Materials.PUMPKIN_SEEDS));
        pool.put("MELON_SEEDS", v(Materials.MELON_SEEDS));

        pool.put("OAK_SAPLING", v(Materials.OAK_SAPLING));
        pool.put("SPRUCE_SAPLING", v(Materials.SPRUCE_SAPLING));
        pool.put("BIRCH_SAPLING", v(Materials.BIRCH_SAPLING));
        pool.put("JUNGLE_SAPLING", v(Materials.JUNGLE_SAPLING));
        pool.put("ACACIA_SAPLING", v(Materials.ACACIA_SAPLING));
        pool.put("DARK_OAK_SAPLING", v(Materials.DARK_OAK_SAPLING));

        pool.put("GRASS_SEEDS", get("GRASS_SEEDS"));

        for (Berry berry : BERRIES) {
            if (berry.getType() == PlantType.ORE_PLANT) {
                continue;
            }
            pool.put(berry.toBush(), get(berry.toBush()));
        }

        for (Tree tree : TREES) {
            pool.put(tree.getSapling(), get(tree.getSapling()));
        }

        GRASS_DROPS.clear();
        for (Map.Entry<String, ItemStack> entry : pool.entrySet()) {
            config.addDefault("grass-drops." + entry.getKey(), true);
            if (config.getBoolean("grass-drops." + entry.getKey(), true) && entry.getValue() != null) {
                GRASS_DROPS.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
