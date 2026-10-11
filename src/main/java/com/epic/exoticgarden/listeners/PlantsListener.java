/*
 * Derived from ExoticGarden (GPLv3) — https://github.com/Slimefun-Addon-Community/ExoticGarden
 * Modified 2026: replaced Slimefun BlockStorage with SQLite+PDC, added debounce,
 *                piston migration, STONE_CROOK, durability cost, and paired block checks.
 */
package com.epic.exoticgarden.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.compat.BlockDataAccess;
import com.epic.exoticgarden.compat.BlockIds;
import com.epic.exoticgarden.compat.BlockTags;
import com.epic.exoticgarden.compat.ItemTags;
import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.SkullUtil;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.content.PlantContent;
import com.epic.exoticgarden.core.Blocks;
import com.epic.exoticgarden.item.Items;
import com.epic.exoticgarden.plant.Berry;
import com.epic.exoticgarden.plant.PlantType;
import com.epic.exoticgarden.plant.Tree;
import com.epic.exoticgarden.schematics.Schematic;

/**
 * Growth, world generation, harvesting and destruction of all EpicExoticGarden plants.
 *
 * <p>Ported from the original ExoticGarden {@code PlantsListener} with Slimefun, PaperLib and
 * protection-manager dependencies removed.</p>
 *
 * @author TheBusyBiscuit
 */
public class PlantsListener implements Listener {

    private static final BlockFace[] FACES = {
        BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
        BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };

    /**
     * Minimum interval (ms) between two right-click harvests by the same player.
     *
     * <p>Tools such as the wooden-hoe Crook cause the client to fire two
     * {@code RIGHT_CLICK_BLOCK} packets in quick succession during a single click
     * (the "use item" swing plus the block interaction). Without a cooldown the
     * second packet would harvest a neighbouring plant, producing an apparent
     * 2x1 range harvest. Human clicks are spaced well above this threshold so
     * normal rapid harvesting is unaffected.</p>
     */
    private static final long HARVEST_COOLDOWN_MS = 200L;

    private final Map<UUID, Long> lastHarvest = new ConcurrentHashMap<>();

    private final EpicExoticGarden plugin;

    public PlantsListener(EpicExoticGarden plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // BlockExplodeEvent only exists on 1.9+; its listener class must be loaded conditionally.
        if (classExists("org.bukkit.event.block.BlockExplodeEvent")) {
            plugin.getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), plugin);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Bonemeal / natural growth
    // ------------------------------------------------------------------

    @EventHandler
    public void onGrow(StructureGrowEvent e) {
        Block block = e.getLocation().getBlock();
        String id = Blocks.idAt(block);
        if (id == null) {
            return;
        }

        for (Tree tree : PlantContent.getTrees()) {
            if (id.equalsIgnoreCase(tree.getSapling())) {
                e.setCancelled(true);
                Blocks.remove(block);
                Schematic.pasteSchematic(e.getLocation(), tree);
                return;
            }
        }

        for (Berry berry : PlantContent.getBerries()) {
            if (id.equalsIgnoreCase(berry.toBush())) {
                e.setCancelled(true);
                ripenBush(block, berry);
                return;
            }
        }
    }

    /**
     * Turns a planted bush/sapling into its ripe form and stores the fruit id on the grown block(s).
     */
    private static void ripenBush(Block block, Berry berry) {
        switch (berry.getType()) {
            case BUSH:
                Materials.setBlock(block, Materials.OAK_LEAVES);
                break;
            case ORE_PLANT:
            case DOUBLE_PLANT: {
                Block above = block.getRelative(BlockFace.UP);
                if (Blocks.idAt(above) != null) {
                    return;
                }

                Material aboveType = above.getType();
                if (!BlockTags.isSapling(aboveType) && !BlockTags.isLeaves(aboveType)
                        && aboveType != Materials.AIR.material && aboveType != Materials.CAVE_AIR.material
                        && aboveType != Materials.SNOW.material) {
                    return;
                }

                // Order matters: the head must exist (and carry its skin) before storing,
                // because store stamps the id onto the head's tile state and setType would
                // wipe a state written too early.
                Materials.setBlock(block, Materials.OAK_LEAVES);
                placeFruitHead(above, berry);
                Blocks.store(above, berry.getItem());
                break;
            }
            default: {
                // FRUIT: a single ripe player-head on the same spot.
                placeFruitHead(block, berry);
                break;
            }
        }

        Blocks.remove(block);
        Blocks.store(block, berry.getItem());
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
    }

    private static void placeFruitHead(Block block, Berry berry) {
        Materials.setBlock(block, Materials.PLAYER_HEAD);
        rotateHead(block);
        SkullUtil.setSkin(block, berry.getTexture(), true);
    }

    private static void rotateHead(Block block) {
        BlockDataAccess.setRotation(block, FACES[ThreadLocalRandom.current().nextInt(FACES.length)]);
    }

    // ------------------------------------------------------------------
    // Natural world generation
    // ------------------------------------------------------------------

    @EventHandler
    public void onGenerate(ChunkPopulateEvent e) {
        final World world = e.getWorld();

        if (plugin.getConfig().getStringList("world-blacklist").contains(world.getName())) {
            return;
        }

        final Random random = ThreadLocalRandom.current();

        if (random.nextInt(100) < plugin.getConfig().getInt("chances.BUSH", 16)) {
            List<Berry> berries = PlantContent.getBerries();
            Berry berry = berries.get(random.nextInt(berries.size()));
            if (berry.getType() == PlantType.ORE_PLANT) {
                return;
            }

            final int x = e.getChunk().getX() * 16 + random.nextInt(16);
            final int z = e.getChunk().getZ() * 16 + random.nextInt(16);

            if (insideWorldBorder(world, x, z)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> growBush(world, x, z, berry));
            }
        } else if (random.nextInt(100) < plugin.getConfig().getInt("chances.TREE", 22)) {
            List<Tree> trees = PlantContent.getTrees();
            final Tree tree = trees.get(random.nextInt(trees.size()));

            final int x = e.getChunk().getX() * 16 + random.nextInt(16);
            final int z = e.getChunk().getZ() * 16 + random.nextInt(16);

            if (insideWorldBorder(world, x, z)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> pasteTree(world, x, z, tree));
            }
        }
    }

    private static boolean insideWorldBorder(World world, int x, int z) {
        // Mirrors the original addon's border check (border centred on the world origin).
        int limit = (int) world.getWorldBorder().getSize();
        return x < limit && x > -limit && z < limit && z > -limit;
    }

    // World#getMaxHeight() was added in 1.17; older worlds are 256 blocks tall.
    private static int getWorldMaxHeight(World world) {
        try {
            Object result = world.getClass().getMethod("getMaxHeight").invoke(world);
            return result instanceof Number ? ((Number) result).intValue() : 256;
        } catch (Throwable e) {
            return 256;
        }
    }

    private static void pasteTree(World world, int x, int z, Tree tree) {
        for (int y = getWorldMaxHeight(world); y > 30; y--) {
            Block current = world.getBlockAt(x, y, z);
            Material type = current.getType();
            if (!type.isSolid()
                    && type != Materials.WATER.material
                    && (Materials.SEAGRASS == null || type != Materials.SEAGRASS.material)
                    && (Materials.TALL_SEAGRASS == null || type != Materials.TALL_SEAGRASS.material)
                    && !BlockDataAccess.isWaterlogged(current)
                    && tree.isSoil(current.getRelative(0, -1, 0).getType())
                    && isFlat(current)) {
                Schematic.pasteSchematic(new Location(world, x, y, z), tree);
                break;
            }
        }
    }

    private static void growBush(World world, int x, int z, Berry berry) {
        for (int y = getWorldMaxHeight(world); y > 30; y--) {
            Block current = world.getBlockAt(x, y, z);
            Material type = current.getType();
            if (!type.isSolid()
                    && type != Materials.WATER.material
                    && berry.isSoil(current.getRelative(BlockFace.DOWN).getType())) {
                // Place the ripe blocks first, then store: store stamps the id onto a fruit
                // head's tile state, which setType/setSkin would otherwise clobber.
                Block above = null;
                switch (berry.getType()) {
                    case BUSH:
                        Materials.setBlock(current, Materials.OAK_LEAVES);
                        break;
                    case FRUIT:
                        placeFruitHead(current, berry);
                        break;
                    case ORE_PLANT:
                    case DOUBLE_PLANT:
                        above = current.getRelative(BlockFace.UP);
                        Materials.setBlock(current, Materials.OAK_LEAVES);
                        placeFruitHead(above, berry);
                        break;
                    default:
                        break;
                }
                Blocks.store(current, berry.getItem());
                if (above != null) {
                    Blocks.store(above, berry.getItem());
                }
                break;
            }
        }
    }

    private static boolean isFlat(Block current) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 6; k++) {
                    Block relative = current.getRelative(i, k, j);
                    if (relative.getType().isSolid() || BlockTags.isLeaves(relative.getType())
                            || !current.getRelative(i, -1, j).getType().isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Placement / breaking
    // ------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        String id = com.epic.exoticgarden.compat.ItemTags.getId(e.getItemInHand());
        // Record every EEG item placement (including fruit heads a player places as
        // decoration), not only "plantable" blocks, so the head stays identifiable
        // instead of becoming a texture-only dead skull.
        if (id != null && Items.getDef(id) != null) {
            Blocks.store(e.getBlock(), id);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();

        if (isPlayerHead(type, block) || BlockTags.isLeaves(type)) {
            dropFruitFromTree(block);
        }

        if (isShortGrass(type, block)) {
            if (!PlantContent.getGrassDrops().isEmpty() && e.getPlayer().getGameMode() != GameMode.CREATIVE
                    && ThreadLocalRandom.current().nextInt(100) < 6) {
                ItemStack[] items = PlantContent.getGrassDrops().values().toArray(new ItemStack[0]);
                block.getWorld().dropItemNaturally(block.getLocation(), items[ThreadLocalRandom.current().nextInt(items.length)]);
            }
            return;
        }

        ItemStack produce = harvestPlant(block);
        if (produce != null) {
            e.setCancelled(true);
            block.getWorld().dropItemNaturally(block.getLocation(), produce);
            return;
        }

        // Unripe bushes/saplings/plants and saplings stored inside grown tree leaves.
        String id = Blocks.idAt(block);
        if (id != null) {
            ItemStack stored = Items.get(id);
            if (stored != null) {
                e.setCancelled(true);
                Blocks.remove(block);
                block.getWorld().dropItemNaturally(block.getLocation(), stored);
                Materials.setBlock(block, Materials.AIR);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDecay(LeavesDecayEvent e) {
        Block block = e.getBlock();
        String id = Blocks.idAt(block);

        if (id != null) {
            for (Berry berry : PlantContent.getBerries()) {
                if (id.equalsIgnoreCase(berry.getID())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        dropFruitFromTree(block);

        ItemStack item = Blocks.retrieve(block);
        if (item != null) {
            e.setCancelled(true);
            Materials.setBlock(block, Materials.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || isOffHand(e) || e.getPlayer().isSneaking()) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        // Debounce: tools (e.g. the Crook wooden hoe) fire two right-click packets
        // per click. Ignore the second one when it arrives within the cooldown window.
        UUID playerId = e.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastHarvest.get(playerId);
        if (last != null && now - last < HARVEST_COOLDOWN_MS) {
            return;
        }

        // Stone Crook: harvest a 3x3 area centered on the clicked block.
        String toolId = ItemTags.getId(e.getPlayer().getInventory().getItemInMainHand());
        boolean isStoneCrook = "STONE_CROOK".equals(toolId);
        boolean isCrook = "CROOK".equals(toolId) || isStoneCrook;

        if (isStoneCrook) {
            // Orient the 3x3 grid by the player's cardinal facing direction so the harvest
            // area always extends left/right/forward/backward relative to where they look,
            // not just along the world X/Z axes.
            float yaw = (e.getPlayer().getLocation().getYaw() % 360 + 360) % 360;
            int fx, fz, rx, rz;
            if (yaw < 45 || yaw >= 315) {
                fx = 0; fz = 1; rx = -1; rz = 0;   // South
            } else if (yaw < 135) {
                fx = -1; fz = 0; rx = 0; rz = -1;  // West
            } else if (yaw < 225) {
                fx = 0; fz = -1; rx = 1; rz = 0;   // North
            } else {
                fx = 1; fz = 0; rx = 0; rz = 1;    // East
            }

            boolean harvested = false;
            // Grid extends forward from the clicked block (j=0 is the clicked row, j=1 and
            // j=2 are further away from the player), so clicking the nearest plant harvests
            // the 3x3 area in front of the player, not centered on the clicked block.
            for (int i = -1; i <= 1; i++) {
                for (int j = 0; j <= 2; j++) {
                    Block target = block.getRelative(rx * i + fx * j, 0, rz * i + fz * j);
                    ItemStack produce = harvestPlant(target);
                    if (produce != null) {
                        target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
                        target.getWorld().dropItemNaturally(target.getLocation(), produce);
                        harvested = true;
                        continue;
                    }
                    // Tree fruit heads in the area.
                    String fruitId = Blocks.idAt(target);
                    if (fruitId != null) {
                        for (Tree tree : PlantContent.getTrees()) {
                            if (fruitId.equalsIgnoreCase(tree.getFruitID())) {
                                harvestFruit(target);
                                harvested = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (harvested) {
                lastHarvest.put(playerId, now);
                damageTool(e.getPlayer());
                e.setCancelled(true);
            }
            return;
        }

        ItemStack produce = harvestPlant(block);
        if (produce != null) {
            lastHarvest.put(playerId, now);
            if (isCrook) {
                damageTool(e.getPlayer());
            }
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
            block.getWorld().dropItemNaturally(block.getLocation(), produce);
            e.setCancelled(true);
            return;
        }
        // Tree fruit heads are picked directly (no plant revert).
        String fruitId = Blocks.idAt(block);
        if (fruitId != null) {
            for (Tree tree : PlantContent.getTrees()) {
                if (fruitId.equalsIgnoreCase(tree.getFruitID())) {
                    lastHarvest.put(playerId, now);
                    if (isCrook) {
                        damageTool(e.getPlayer());
                    }
                    harvestFruit(block);
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Pistons: relocate block data together with pushed/pulled blocks
    // ------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        relocatePistonData(e.getBlocks(), e.getDirection());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        relocatePistonData(e.getBlocks(), e.getDirection());
    }

    /**
     * Piston-moved blocks keep their material and skin, but our coordinate records would stay
     * at the old positions, leaving dead heads at the destinations. The ids are collected and
     * the source records cleared before the move; on the next tick, once the move is complete,
     * they are stored again at the destination blocks (which also re-stamps head PDC).
     */
    private void relocatePistonData(List<Block> blocks, BlockFace direction) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        final Map<Location, String> moving = new HashMap<>();
        for (Block b : blocks) {
            String id = Blocks.idAt(b);
            if (id == null && isPlayerHead(b.getType(), b)) {
                id = BlockIds.getId(b);
            }
            if (id != null) {
                moving.put(b.getRelative(direction).getLocation(), id);
                Blocks.remove(b);
            }
        }

        if (moving.isEmpty()) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Map.Entry<Location, String> entry : moving.entrySet()) {
                Blocks.store(entry.getKey().getBlock(), entry.getValue());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeAll(getAffectedBlocks(e.blockList()));
    }

    /**
     * Harvests every ripe plant contained in the explosion's block list; harvested blocks are
     * removed from the list so the plant reverts to its sapling stage instead of being destroyed.
     */
    Set<Block> getAffectedBlocks(List<Block> blockList) {
        Set<Block> blocksToRemove = new HashSet<>();

        for (Block block : blockList) {
            ItemStack item = harvestPlant(block);
            if (item != null) {
                blocksToRemove.add(block);
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }

        return blocksToRemove;
    }

    // ------------------------------------------------------------------
    // Harvest helpers (static so other listeners may reuse them)
    // ------------------------------------------------------------------

    /**
     * Harvests a ripe plant at the given block, reverting it to its unripe bush/sapling form.
     * Returns the dropped produce, or {@code null} when the block is not a ripe plant.
     */
    public static ItemStack harvestPlant(Block block) {
        String id = Blocks.idAt(block);
        if (id == null) {
            return null;
        }

        for (Berry berry : PlantContent.getBerries()) {
            if (!id.equalsIgnoreCase(berry.getID())) {
                continue;
            }

            switch (berry.getType()) {
                case ORE_PLANT:
                case DOUBLE_PLANT: {
                    // The ripe plant spans two blocks (base + fruit head), both stored with the
                    // berry's fruit id. Only touch the counterpart when it really belongs to this
                    // berry - otherwise stacked or malformed plants bleed into foreign blocks
                    // (another plant's part, or even the ground below a lone head).
                    boolean clickedBase = BlockTags.isLeaves(block.getType());
                    Block counterpart = clickedBase
                            ? block.getRelative(BlockFace.UP)
                            : block.getRelative(BlockFace.DOWN);

                    boolean paired;
                    if (clickedBase) {
                        paired = isPlayerHead(counterpart.getType(), counterpart)
                                && berry.getID().equalsIgnoreCase(Blocks.idAt(counterpart));
                    } else {
                        paired = BlockTags.isLeaves(counterpart.getType())
                                && berry.getID().equalsIgnoreCase(Blocks.idAt(counterpart));
                    }

                    if (clickedBase) {
                        if (paired) {
                            Blocks.remove(counterpart);
                            counterpart.getWorld().playEffect(counterpart.getLocation(), Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
                            Materials.setBlock(counterpart, Materials.AIR);
                        }
                        Materials.setBlock(block, Materials.OAK_SAPLING);
                        Blocks.remove(block);
                        Blocks.store(block, berry.toBush());
                        return paired ? berry.getItem().clone() : null;
                    }

                    Blocks.remove(block);
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
                    Materials.setBlock(block, Materials.AIR);
                    if (paired) {
                        Materials.setBlock(counterpart, Materials.OAK_SAPLING);
                        Blocks.remove(counterpart);
                        Blocks.store(counterpart, berry.toBush());
                    }
                    return berry.getItem().clone();
                }
                default: {
                    Materials.setBlock(block, Materials.OAK_SAPLING);
                    Blocks.remove(block);
                    Blocks.store(block, berry.toBush());
                    return berry.getItem().clone();
                }
            }
        }

        return null;
    }

    /**
     * Picks a ripe tree-fruit head, dropping its fruit and clearing the block.
     */
    public static void harvestFruit(Block fruit) {
        Location loc = fruit.getLocation();
        String id = Blocks.idAt(fruit);
        if (id == null) {
            return;
        }

        for (Tree tree : PlantContent.getTrees()) {
            if (id.equalsIgnoreCase(tree.getFruitID())) {
                Blocks.remove(fruit);
                fruit.getWorld().playEffect(loc, Effect.STEP_SOUND, Materials.OAK_LEAVES.material);
                fruit.getWorld().dropItemNaturally(loc, tree.getFruit());
                Materials.setBlock(fruit, Materials.AIR);
                return;
            }
        }
    }

    /**
     * Damages the tool in the player's main hand by 1 point. When durability runs out the item
     * breaks (removed + break sound). Works across versions: 1.12 uses short durability on
     * the ItemStack itself; 1.13+ also supports the Damageable meta interface.
     */
    private static void damageTool(Player player) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) {
            return;
        }
        Material mat = tool.getType();
        int max = mat.getMaxDurability();
        if (max <= 0) {
            return;
        }

        int current = tool.getDurability();
        if (current + 1 >= max) {
            player.getInventory().setItemInMainHand(null);
            try {
                player.playSound(player.getEyeLocation(),
                        Sound.valueOf("ENTITY_ITEM_BREAK"), 1f, 1f);
            } catch (IllegalArgumentException ignored) {
                // Sound enum name varies across versions; break is silent on unknown.
            }
        } else {
            tool.setDurability((short) (current + 1));
            player.getInventory().setItemInMainHand(tool);
        }
    }

    private void dropFruitFromTree(Block block) {
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    Block fruit = block.getRelative(x, y, z);
                    if (isAir(fruit.getType())) {
                        continue;
                    }
                    harvestFruit(fruit);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Version-sensitive predicates
    // ------------------------------------------------------------------

    // Material#isAir() was added in 1.13; compare the resolved air materials directly on 1.12.
    private static boolean isAir(Material type) {
        return type == Materials.AIR.material || type == Materials.CAVE_AIR.material;
    }

    private static boolean isShortGrass(Material type, Block block) {
        if (type != Materials.SHORT_GRASS.material) {
            return false;
        }
        // Legacy 1.12: LONG_GRASS data 1 is the ordinary short grass (0 is shrub, 2 is fern).
        return !XMat.LEGACY || (block.getData() & 3) == 1;
    }

    private static boolean isPlayerHead(Material type, Block block) {
        if (type != Materials.PLAYER_HEAD.material) {
            return false;
        }
        // Legacy 1.12: SKULL data 1 is a floor skull (our fruit heads); data 2/3 are wall skulls.
        return !XMat.LEGACY || block.getData() == 1;
    }

    // PlayerInteractEvent#getHand() is 1.9+; on 1.12 there is only the main hand.
    private static boolean isOffHand(PlayerInteractEvent e) {
        try {
            Object hand = PlayerInteractEvent.class.getMethod("getHand").invoke(e);
            return hand != null && !"HAND".equals(((Enum<?>) hand).name());
        } catch (Throwable ex) {
            return false;
        }
    }
}
