/*
 * Food/drink data derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: item registry, recipes, hunger values; added ICE_CUBE, WHEAT_FLOUR, etc.
 */
package com.epic.exoticgarden.content;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.item.Category;
import com.epic.exoticgarden.item.Items;
import com.epic.exoticgarden.recipe.DefaultRecipes;
import com.epic.exoticgarden.recipe.RecipeGroup;
import com.epic.exoticgarden.recipe.Tokens;

/**
 * Registers all dishes, drinks and pantry ingredients.
 *
 * <p>Ported from the original ExoticGarden FoodRegistry; every Slimefun machine recipe
 * (Kitchen / Juicer / Grind Stone / Enhanced Crafting Table) is now an ordinary vanilla
 * workbench recipe, and Slimefun-provided ingredients (flour, butter, ...) are regular
 * EpicExoticGarden items.</p>
 */
public final class FoodContent {

    private FoodContent() {}

    private static ItemStack v(com.epic.exoticgarden.compat.XMat m) {
        return Materials.stack(m);
    }

    private static ItemStack get(String id) {
        return Items.get(id);
    }

    /**
     * Registers an edible head-food with its default workbench recipe.
     */
    private static void food(String id, String texture, int hunger, ItemStack... recipe) {
        Items.registerFood(id, Category.FOOD, texture, hunger);
        DefaultRecipes.addShapeless(RecipeGroup.DISHES, id, hunger, 1, Tokens.fromStacks(recipe));
    }

    /**
     * Registers an edible head-food in the drinks category.
     */
    private static void drinkFood(String id, String texture, int hunger, ItemStack... recipe) {
        Items.registerFood(id, Category.DRINKS, texture, hunger);
        DefaultRecipes.addShapeless(RecipeGroup.DRINKS, id, hunger, 1, Tokens.fromStacks(recipe));
    }

    private static void pantry(String id, String texture, ItemStack... recipe) {
        Items.registerHead(id, Category.MISC, texture);
        DefaultRecipes.addShapeless(RecipeGroup.INGREDIENTS, id, 0, 1, Tokens.fromStacks(recipe));
    }

    private static void juice(String id, Color color, int hunger, ItemStack... recipe) {
        // Saturation is instant: drinking applies (amplifier + 1) food points once, so the
        // potency goes into the amplifier and the duration is irrelevant (1 tick).
        Items.registerDrink(id, Category.DRINKS, color,
                new PotionEffect(PotionEffectType.SATURATION, 1, Math.max(0, hunger - 1)));
        DefaultRecipes.addShapeless(RecipeGroup.DRINKS, id, hunger, 1, Tokens.fromStacks(recipe));
    }

    public static void register() {
        // ---- Drinks ----
        juice("LIME_SMOOTHIE", Color.LIME, 10, get("LIME_JUICE"), get("ICE_CUBE"));
        juice("TOMATO_JUICE", Color.FUCHSIA, 6, get("TOMATO"));
        juice("WINE", Color.RED, 10, get("GRAPE"), v(Materials.SUGAR));
        juice("LEMON_ICED_TEA", Color.YELLOW, 13, get("LEMON"), get("ICE_CUBE"), get("TEA_LEAF"));
        juice("RASPBERRY_ICED_TEA", Color.FUCHSIA, 13, get("RASPBERRY"), get("ICE_CUBE"), get("TEA_LEAF"));
        juice("PEACH_ICED_TEA", Color.FUCHSIA, 13, get("PEACH"), get("ICE_CUBE"), get("TEA_LEAF"));
        juice("STRAWBERRY_ICED_TEA", Color.FUCHSIA, 13, get("STRAWBERRY"), get("ICE_CUBE"), get("TEA_LEAF"));
        juice("CHERRY_ICED_TEA", Color.FUCHSIA, 13, get("CHERRY"), get("ICE_CUBE"), get("TEA_LEAF"));
        juice("THAI_TEA", Color.RED, 14, get("TEA_LEAF"), v(Materials.SUGAR), get("HEAVY_CREAM"), get("COCONUT_MILK"));

        food("PUMPKIN_BREAD", "f3487d457f9062d787a3e6ce1c4664bf7402ec67dd111256f19b38ce4f670", 8,
                v(Materials.PUMPKIN), v(Materials.SUGAR), get("WHEAT_FLOUR"));

        // ---- Pantry staples ----
        pantry("MAYO", "7f8d536c8c2c2596bcc1709590a9d7e33061c56e658974cd81bb832ea4d8842",
                v(Materials.EGG));
        pantry("MUSTARD", "9b9e99621b9773b29e375e62c6495ff1ac847f85b29816c2eb77b587874ba62",
                get("MUSTARD_SEED"));
        pantry("BBQ_SAUCE", "a86f19bf23d248e662c9c8b7fa15efb8a1f1d5bdacd3b8625a9b59e93ac8a",
                get("TOMATO"), get("MUSTARD"), get("SALT"), v(Materials.SUGAR));
        pantry("VEGETABLE_OIL", "2acb28fb8a310443af02c7a1283ace95a9906b2e0e6f3636597edbe8cad4e",
                v(Materials.BEETROOT_SEEDS), v(Materials.WATER_BUCKET));
        pantry("CORNMEAL", "606be2df2122344bda479feece365ee0e9d5da276afa0e8ce8d848f373dd131",
                get("CORN"));
        pantry("YEAST", "606be2df2122344bda479feece365ee0e9d5da276afa0e8ce8d848f373dd131",
                v(Materials.SUGAR), v(Materials.WATER_BUCKET));
        pantry("MOLASSES", "f21d7b155edf440cb87ec94487cba64e8d128171eb1187c26d5ffe58bd794c",
                v(Materials.BEETROOT), v(Materials.SUGAR_CANE), v(Materials.WATER_BUCKET));
        pantry("BROWN_SUGAR", "964d4247278e1498374aa6b0e47368fe4f138abc94e583e8839965fbe241be",
                v(Materials.SUGAR), get("MOLASSES"));
        pantry("COUNTRY_GRAVY", "f21fa9439bfd8384464146f9c67ebd4c5fbf4196924892627eadf3bce1ff",
                get("WHEAT_FLOUR"), v(Materials.SUGAR), get("BLACK_PEPPER"));

        food("CHOCOLATE_BAR", "819f948d17718adace5dd6e050c586229653fef645d7113ab94d17b639cc466", 3,
                v(Materials.COCOA_BEANS), get("HEAVY_CREAM"));
        food("POTATO_SALAD", "1fe92e11a67b56935446a214caa3723d29e6db56c55fa8d43179a8a3176c6c1", 12,
                v(Materials.BAKED_POTATO), get("MAYO"), get("ONION"), v(Materials.BOWL));
        food("CHICKEN_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 11,
                v(Materials.COOKED_CHICKEN), get("MAYO"), v(Materials.BREAD));
        food("FISH_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 11,
                v(Materials.COOKED_COD), get("MAYO"), v(Materials.BREAD));
        food("BAGEL", "502e92f13de3bee69228c384478e761230681e5fce9bda195daeaf8484139331", 4,
                get("YEAST"), get("WHEAT_FLOUR"));
        food("EGG_SALAD", "1fe92e11a67b56935446a214caa3723d29e6db56c55fa8d43179a8a3176c6c1", 12,
                v(Materials.EGG), get("MAYO"), v(Materials.BOWL));
        food("TOMATO_SOUP", "76366f17428a4990126844f74a02dbf5524f35be1323f8fab0bf61a57ff41de3", 11,
                v(Materials.BOWL), get("TOMATO"));
        food("STRAWBERRY_SALAD", "1fe92e11a67b56935446a214caa3723d29e6db56c55fa8d43179a8a3176c6c1", 10,
                v(Materials.BOWL), get("STRAWBERRY"), get("LETTUCE"), get("TOMATO"));
        food("GRAPE_SALAD", "1fe92e11a67b56935446a214caa3723d29e6db56c55fa8d43179a8a3176c6c1", 10,
                v(Materials.BOWL), get("GRAPE"), get("LETTUCE"), get("TOMATO"));
        food("CHICKEN_CURRY", "d09e0dd5489f03efdc8083088f521b82946cdec98fc1c94c4e09792e4735184a", 16,
                get("CILANTRO"), v(Materials.COOKED_CHICKEN), get("BROWN_SUGAR"), get("CURRY_LEAF"),
                get("VEGETABLE_OIL"), get("CURRY_LEAF"), get("ONION"), v(Materials.BOWL), get("GARLIC"));
        food("COCONUT_CHICKEN_CURRY", "d09e0dd5489f03efdc8083088f521b82946cdec98fc1c94c4e09792e4735184a", 19,
                get("COCONUT"), get("COCONUT"), get("CHICKEN_CURRY"));
        food("BISCUIT", "ef094456fd794b6531fc6dec6f396b680b9536002063e11ce24d0a74b0b7d885", 4,
                get("WHEAT_FLOUR"), get("BUTTER"));
        food("BISCUITS_GRAVY", "28bbb835e22d9ec62e22411b8e015138d5597283ad36e618fe44ba5f1a6b60fd", 13,
                get("COUNTRY_GRAVY"), get("COUNTRY_GRAVY"), get("COUNTRY_GRAVY"),
                get("BISCUIT"), get("BISCUIT"), get("BISCUIT"), v(Materials.BOWL));
        food("CHEESECAKE", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 16,
                v(Materials.SUGAR), get("WHEAT_FLOUR"), get("HEAVY_CREAM"), v(Materials.EGG));
        food("CHERRY_CHEESECAKE", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 17,
                get("CHEESECAKE"), get("CHERRY"));
        food("BLUEBERRY_CHEESECAKE", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 17,
                get("CHEESECAKE"), get("BLUEBERRY"));
        food("PUMPKIN_CHEESECAKE", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 17,
                get("CHEESECAKE"), v(Materials.PUMPKIN));
        food("SWEETENED_PEAR_CHEESECAKE", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 18,
                get("CHEESECAKE"), v(Materials.SUGAR), get("PEAR"));
        food("BLACKBERRY_COBBLER", "c6c36523c2d11b8c8ea2e992291c52a654760ec72dcc32da2cb63616481ee", 12,
                v(Materials.SUGAR), get("BLACKBERRY"), get("WHEAT_FLOUR"));
        food("PAVLOVA", "6365b61e79fcb913bc860f4ec635d4a6ab1b74bfab62fb6ea6d89a16aa841", 18,
                get("LEMON"), get("STRAWBERRY"), v(Materials.SUGAR), v(Materials.EGG), get("HEAVY_CREAM"));
        // Corn on the cob reuses a vanilla golden-carrot material lookalike.
        Items.registerFood("CORN_ON_THE_COB", Category.FOOD, Materials.GOLDEN_CARROT, 9);
        DefaultRecipes.addShapeless(RecipeGroup.DISHES, "CORN_ON_THE_COB", 9, 1,
                Tokens.fromStacks(get("BUTTER"), get("CORN")));

        food("CREAMED_CORN", "9174b34c549eed8bafe727618bab6821afcb1787b5decd1eecd6c213e7e7c6d", 8,
                get("HEAVY_CREAM"), get("CORN"), v(Materials.BOWL));

        // Bacon yields 3 per cooked porkchop.
        Items.registerFood("BACON", Category.FOOD, "e7ba22d5df21e821a6de4b8c9d373a3aa187d8ae74f288a82d2b61f272e5", 3);
        DefaultRecipes.addShapeless(RecipeGroup.DISHES, "BACON", 3, 3,
                Tokens.fromStacks(v(Materials.COOKED_PORKCHOP)));

        food("SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 19,
                v(Materials.BREAD), get("MAYO"), v(Materials.COOKED_BEEF), get("TOMATO"), get("LETTUCE"));
        food("BLT", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 18,
                v(Materials.BREAD), v(Materials.COOKED_PORKCHOP), get("TOMATO"), get("LETTUCE"));
        food("LEAFY_CHICKEN_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 13,
                get("CHICKEN_SANDWICH"), get("LETTUCE"));
        food("LEAFY_FISH_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 13,
                get("FISH_SANDWICH"), get("LETTUCE"));
        food("HAMBURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 10,
                v(Materials.BREAD), v(Materials.COOKED_BEEF));
        food("CHEESEBURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 13,
                get("HAMBURGER"), get("CHEESE"));
        food("BACON_CHEESEBURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 17,
                get("CHEESEBURGER"), get("BACON"));
        food("DELUXE_CHEESEBURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 16,
                get("CHEESEBURGER"), get("LETTUCE"), get("TOMATO"));
        food("GARLIC_BREAD", "a33fa7d3e63b280a5d7e2bb09332dff86b17decd2b09eccdd62da5265597f74d", 10,
                get("GARLIC"), v(Materials.BREAD));
        food("GARLIC_CHEESE_BREAD", "a33fa7d3e63b280a5d7e2bb09332dff86b17decd2b09eccdd62da5265597f74d", 13,
                get("CHEESE"), get("GARLIC"), v(Materials.BREAD));
        food("CARROT_CAKE", "f9136514f342e7c5208a1422506a866158ef84d2b249220139e8bf6032e193", 12,
                v(Materials.CARROT), get("WHEAT_FLOUR"), v(Materials.SUGAR), v(Materials.EGG));
        food("CHICKEN_BURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 10,
                v(Materials.BREAD), v(Materials.COOKED_CHICKEN));
        food("CHICKEN_CHEESEBURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 13,
                get("CHICKEN_BURGER"), get("CHEESE"));
        food("BACON_BURGER", "cdadf1744433e1c79d1d59d2777d939de159a24cf57e8a61c82bc4fe3777553c", 10,
                v(Materials.BREAD), get("BACON"));
        food("BACON_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 19,
                v(Materials.BREAD), get("BACON"), get("MAYO"), get("TOMATO"), get("LETTUCE"));
        food("TACO", "98ced74a22021a535f6bce21c8c632b273dc2d9552b71a38d57269b3538cf", 18,
                get("CORNMEAL"), v(Materials.COOKED_BEEF), get("LETTUCE"), get("TOMATO"), get("CHEESE"));
        food("FISH_TACO", "98ced74a22021a535f6bce21c8c632b273dc2d9552b71a38d57269b3538cf", 18,
                get("CORNMEAL"), v(Materials.COOKED_COD), get("LETTUCE"), get("TOMATO"), get("CHEESE"));
        food("STREET_TACO", "1ad7c0a04f1485c7a3ef261a48ee83b2f1aa701ab11f3fc911e0366a9b97e", 18,
                get("CORNMEAL"), v(Materials.COOKED_BEEF), get("CILANTRO"), get("ONION"));
        food("JAMMY_DODGER", "1d00dfb3a57c068a0cc7b624d8d8852070435d2634c0e5da9cbbab46174af0df", 10,
                get("BISCUIT"), get("RASPBERRY_JUICE"), get("BISCUIT"));
        food("PANCAKES", "347f4f5a74c6691280cd80e7148b49b2ce17dcf64fd55368627f5d92a976a6a8", 12,
                get("WHEAT_FLOUR"), v(Materials.SUGAR), get("BUTTER"), v(Materials.EGG), v(Materials.EGG));
        food("BLUEBERRY_PANCAKES", "347f4f5a74c6691280cd80e7148b49b2ce17dcf64fd55368627f5d92a976a6a8", 13,
                get("PANCAKES"), get("BLUEBERRY"));
        if (Materials.SWEET_BERRIES != null) {
            Items.registerFood("SWEET_BERRY_PANCAKES", Category.FOOD,
                    "e44ca99e308a186b30281b2017c44189acafb591152f81feea96fecbe57", 13);
            DefaultRecipes.addShapeless(RecipeGroup.DISHES, "SWEET_BERRY_PANCAKES", 13, 1,
                    Tokens.fromStacks(get("PANCAKES"), v(Materials.SWEET_BERRIES)));
        }
        food("FRIES", "563b8aeaf1df11488efc9bd303c233a87ccba3b33f7fba9c2fecaee9567f053", 12,
                v(Materials.POTATO), get("SALT"));
        food("POPCORN", "1497b147cfae52205597f72e3c4ef52512e9677020e4b4fa7512c3c6acdd8c1", 8,
                get("CORN"), get("BUTTER"));
        food("SWEET_POPCORN", "1497b147cfae52205597f72e3c4ef52512e9677020e4b4fa7512c3c6acdd8c1", 12,
                get("CORN"), get("BUTTER"), v(Materials.SUGAR));
        food("SALTY_POPCORN", "1497b147cfae52205597f72e3c4ef52512e9677020e4b4fa7512c3c6acdd8c1", 12,
                get("CORN"), get("BUTTER"), get("SALT"));
        food("SHEPARDS_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", 16,
                get("CABBAGE"), v(Materials.CARROT), get("WHEAT_FLOUR"), v(Materials.COOKED_BEEF), get("TOMATO"));
        food("CHICKEN_POT_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", 17,
                v(Materials.COOKED_CHICKEN), v(Materials.CARROT), get("WHEAT_FLOUR"), v(Materials.POTATO));
        food("CHOCOLATE_CAKE", "9119fca4f28a755d37fbe5dcf6d8c3ef50fe394c1a7850bc7e2b71ee78303c4c", 17,
                get("CHOCOLATE_BAR"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), v(Materials.EGG));
        food("CREAM_COOKIE", "dfd71e20fc50abf0de2ef7decfc01ce27ad51955759e072ceaab96355f594f0", 12,
                get("CHOCOLATE_BAR"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("HEAVY_CREAM"));
        food("BLUEBERRY_MUFFIN", "83794c736fc76e45706830325b95969466d86f8d7b28fce8edb2c75e2ab25c", 13,
                get("BLUEBERRY"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("HEAVY_CREAM"), v(Materials.EGG));
        food("PUMPKIN_MUFFIN", "83794c736fc76e45706830325b95969466d86f8d7b28fce8edb2c75e2ab25c", 13,
                v(Materials.PUMPKIN), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("HEAVY_CREAM"), v(Materials.EGG));
        food("CHOCOLATE_CHIP_MUFFIN", "83794c736fc76e45706830325b95969466d86f8d7b28fce8edb2c75e2ab25c", 13,
                get("CHOCOLATE_BAR"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("HEAVY_CREAM"), v(Materials.EGG));
        food("BOSTON_CREAM_PIE", "dfd71e20fc50abf0de2ef7decfc01ce27ad51955759e072ceaab96355f594f0", 9,
                get("CHOCOLATE_BAR"), get("HEAVY_CREAM"), get("BISCUIT"));
        food("HOT_DOG", "33f2d7d7a8b1b969142881eb5a87e737b5f75fb808b9a157adddb2c6aec382", 10,
                v(Materials.COOKED_PORKCHOP), v(Materials.BREAD));
        food("BACON_WRAPPED_CHEESE_FILLED_HOT_DOG", "33f2d7d7a8b1b969142881eb5a87e737b5f75fb808b9a157adddb2c6aec382", 17,
                get("BACON"), get("HOT_DOG"), get("BACON"), get("CHEESE"));
        food("BBQ_BACON_WRAPPED_HOT_DOG", "33f2d7d7a8b1b969142881eb5a87e737b5f75fb808b9a157adddb2c6aec382", 17,
                get("BACON"), get("HOT_DOG"), get("BACON"), get("BBQ_SAUCE"));
        food("BBQ_DOUBLE_BACON_WRAPPED_HOT_DOG_IN_A_TORTILLA_WITH_CHEESE", "33f2d7d7a8b1b969142881eb5a87e737b5f75fb808b9a157adddb2c6aec382", 20,
                get("BACON"), get("BBQ_SAUCE"), get("BACON"), get("BACON"), v(Materials.COOKED_PORKCHOP),
                get("BACON"), get("CORNMEAL"), get("CHEESE"), get("CORNMEAL"));

        drinkFood("SWEETENED_TEA", "d8e94ddd769a5bea748376b4ec7383fd36d267894d7c3bee011e8e4f5fcd7", 6,
                get("TEA_LEAF"), v(Materials.SUGAR));
        drinkFood("HOT_CHOCOLATE", "411511bdd55bcb82803c8039f1c155fd43062636e23d4d46c4d761c04d22c2", 8,
                get("CHOCOLATE_BAR"), get("HEAVY_CREAM"));
        drinkFood("PINACOLADA", "2a8f1f70e85825607d28edce1a2ad4506e732b4a5345a5ea6e807c4b313e88", 14,
                get("PINEAPPLE"), get("ICE_CUBE"), get("COCONUT_MILK"));
        food("CHOCOLATE_STRAWBERRY", "6d4ed7c73ac2853dfcaa9ca789fb18da1d47b17ad68b2da748dbd11de1a49ef", 5,
                get("CHOCOLATE_BAR"), get("STRAWBERRY"));
        juice("LEMONADE", Color.YELLOW, 8, get("LEMON_JUICE"), v(Materials.SUGAR));
        food("SWEET_POTATO_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", 13,
                get("SWEET_POTATO"), v(Materials.EGG), get("HEAVY_CREAM"), get("WHEAT_FLOUR"));
        food("LAMINGTON", "9119fca4f28a755d37fbe5dcf6d8c3ef50fe394c1a7850bc7e2b71ee78303c4c", 18,
                get("CHOCOLATE_BAR"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("COCONUT"));
        food("WAFFLES", "347f4f5a74c6691280cd80e7148b49b2ce17dcf64fd55368627f5d92a976a6a8", 12,
                get("WHEAT_FLOUR"), v(Materials.EGG), v(Materials.SUGAR), get("BUTTER"));
        food("CLUB_SANDWICH", "a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed", 19,
                v(Materials.BREAD), get("MAYO"), get("BACON"), get("TOMATO"), get("LETTUCE"), get("MUSTARD"));
        food("BURRITO", "a387a621e266186e60683392eb274ebb225b04868ab959177d9dc181d8f286", 18,
                get("CORNMEAL"), v(Materials.COOKED_BEEF), get("LETTUCE"), get("TOMATO"), get("HEAVY_CREAM"), get("CHEESE"));
        food("CHICKEN_BURRITO", "a387a621e266186e60683392eb274ebb225b04868ab959177d9dc181d8f286", 18,
                get("CORNMEAL"), v(Materials.COOKED_CHICKEN), get("LETTUCE"), get("TOMATO"), get("HEAVY_CREAM"), get("CHEESE"));
        food("GRILLED_SANDWICH", "baee84d19c85aff796c88abda21ec4c92c655e2d67b72e5e77b5aa5e99ed", 11,
                v(Materials.BREAD), v(Materials.COOKED_PORKCHOP), get("CHEESE"));
        food("LASAGNA", "03a3574a848f36ae37121e9058aa61c12a261ee5a3716f6d8269e11e19e37", 17,
                get("TOMATO"), get("CHEESE"), get("WHEAT_FLOUR"), get("TOMATO"), get("CHEESE"), v(Materials.COOKED_BEEF));
        food("ICE_CREAM", "95366ca17974892e4fd4c7b9b18feb11f05ba2ec47aa5035c81a9533b28", 16,
                get("HEAVY_CREAM"), get("ICE_CUBE"), v(Materials.SUGAR), v(Materials.COCOA_BEANS), get("STRAWBERRY"));
        juice("PINEAPPLE_JUICE", Color.ORANGE, 6, get("PINEAPPLE"));
        juice("PINEAPPLE_SMOOTHIE", Color.ORANGE, 10, get("PINEAPPLE_JUICE"), get("ICE_CUBE"));
        food("TIRAMISU", "169091d288022c7b0eb6d3e3f44b0fea7f2c069f497491a1dcab587eb1d56d4", 16,
                get("HEAVY_CREAM"), v(Materials.EGG), v(Materials.SUGAR), v(Materials.COCOA_BEANS), v(Materials.EGG));
        food("TIRAMISU_WITH_STRAWBERRIES", "169091d288022c7b0eb6d3e3f44b0fea7f2c069f497491a1dcab587eb1d56d4", 18,
                get("TIRAMISU"), get("STRAWBERRY"));
        food("TIRAMISU_WITH_RASPBERRIES", "169091d288022c7b0eb6d3e3f44b0fea7f2c069f497491a1dcab587eb1d56d4", 18,
                get("TIRAMISU"), get("RASPBERRY"));
        food("TIRAMISU_WITH_BLACKBERRIES", "169091d288022c7b0eb6d3e3f44b0fea7f2c069f497491a1dcab587eb1d56d4", 18,
                get("TIRAMISU"), get("BLACKBERRY"));
        food("CHOCOLATE_PEAR_CAKE", "9119fca4f28a755d37fbe5dcf6d8c3ef50fe394c1a7850bc7e2b71ee78303c4c", 19,
                get("CHOCOLATE_BAR"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("PEAR"), v(Materials.EGG));
        food("APPLE_PEAR_CAKE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", 18,
                get("OAK_APPLE"), v(Materials.SUGAR), get("WHEAT_FLOUR"), get("BUTTER"), get("PEAR"), v(Materials.EGG));
        food("STUFFED_RED_BELL_PEPPER", "b6c98b410123b0944422303798fc2db8cea0feeb09d0da40f5361b59498f3e8b", 14,
                get("RED_BELL_PEPPER"), get("ONION"), get("GARLIC"), get("TOMATO"));
    }
}
