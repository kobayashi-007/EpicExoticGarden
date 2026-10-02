package io.github.thebusybiscuit.exoticgarden;

import io.github.thebusybiscuit.exoticgarden.items.BonemealableItem;
import io.github.thebusybiscuit.exoticgarden.items.Crook;
import io.github.thebusybiscuit.exoticgarden.items.CustomFood;
import io.github.thebusybiscuit.exoticgarden.items.ExoticGardenFruit;
import io.github.thebusybiscuit.exoticgarden.items.FoodRegistry;
import io.github.thebusybiscuit.exoticgarden.items.GrassSeeds;
import io.github.thebusybiscuit.exoticgarden.items.Kitchen;
import io.github.thebusybiscuit.exoticgarden.items.MagicalEssence;
import io.github.thebusybiscuit.exoticgarden.listeners.AndroidListener;
import io.github.thebusybiscuit.exoticgarden.listeners.PlantsListener;
import io.github.thebusybiscuit.slimefun5.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.api.researches.Research;
import io.github.thebusybiscuit.slimefun5.core.guide.wiki.WikiText;
import io.github.thebusybiscuit.slimefun5.core.guide.wiki.WikiTopic;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun5.implementation.items.food.Juice;
import io.github.thebusybiscuit.slimefun5.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedPlayerHead;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * ExoticGarden is a Slimefun addon that adds exotic plants, fruits,
 * vegetables and food items to the game.
 *
 * @author TheBusyBiscuit
 */
public class ExoticGarden extends JavaPlugin implements SlimefunAddon {

    public static ExoticGarden instance;

    private final File schematicsFolder = new File(getDataFolder(), "schematics");

    private final List<Berry> berries = new ArrayList<>();
    private final List<Tree> trees = new ArrayList<>();
    private final Map<String, ItemStack> items = new HashMap<>();
    private final Set<String> treeFruits = new HashSet<>();

    protected Config cfg;

    private NestedItemGroup nestedItemGroup;
    private ItemGroup mainItemGroup;
    private ItemGroup miscItemGroup;
    private ItemGroup foodItemGroup;
    private ItemGroup drinksItemGroup;
    private ItemGroup magicalItemGroup;
    private Kitchen kitchen;

    @Override
    public void onEnable() {
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        instance = this;
        cfg = new Config(this);

        registerItems();

        new AndroidListener(this);
        new PlantsListener(this);

        Slimefun.getItemTranslationService().registerTranslations(this);
        registerWiki();
    }

    private void registerWiki() {
        WikiText wiki = Slimefun.getWikiText();

        // Bucket this addon's items by their ItemGroup, preserving registration order.
        Map<ItemGroup, List<String>> buckets = new LinkedHashMap<>();
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            try {
                if (item.getAddon() != this) {
                    continue;
                }

                buckets.computeIfAbsent(item.getItemGroup(), key -> new ArrayList<>()).add(item.getId());
                registerItemPage(wiki, item.getId());
            } catch (Exception | LinkageError ignored) {
                // Skip items that fail to resolve their addon/group on this version.
            }
        }

        for (Map.Entry<ItemGroup, List<String>> entry : buckets.entrySet()) {
            ItemGroup group = entry.getKey();
            String groupKey = group.getKey().getKey();
            String topicId = "addon_exoticgarden_" + groupKey;

            wiki.registerTopic(new WikiTopic(topicId, getWikiName(groupKey), getWikiIcon(groupKey), getWikiTagline(groupKey)));
            wiki.setMechanic(topicId, getWikiMechanic(groupKey));
            wiki.setTopicItems(topicId, entry.getValue());
        }
    }

    @Nonnull
    private static String getWikiName(@Nonnull String groupKey) {
        switch (groupKey) {
            case "plants_and_fruits": return "Exotic Garden: Plants & Fruits";
            case "misc": return "Exotic Garden: Ingredients & Tools";
            case "food": return "Exotic Garden: Dishes";
            case "drinks": return "Exotic Garden: Drinks";
            case "magical_crops": return "Exotic Garden: Magical Crops";
            default: return "Exotic Garden";
        }
    }

    @Nonnull
    private static XMaterial getWikiIcon(@Nonnull String groupKey) {
        switch (groupKey) {
            case "plants_and_fruits": return XMaterial.MELON_SLICE;
            case "misc": return XMaterial.WHEAT;
            case "food": return XMaterial.COOKED_BEEF;
            case "drinks": return XMaterial.HONEY_BOTTLE;
            case "magical_crops": return XMaterial.BLAZE_POWDER;
            default: return XMaterial.MELON;
        }
    }

    @Nonnull
    private static String getWikiTagline(@Nonnull String groupKey) {
        switch (groupKey) {
            case "plants_and_fruits": return "&7Berries, fruit trees, bushes & crops";
            case "misc": return "&7Pantry ingredients & the harvesting Crook";
            case "food": return "&7Cook hearty dishes in the Kitchen";
            case "drinks": return "&7Juices, smoothies, teas & cocktails";
            case "magical_crops": return "&5Grow ores & resources from seeds";
            default: return "&7New crops, fruit and food";
        }
    }

    @Nonnull
    private static List<String> getWikiMechanic(@Nonnull String groupKey) {
        switch (groupKey) {
            case "plants_and_fruits":
                return Arrays.asList(
                    "&7The heart of Exotic Garden: dozens of new", "&7plants that grow naturally in the wild.", "",
                    "&aBushes &7(berries & crops) and &aSaplings &7(fruit", "&7trees) drop from tall grass - cut grass with a", "&7&oCrook &7to boost the drop rate.", "",
                    "&7Plant a bush on dirt and it slowly ripens; a", "&7sapling grows a custom tree that bears fruit", "&7blocks you can punch to harvest.", "",
                    "&eRight-click &7ripe bushes to harvest without", "&7destroying the plant - it regrows over time.", "",
                    "&7Fruits & berries are eaten raw or refined into", "&7juices and dishes. Click an item for details.");
            case "misc":
                return Arrays.asList(
                    "&7The pantry of Exotic Garden - the staple", "&7ingredients almost every recipe relies on.", "",
                    "&7Most are made in the &eGrind Stone &7or", "&e&oEnhanced Crafting Table&7: Mayo, Mustard,", "&7Cornmeal, Yeast, Molasses, Brown Sugar,", "&7Vegetable Oil, BBQ Sauce and more.", "",
                    "&7The &eCrook &7is a wooden-hoe tool that adds", "&b+25% &7sapling & seed drops when breaking grass.", "",
                    "&7Stock up here before heading to the Kitchen.", "&7Click an item for its exact recipe.");
            case "food":
                return Arrays.asList(
                    "&7Real cooking lives here. Build a &eKitchen", "&7multiblock to combine ingredients into meals.", "",
                    "&7&lBuilding the Kitchen:", "&7Bookshelf on top, Iron Trapdoor & Pressure", "&7Plate in front, a Furnace beside a Dispenser,", "&7and a Crafting Table - see the Kitchen item.", "",
                    "&7Drop ingredients into the &eDispenser&7, then", "&eright-click &7the trapdoor. The finished dish", "&7appears in the &eFurnace output slot&7.", "",
                    "&7From Pancakes to Lasagna to absurd Hot Dogs,", "&7each dish restores plenty of hunger.", "&7Click a dish for its ingredients.");
            case "drinks":
                return Arrays.asList(
                    "&7Thirsty work, gardening. Turn fruit into", "&7refreshing drinks that restore saturation.", "",
                    "&7Squeeze a single fruit in the &eJuicer &7for a", "&7basic &aJuice&7. Combine that juice with an", "&b&oIce Cube &7in an Enhanced Crafting Table for", "&7a chilled &aSmoothie&7.", "",
                    "&7Tea Leaves brew &aIced Teas&7; coconuts and", "&7pineapple make tropical cocktails like the", "&aPinacolada&7. Lemons become &aLemonade&7.", "",
                    "&7Drinks leave an empty bottle behind.", "&7Click a drink for its recipe.");
            case "magical_crops":
                return Arrays.asList(
                    "&5A magical twist: farm ores and resources", "&7instead of mining them.", "",
                    "&7Craft a &dMagical Essence &7for a resource,", "&7then craft it into a &dPlant&7. Plant the seed", "&7on dirt and it grows like any other crop.", "",
                    "&7Harvesting yields that resource - Coal, Iron,", "&7Gold, Diamond, Emerald, Redstone, Lapis,", "&7Ender Pearls, Quartz, Glowstone and more.", "",
                    "&7Higher tiers require the previous tier's plant", "&7in their recipe, so build the chain step by step.", "",
                    "&7Click a plant or essence for its recipe.");
            default:
                return Arrays.asList(
                    "&7New crops, fruit and food.", "",
                    "&7Click an item below for its recipe.");
        }
    }

    private void registerItemPage(@Nonnull WikiText wiki, @Nonnull String id) {
        List<String> page = getItemPage(id);
        if (page != null) {
            wiki.set(id, page);
        }
    }

    @Nullable
    private static List<String> getItemPage(@Nonnull String id) {
        if (id.endsWith("_BUSH")) {
            return Arrays.asList(
                "&7Plant this on &adirt &7and it slowly ripens.",
                "&7Once grown, &eright-click &7it to harvest the",
                "&7fruit - the bush stays and regrows over time.",
                "&7Drops from tall grass; use a &oCrook &7for more.");
        }

        if (id.endsWith("_SAPLING")) {
            return Arrays.asList(
                "&7Plant on dirt or grass to grow a custom",
                "&afruit tree&7. Bonemeal speeds it up.",
                "&7The tree bears &efruit blocks &7- punch them",
                "&7to harvest. Saplings drop from tall grass.");
        }

        if (id.endsWith("_ESSENCE")) {
            return Arrays.asList(
                "&dMagical Essence &7carries the seed of a",
                "&7resource. Craft it into the matching &dPlant&7,",
                "&7grow it on dirt, then harvest to reap the",
                "&7resource without ever touching a pickaxe.");
        }

        if (id.endsWith("_PLANT")) {
            return Arrays.asList(
                "&7A &dmagical crop&7. Plant on dirt and let it",
                "&7grow, then harvest for its resource.",
                "&7Higher tiers need the previous tier's plant",
                "&7in the recipe - build the chain in order.");
        }

        if (id.endsWith("_JUICE")) {
            return Arrays.asList(
                "&7Made by squeezing the fruit in a &eJuicer&7.",
                "&7Restores saturation and leaves an empty bottle.",
                "&7Use it as a base for smoothies and iced teas.");
        }

        if (id.endsWith("_SMOOTHIE")) {
            return Arrays.asList(
                "&7A chilled drink: combine the matching &eJuice",
                "&7with an &bIce Cube &7in an Enhanced Crafting",
                "&7Table. Restores more saturation than juice.");
        }

        if (id.endsWith("_ICED_TEA")) {
            return Arrays.asList(
                "&7Brew the fruit with an &bIce Cube &7and a",
                "&eTea Leaf &7in an Enhanced Crafting Table.",
                "&7A refreshing, hunger-restoring drink.");
        }

        if (id.endsWith("_PIE")) {
            return Arrays.asList(
                "&7A baked pie made from the fruit plus Egg,",
                "&7Sugar, Milk and Wheat Flour.",
                "&7Crafted in the &eEnhanced Crafting Table&7.");
        }

        if (id.endsWith("_JELLY_SANDWICH")) {
            return Arrays.asList(
                "&7Bread layered with fruit juice for a sweet,",
                "&7filling snack. Restores a lot of hunger.",
                "&7Crafted in the Enhanced Crafting Table.");
        }

        if (id.endsWith("_CHEESECAKE") || id.equals("CHEESECAKE")) {
            return Arrays.asList(
                "&7A creamy cake baked from Sugar, Flour,",
                "&7Heavy Cream and an Egg.",
                "&7Top a plain Cheesecake with fruit for a",
                "&7fancier dessert that heals even more.");
        }

        switch (id) {
            case "ICE_CUBE":
                return Arrays.asList(
                    "&7Frozen water, ground from &bIce &7in the",
                    "&eGrind Stone&7. A core ingredient for every",
                    "&7smoothie and iced tea in Exotic Garden.");
            case "CROOK":
                return Arrays.asList(
                    "&7A wooden-hoe tool for harvesting plants.",
                    "&7Breaking tall grass with it grants a",
                    "&b+25% &7chance for saplings, bushes and seeds.",
                    "&7Your main way to collect Exotic Garden plants.");
            case "GRASS_SEEDS":
                return Arrays.asList(
                    "&7Seeds that grow into tall grass on dirt,",
                    "&7giving you a renewable source of grass to",
                    "&7cut for saplings and bushes.");
            case "KITCHEN":
                return Arrays.asList(
                    "&7A multiblock for cooking dishes.",
                    "&7Drop ingredients in the &eDispenser&7, then",
                    "&eright-click &7the Iron Trapdoor.",
                    "&7The dish appears in the &eFurnace output&7.");
            case "MAYO":
                return Arrays.asList(
                    "&7Whipped from an Egg in the &eGrind Stone&7.",
                    "&7A spread used in many sandwiches and salads.");
            case "MUSTARD":
                return Arrays.asList(
                    "&7Ground from a &eMustard Seed&7.",
                    "&7A tangy condiment for sauces and sandwiches.");
            case "CORNMEAL":
                return Arrays.asList(
                    "&7Corn ground in the &eGrind Stone&7.",
                    "&7The base for tacos, burritos and tortillas.");
            case "YEAST":
                return Arrays.asList(
                    "&7Sugar fermented in water.",
                    "&7Needed to bake Bagels and other breads.");
            case "MOLASSES":
                return Arrays.asList(
                    "&7Boiled from Beetroot, Sugar Cane and water.",
                    "&7Used to make Brown Sugar.");
            case "BROWN_SUGAR":
                return Arrays.asList(
                    "&7Sugar enriched with &eMolasses&7.",
                    "&7A richer sweetener for curries and baking.");
            case "VEGETABLE_OIL":
                return Arrays.asList(
                    "&7Pressed from Beetroot Seeds and water.",
                    "&7A cooking fat used in curry dishes.");
            case "BBQ_SAUCE":
                return Arrays.asList(
                    "&7Tomato, Mustard, Salt and Sugar combined",
                    "&7into a smoky sauce for hot dogs and more.");
            case "COUNTRY_GRAVY":
                return Arrays.asList(
                    "&7Flour, Sugar and Black Pepper whisked",
                    "&7into a gravy. Pairs with Biscuits.");
            default:
                break;
        }

        if (isPlantProduce(id)) {
            return Arrays.asList(
                "&7A fresh Exotic Garden harvest.",
                "&7Eat it raw, juice it, or cook it into a dish.",
                "&7Grown from its matching bush or fruit tree.");
        }

        return Arrays.asList(
            "&7A prepared dish. Combine its ingredients in",
            "&7the &eKitchen &7multiblock to cook it.",
            "&7Restores a generous amount of hunger.");
    }

    private static boolean isPlantProduce(@Nonnull String id) {
        for (Berry berry : getBerries()) {
            if (id.equalsIgnoreCase(berry.getID())) {
                return true;
            }
        }

        for (Tree tree : getTrees()) {
            if (id.equalsIgnoreCase(tree.getFruitID())) {
                return true;
            }
        }

        return false;
    }

    private void registerItems() {
        nestedItemGroup = new NestedItemGroup(new NamespacedKey(this, "parent_category"), CustomItemStack.create(VersionedPlayerHead.getItemStack(VersionedPlayerHead.hashToBase64("847d73a91b52393f2c27e453fb89ab3d784054d414e390d58abd22512edd2b")), "\u00a7aExotic Garden"));
        nestedItemGroup.setTheme("food");
        mainItemGroup = new SubItemGroup(new NamespacedKey(this, "plants_and_fruits"), nestedItemGroup, CustomItemStack.create(VersionedPlayerHead.getItemStack(VersionedPlayerHead.hashToBase64("a5a5c4a0a16dabc9b1ec72fc83e23ac15d0197de61b138babca7c8a29c820")), "\u00a7aExotic Garden - Plants and Fruits"));
        miscItemGroup = new SubItemGroup(new NamespacedKey(this, "misc"), nestedItemGroup, CustomItemStack.create(VersionedPlayerHead.getItemStack(VersionedPlayerHead.hashToBase64("606be2df2122344bda479feece365ee0e9d5da276afa0e8ce8d848f373dd131")), "\u00a7aExotic Garden - Ingredients and Tools"));
        foodItemGroup = new SubItemGroup(new NamespacedKey(this, "food"), nestedItemGroup, CustomItemStack.create(VersionedPlayerHead.getItemStack(VersionedPlayerHead.hashToBase64("a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed")), "\u00a7aExotic Garden - Food"));
        drinksItemGroup = new SubItemGroup(new NamespacedKey(this, "drinks"), nestedItemGroup, CustomItemStack.create(VersionedPlayerHead.getItemStack(VersionedPlayerHead.hashToBase64("2a8f1f70e85825607d28edce1a2ad4506e732b4a5345a5ea6e807c4b313e88")), "\u00a7aExotic Garden - Drinks"));
        magicalItemGroup = new SubItemGroup(new NamespacedKey(this, "magical_crops"), nestedItemGroup, CustomItemStack.create(MaterialCompat.safe(XMaterial.BLAZE_POWDER), "\u00a75Exotic Garden - Magical Plants"));

        kitchen = new Kitchen(this, miscItemGroup);
        kitchen.register(this);
        Research kitchenResearch = new Research(new NamespacedKey(this, "kitchen"), 600, "Kitchen", 30);
        kitchenResearch.addItems(kitchen);
        kitchenResearch.register();

        // @formatter:off
        SlimefunItemStack iceCube = new SlimefunItemStack("ICE_CUBE", "9340bef2c2c33d113bac4e6a1a84d5ffcecbbfab6b32fa7a7f76195442bd1a2");
        new SlimefunItem(miscItemGroup, iceCube, RecipeType.GRIND_STONE, new ItemStack[] {new ItemStack(MaterialCompat.safe(XMaterial.ICE)), null, null, null, null, null, null, null, null}, copy(iceCube.item(), 4))
        .setGuideType("resources")
        .register(this);

        registerBerry("Grape", ChatColor.RED, Color.RED, PlantType.BUSH, "6ee97649bd999955413fcbf0b269c91be4342b10d0755bad7a17e95fcefdab0");
        registerBerry("Blueberry", ChatColor.BLUE, Color.BLUE, PlantType.BUSH, "a5a5c4a0a16dabc9b1ec72fc83e23ac15d0197de61b138babca7c8a29c820");
        registerBerry("Elderberry", ChatColor.RED, Color.FUCHSIA, PlantType.BUSH, "1e4883a1e22c324e753151e2ac424c74f1cc646eec8ea0db3420f1dd1d8b");
        registerBerry("Raspberry", ChatColor.LIGHT_PURPLE, Color.FUCHSIA, PlantType.BUSH, "8262c445bc2dd1c5bbc8b93f2482f9fdbef48a7245e1bdb361d4a568190d9b5");
        registerBerry("Blackberry", ChatColor.DARK_GRAY, Color.GRAY, PlantType.BUSH, "2769f8b78c42e272a669d6e6d19ba8651b710ab76f6b46d909d6a3d482754");
        registerBerry("Cranberry", ChatColor.RED, Color.FUCHSIA, PlantType.BUSH, "d5fe6c718fba719ff622237ed9ea6827d093effab814be2192e9643e3e3d7");
        registerBerry("Cowberry", ChatColor.RED, Color.FUCHSIA, PlantType.BUSH, "a04e54bf255ab0b1c498ca3a0ceae5c7c45f18623a5a02f78a7912701a3249");
        registerBerry("Strawberry", ChatColor.DARK_RED, Color.FUCHSIA, PlantType.FRUIT, "cbc826aaafb8dbf67881e68944414f13985064a3f8f044d8edfb4443e76ba");

        registerPlant("Tomato", ChatColor.DARK_RED, PlantType.FRUIT, "99172226d276070dc21b75ba25cc2aa5649da5cac745ba977695b59aebd");
        registerPlant("Lettuce", ChatColor.DARK_GREEN, PlantType.FRUIT, "477dd842c975d8fb03b1add66db8377a18ba987052161f22591e6a4ede7f5");
        registerPlant("Tea Leaf", ChatColor.GREEN, PlantType.DOUBLE_PLANT, "1514c8b461247ab17fe3606e6e2f4d363dccae9ed5bedd012b498d7ae8eb3");
        registerPlant("Cabbage", ChatColor.DARK_GREEN, PlantType.FRUIT, "fcd6d67320c9131be85a164cd7c5fcf288f28c2816547db30a3187416bdc45b");
        registerPlant("Sweet Potato", ChatColor.GOLD, PlantType.FRUIT, "3ff48578b6684e179944ab1bc75fec75f8fd592dfb456f6def76577101a66");
        registerPlant("Mustard Seed", ChatColor.YELLOW, PlantType.FRUIT, "ed53a42495fa27fb925699bc3e5f2953cc2dc31d027d14fcf7b8c24b467121f");
        registerPlant("Curry Leaf", ChatColor.DARK_GREEN, PlantType.DOUBLE_PLANT, "32af7fa8bdf3252f69863b204559d23bfc2b93d41437103437ab1935f323a31f");
        registerPlant("Onion", ChatColor.RED, PlantType.FRUIT, "6ce036e327cb9d4d8fef36897a89624b5d9b18f705384ce0d7ed1e1fc7f56");
        registerPlant("Garlic", ChatColor.RESET, PlantType.FRUIT, "3052d9c11848ebcc9f8340332577bf1d22b643c34c6aa91fe4c16d5a73f6d8");
        registerPlant("Cilantro", ChatColor.GREEN, PlantType.DOUBLE_PLANT, "16149196f3a8d6d6f24e51b27e4cb71c6bab663449daffb7aa211bbe577242");
        registerPlant("Black Pepper", ChatColor.DARK_GRAY, PlantType.DOUBLE_PLANT, "2342b9bf9f1f6295842b0efb591697b14451f803a165ae58d0dcebd98eacc");

        registerPlant("Corn", ChatColor.GOLD, PlantType.DOUBLE_PLANT, "9bd3802e5fac03afab742b0f3cca41bcd4723bee911d23be29cffd5b965f1");
        registerPlant("Pineapple", ChatColor.GOLD, PlantType.DOUBLE_PLANT, "d7eddd82e575dfd5b7579d89dcd2350c991f0483a7647cffd3d2c587f21");

        registerPlant("Red Bell Pepper", ChatColor.RED, PlantType.DOUBLE_PLANT, "65f7810414a2cee2bc1de12ecef7a4c89fc9b38e9d0414a90991241a5863705f");

        registerTree("Oak Apple", "cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633", "\u00a7c", Color.FUCHSIA, "Oak Apple Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Coconut", "6d27ded57b94cf715b048ef517ab3f85bef5a7be69f14b1573e14e7e42e2e8", "\u00a76", Color.MAROON, "Coconut Milk", false, MaterialCompat.safe(XMaterial.SAND));
        registerTree("Cherry", "c520766b87d2463c34173ffcd578b0e67d163d37a2d7c2e77915cd91144d40d1", "\u00a7c", Color.FUCHSIA, "Cherry Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Pomegranate", "cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633", "\u00a74", Color.RED, "Pomegranate Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Lemon", "957fd56ca15978779324df519354b6639a8d9bc1192c7c3de925a329baef6c", "\u00a7e", Color.YELLOW, "Lemon Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Plum", "69d664319ff381b4ee69a697715b7642b32d54d726c87f6440bf017a4bcd7", "\u00a75", Color.RED, "Plum Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Lime", "5a5153479d9f146a5ee3c9e218f5e7e84c4fa375e4f86d31772ba71f6468", "\u00a7a", Color.LIME, "Lime Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Orange", "65b1db547d1b7956d4511accb1533e21756d7cbc38eb64355a2626412212", "\u00a76", Color.ORANGE, "Orange Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Peach", "d3ba41fe82757871e8cbec9ded9acbfd19930d93341cf8139d1dfbfaa3ec2a5", "\u00a75", Color.RED, "Peach Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Pear", "2de28df844961a8eca8efb79ebb4ae10b834c64a66815e8b645aeff75889664b", "\u00a7a", Color.LIME, "Pear Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));
        registerTree("Dragon Fruit", "847d73a91b52393f2c27e453fb89ab3d784054d414e390d58abd22512edd2b", "\u00a7d", Color.FUCHSIA, "Dragon Fruit Juice", true, MaterialCompat.safe(XMaterial.DIRT), MaterialCompat.safe(XMaterial.GRASS_BLOCK));

        FoodRegistry.register(this, miscItemGroup, drinksItemGroup, foodItemGroup);

        registerMagicalPlant("Dirt", new ItemStack(MaterialCompat.safe(XMaterial.DIRT), 2), "1ab43b8c3d34f125e5a3f8b92cd43dfd14c62402c33298461d4d4d7ce2d3aea", 
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.DIRT)), null, new ItemStack(MaterialCompat.safe(XMaterial.DIRT)), new ItemStack(MaterialCompat.safe(XMaterial.WHEAT_SEEDS)), new ItemStack(MaterialCompat.safe(XMaterial.DIRT)), null, new ItemStack(MaterialCompat.safe(XMaterial.DIRT)), null});

        registerMagicalPlant("Coal", new ItemStack(MaterialCompat.safe(XMaterial.COAL), 2), "7788f5ddaf52c5842287b9427a74dac8f0919eb2fdb1b51365ab25eb392c47",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.COAL_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.COAL_ORE)), new ItemStack(MaterialCompat.safe(XMaterial.WHEAT_SEEDS)), new ItemStack(MaterialCompat.safe(XMaterial.COAL_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.COAL_ORE)), null});

        registerMagicalPlant("Iron", new ItemStack(MaterialCompat.safe(XMaterial.IRON_INGOT)), "db97bdf92b61926e39f5cddf12f8f7132929dee541771e0b592c8b82c9ad52d",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.IRON_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.IRON_BLOCK)), getItem("COAL_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.IRON_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.IRON_BLOCK)), null});

        registerMagicalPlant("Gold", SlimefunItems.GOLD_4K.item(), "e4df892293a9236f73f48f9efe979fe07dbd91f7b5d239e4acfd394f6eca",
        new ItemStack[] {null, SlimefunItems.GOLD_16K.item(), null, SlimefunItems.GOLD_16K.item(), getItem("IRON_PLANT"), SlimefunItems.GOLD_16K.item(), null, SlimefunItems.GOLD_16K.item(), null});

        registerMagicalPlant("Copper", copy(SlimefunItems.COPPER_DUST.item(), 8), "d4fc72f3d5ee66279a45ac9c63ac98969306227c3f4862e9c7c2a4583c097b8a",
        new ItemStack[] {null, SlimefunItems.COPPER_DUST.item(), null, SlimefunItems.COPPER_DUST.item(), getItem("GOLD_PLANT"), SlimefunItems.COPPER_DUST.item(), null, SlimefunItems.COPPER_DUST.item(), null});

        registerMagicalPlant("Aluminum", copy(SlimefunItems.ALUMINUM_DUST.item(), 8), "f4455341eaff3cf8fe6e46bdfed8f501b461fb6f6d2fe536be7d2bd90d2088aa",
        new ItemStack[] {null, SlimefunItems.ALUMINUM_DUST.item(), null, SlimefunItems.ALUMINUM_DUST.item(), getItem("IRON_PLANT"), SlimefunItems.ALUMINUM_DUST.item(), null, SlimefunItems.ALUMINUM_DUST.item(), null});

        registerMagicalPlant("Tin", copy(SlimefunItems.TIN_DUST.item(), 8), "6efb43ba2fe6959180ee7307f3f054715a34c0a07079ab73712547ffd753dedd",
        new ItemStack[] {null, SlimefunItems.TIN_DUST.item(), null, SlimefunItems.TIN_DUST.item(), getItem("IRON_PLANT"), SlimefunItems.TIN_DUST.item(), null, SlimefunItems.TIN_DUST.item(), null});

        registerMagicalPlant("Silver", copy(SlimefunItems.SILVER_DUST.item(), 8), "1dd968b1851aa7160d1cd9db7516a8e1bf7b7405e5245c5338aa895fe585f26c",
        new ItemStack[] {null, SlimefunItems.SILVER_DUST.item(), null, SlimefunItems.SILVER_DUST.item(), getItem("IRON_PLANT"), SlimefunItems.SILVER_DUST.item(), null, SlimefunItems.SILVER_DUST.item(), null});

        registerMagicalPlant("Lead", copy(SlimefunItems.LEAD_DUST.item(), 8), "93c3c418039c4b28b0da75a6d9b22712c7015432d4f4226d6cc0a77d54b64178",
        new ItemStack[] {null, SlimefunItems.LEAD_DUST.item(), null, SlimefunItems.LEAD_DUST.item(), getItem("IRON_PLANT"), SlimefunItems.LEAD_DUST.item(), null, SlimefunItems.LEAD_DUST.item(), null});

        registerMagicalPlant("Redstone", new ItemStack(MaterialCompat.safe(XMaterial.REDSTONE), 8), "e8deee5866ab199eda1bdd7707bdb9edd693444f1e3bd336bd2c767151cf2",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.REDSTONE_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.REDSTONE_BLOCK)), getItem("GOLD_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.REDSTONE_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.REDSTONE_BLOCK)), null});

        registerMagicalPlant("Lapis", new ItemStack(MaterialCompat.safe(XMaterial.LAPIS_LAZULI), 16), "2aa0d0fea1afaee334cab4d29d869652f5563c635253c0cbed797ed3cf57de0",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.LAPIS_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.LAPIS_ORE)), getItem("REDSTONE_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.LAPIS_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.LAPIS_ORE)), null});

        registerMagicalPlant("Ender", new ItemStack(MaterialCompat.safe(XMaterial.ENDER_PEARL), 4), "4e35aade81292e6ff4cd33dc0ea6a1326d04597c0e529def4182b1d1548cfe1",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.ENDER_PEARL)), null, new ItemStack(MaterialCompat.safe(XMaterial.ENDER_PEARL)), getItem("LAPIS_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.ENDER_PEARL)), null, new ItemStack(MaterialCompat.safe(XMaterial.ENDER_PEARL)), null});

        registerMagicalPlant("Quartz", new ItemStack(MaterialCompat.safe(XMaterial.QUARTZ), 8), "26de58d583c103c1cd34824380c8a477e898fde2eb9a74e71f1a985053b96",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.NETHER_QUARTZ_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.NETHER_QUARTZ_ORE)), getItem("ENDER_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.NETHER_QUARTZ_ORE)), null, new ItemStack(MaterialCompat.safe(XMaterial.NETHER_QUARTZ_ORE)), null});

        registerMagicalPlant("Diamond", new ItemStack(MaterialCompat.safe(XMaterial.DIAMOND)), "f88cd6dd50359c7d5898c7c7e3e260bfcd3dcb1493a89b9e88e9cbecbfe45949",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.DIAMOND)), null, new ItemStack(MaterialCompat.safe(XMaterial.DIAMOND)), getItem("QUARTZ_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.DIAMOND)), null, new ItemStack(MaterialCompat.safe(XMaterial.DIAMOND)), null});

        registerMagicalPlant("Emerald", new ItemStack(MaterialCompat.safe(XMaterial.EMERALD)), "4fc495d1e6eb54a386068c6cb121c5875e031b7f61d7236d5f24b77db7da7f",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.EMERALD)), null, new ItemStack(MaterialCompat.safe(XMaterial.EMERALD)), getItem("DIAMOND_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.EMERALD)), null, new ItemStack(MaterialCompat.safe(XMaterial.EMERALD)), null});

        if (Slimefun.getMinecraftVersion().isAtLeast(MinecraftVersion.MINECRAFT_1_16)) {
            registerMagicalPlant("Netherite", new ItemStack(MaterialCompat.safe(XMaterial.NETHERITE_INGOT)), "27957f895d7bc53423a35aac59d584b41cc30e040269c955e451fe680a1cc049", 
            new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.NETHERITE_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.NETHERITE_BLOCK)), getItem("EMERALD_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.NETHERITE_BLOCK)), null, new ItemStack(MaterialCompat.safe(XMaterial.NETHERITE_BLOCK)), null});
        }

        registerMagicalPlant("Glowstone", new ItemStack(MaterialCompat.safe(XMaterial.GLOWSTONE_DUST), 8), "65d7bed8df714cea063e457ba5e87931141de293dd1d9b9146b0f5ab383866",
        new ItemStack[] { null, new ItemStack(MaterialCompat.safe(XMaterial.GLOWSTONE)), null, new ItemStack(MaterialCompat.safe(XMaterial.GLOWSTONE)), getItem("REDSTONE_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.GLOWSTONE)), null, new ItemStack(MaterialCompat.safe(XMaterial.GLOWSTONE)), null });

        registerMagicalPlant("Obsidian", new ItemStack(MaterialCompat.safe(XMaterial.OBSIDIAN), 2), "7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.OBSIDIAN)), null, new ItemStack(MaterialCompat.safe(XMaterial.OBSIDIAN)), getItem("LAPIS_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.OBSIDIAN)), null, new ItemStack(MaterialCompat.safe(XMaterial.OBSIDIAN)), null});

        registerMagicalPlant("Slime", new ItemStack(MaterialCompat.safe(XMaterial.SLIME_BALL), 8), "90e65e6e5113a5187dad46dfad3d3bf85e8ef807f82aac228a59c4a95d6f6a",
        new ItemStack[] {null, new ItemStack(MaterialCompat.safe(XMaterial.SLIME_BALL)), null, new ItemStack(MaterialCompat.safe(XMaterial.SLIME_BALL)), getItem("ENDER_PLANT"), new ItemStack(MaterialCompat.safe(XMaterial.SLIME_BALL)), null, new ItemStack(MaterialCompat.safe(XMaterial.SLIME_BALL)), null});

        new Crook(miscItemGroup, new SlimefunItemStack("CROOK", MaterialCompat.safe(XMaterial.WOODEN_HOE)), RecipeType.ENHANCED_CRAFTING_TABLE,
        new ItemStack[] {new ItemStack(MaterialCompat.safe(XMaterial.STICK)), new ItemStack(MaterialCompat.safe(XMaterial.STICK)), null, null, new ItemStack(MaterialCompat.safe(XMaterial.STICK)), null, null, new ItemStack(MaterialCompat.safe(XMaterial.STICK)), null})
        .register(this);

        SlimefunItemStack grassSeeds = new SlimefunItemStack("GRASS_SEEDS", MaterialCompat.safe(XMaterial.PUMPKIN_SEEDS));
        new GrassSeeds(mainItemGroup, grassSeeds, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[] {null, null, null, null, new ItemStack(MaterialCompat.safe(XMaterial.SHORT_GRASS)), null, null, null, null})
        .register(this);
        // @formatter:on

        items.put("WHEAT_SEEDS", new ItemStack(MaterialCompat.safe(XMaterial.WHEAT_SEEDS)));
        items.put("PUMPKIN_SEEDS", new ItemStack(MaterialCompat.safe(XMaterial.PUMPKIN_SEEDS)));
        items.put("MELON_SEEDS", new ItemStack(MaterialCompat.safe(XMaterial.MELON_SEEDS)));

        for (Material sapling : Tag.SAPLINGS.getValues()) {
            items.put(sapling.name(), new ItemStack(sapling));
        }

        items.put("GRASS_SEEDS", grassSeeds.item());

        Iterator<String> iterator = items.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            cfg.setDefaultValue("grass-drops." + key, true);

            if (!cfg.getBoolean("grass-drops." + key)) {
                iterator.remove();
            }
        }

        cfg.save();

        for (Tree tree : ExoticGarden.getTrees()) {
            treeFruits.add(tree.getFruitID());
        }
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerTree(String name, String texture, String color, Color pcolor, String juice, boolean pie, Material... soil) {
        String id = name.toUpperCase(Locale.ROOT).replace(' ', '_');
        Tree tree = new Tree(id, texture, soil);
        trees.add(tree);

        SlimefunItemStack sapling = new SlimefunItemStack(id + "_SAPLING", MaterialCompat.safe(XMaterial.OAK_SAPLING));

        items.put(id + "_SAPLING", sapling.item());

        new BonemealableItem(mainItemGroup, sapling, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[] { null, null, null, null, new ItemStack(MaterialCompat.safe(XMaterial.SHORT_GRASS)), null, null, null, null }).register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(id, texture), ExoticGardenRecipeTypes.HARVEST_TREE, true, new ItemStack[] { null, null, null, null, getItem(id + "_SAPLING"), null, null, null, null }).register(this);

        if (pcolor != null) {
            new Juice(drinksItemGroup, new SlimefunItemStack(juice.toUpperCase().replace(" ", "_"), pcolor, new PotionEffect(PotionEffectType.SATURATION, 6, 0)), RecipeType.JUICER, new ItemStack[] { getItem(id), null, null, null, null, null, null, null, null }).setGuideType("food").register(this);
        }

        if (pie) {
            new CustomFood(foodItemGroup, new SlimefunItemStack(id + "_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79"), new ItemStack[] { getItem(id), new ItemStack(MaterialCompat.safe(XMaterial.EGG)), new ItemStack(MaterialCompat.safe(XMaterial.SUGAR)), new ItemStack(MaterialCompat.safe(XMaterial.MILK_BUCKET)), SlimefunItems.WHEAT_FLOUR.item(), null, null, null, null }, 13).register(this);
        }

        if (!new File(schematicsFolder, id + "_TREE.schematic").exists()) {
            saveSchematic(id + "_TREE");
        }
    }

    private void saveSchematic(@Nonnull String id) {
        try (InputStream input = getClass().getResourceAsStream("/schematics/" + id + ".schematic")) {
            try (FileOutputStream output = new FileOutputStream(new File(schematicsFolder, id + ".schematic"))) {
                byte[] buffer = new byte[1024];
                int len;

                while ((len = input.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e, () -> "Failed to load file: \"" + id + ".schematic\"");
        }
    }

    public void registerBerry(String name, ChatColor color, Color potionColor, PlantType type, String texture) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        Berry berry = new Berry(upperCase, type, texture);
        berries.add(berry);

        SlimefunItemStack sfi = new SlimefunItemStack(upperCase + "_BUSH", MaterialCompat.safe(XMaterial.OAK_SAPLING));

        items.put(upperCase + "_BUSH", sfi.item());

        new BonemealableItem(mainItemGroup, sfi, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[] { null, null, null, null, new ItemStack(MaterialCompat.safe(XMaterial.SHORT_GRASS)), null, null, null, null }).register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(upperCase, texture), ExoticGardenRecipeTypes.HARVEST_BUSH, true, new ItemStack[] { null, null, null, null, getItem(upperCase + "_BUSH"), null, null, null, null }).register(this);

        new Juice(drinksItemGroup, new SlimefunItemStack(upperCase + "_JUICE", potionColor, new PotionEffect(PotionEffectType.SATURATION, 6, 0)), RecipeType.JUICER, new ItemStack[] { getItem(upperCase), null, null, null, null, null, null, null, null }).setGuideType("food").register(this);

        new Juice(drinksItemGroup, new SlimefunItemStack(upperCase + "_SMOOTHIE", potionColor, new PotionEffect(PotionEffectType.SATURATION, 10, 0)), RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] { getItem(upperCase + "_JUICE"), getItem("ICE_CUBE"), null, null, null, null, null, null, null }).setGuideType("food").register(this);

        new CustomFood(foodItemGroup, new SlimefunItemStack(upperCase + "_JELLY_SANDWICH", "8c8a939093ab1cde6677faf7481f311e5f17f63d58825f0e0c174631fb0439"), new ItemStack[] { null, new ItemStack(MaterialCompat.safe(XMaterial.BREAD)), null, null, getItem(upperCase + "_JUICE"), null, null, new ItemStack(MaterialCompat.safe(XMaterial.BREAD)), null }, 16).register(this);

        new CustomFood(foodItemGroup, new SlimefunItemStack(upperCase + "_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79"), new ItemStack[] { getItem(upperCase), new ItemStack(MaterialCompat.safe(XMaterial.EGG)), new ItemStack(MaterialCompat.safe(XMaterial.SUGAR)), new ItemStack(MaterialCompat.safe(XMaterial.MILK_BUCKET)), SlimefunItems.WHEAT_FLOUR.item(), null, null, null, null }, 13).register(this);
    }

    @Nullable
    private static ItemStack getItem(@Nonnull String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        return item != null ? item.getItem() : null;
    }

    public void registerPlant(String name, ChatColor color, PlantType type, String texture) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        String enumStyle = upperCase.replace(' ', '_');

        Berry berry = new Berry(enumStyle, type, texture);
        berries.add(berry);

        SlimefunItemStack bush = new SlimefunItemStack(enumStyle + "_BUSH", MaterialCompat.safe(XMaterial.OAK_SAPLING));
        items.put(upperCase + "_BUSH", bush.item());

        new BonemealableItem(mainItemGroup, bush, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[] { null, null, null, null, new ItemStack(MaterialCompat.safe(XMaterial.SHORT_GRASS)), null, null, null, null })
            .register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(enumStyle, texture), ExoticGardenRecipeTypes.HARVEST_BUSH, true, new ItemStack[] { null, null, null, null, getItem(enumStyle + "_BUSH"), null, null, null, null }).register(this);
    }

    private void registerMagicalPlant(String name, ItemStack item, String texture, ItemStack[] recipe) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        String enumStyle = upperCase.replace(' ', '_');

        SlimefunItemStack essence = new SlimefunItemStack(enumStyle + "_ESSENCE", MaterialCompat.safe(XMaterial.BLAZE_POWDER));

        Berry berry = new Berry(essence.item(), upperCase + "_ESSENCE", PlantType.ORE_PLANT, texture);
        berries.add(berry);

        new BonemealableItem(magicalItemGroup, new SlimefunItemStack(enumStyle + "_PLANT", MaterialCompat.safe(XMaterial.OAK_SAPLING)), RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
            .setGuideType("magic")
            .register(this);

        MagicalEssence magicalEssence = new MagicalEssence(magicalItemGroup, essence);

        magicalEssence.setRecipeOutput(item.clone());
        magicalEssence.register(this);
    }

    @Nullable
    public static ItemStack harvestPlant(@Nonnull Block block) {
        SlimefunItem item = BlockStorage.check(block);

        if (item == null) {
            return null;
        }

        for (Berry berry : getBerries()) {
            if (item.getId().equalsIgnoreCase(berry.getID())) {
                switch (berry.getType()) {
                    case ORE_PLANT:
                    case DOUBLE_PLANT:
                        Block plant = block;

                        if (Tag.LEAVES.isTagged(block.getType())) {
                            block = block.getRelative(BlockFace.UP);
                        } else {
                            plant = block.getRelative(BlockFace.DOWN);
                        }

                        BlockStorage.deleteLocationInfoUnsafely(block.getLocation(), false);
                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.OAK_LEAVES));
                        block.setType(MaterialCompat.safe(XMaterial.AIR));

                        plant.setType(MaterialCompat.safe(XMaterial.OAK_SAPLING));
                        BlockStorage.deleteLocationInfoUnsafely(plant.getLocation(), false);
                        BlockStorage.store(plant, getItem(berry.toBush()));
                        return berry.getItem().clone();
                    default:
                        block.setType(MaterialCompat.safe(XMaterial.OAK_SAPLING));
                        BlockStorage.deleteLocationInfoUnsafely(block.getLocation(), false);
                        BlockStorage.store(block, getItem(berry.toBush()));
                        return berry.getItem().clone();
                }
            }
        }

        return null;
    }

    public void harvestFruit(Block fruit) {
        Location loc = fruit.getLocation();
        SlimefunItem check = BlockStorage.check(loc);

        if (check == null) {
            return;
        }

        if (treeFruits.contains(check.getId())) {
            BlockStorage.clearBlockInfo(loc);
            ItemStack fruits = check.getItem().clone();
            fruit.getWorld().playEffect(loc, Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.OAK_LEAVES));
            fruit.getWorld().dropItemNaturally(loc, fruits);
            fruit.setType(MaterialCompat.safe(XMaterial.AIR));
        }
    }

    public static ExoticGarden getInstance() {
        return instance;
    }

    public File getSchematicsFolder() {
        return schematicsFolder;
    }

    public static Kitchen getKitchen() {
        return instance.kitchen;
    }

    public static List<Tree> getTrees() {
        return instance.trees;
    }

    public static List<Berry> getBerries() {
        return instance.berries;
    }

    public static Map<String, ItemStack> getGrassDrops() {
        return instance.items;
    }

    public Config getCfg() {
        return cfg;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/Slimefun5/ExoticGarden/issues";
    }

    private static ItemStack copy(ItemStack item, int amount) {
        ItemStack copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }

}


