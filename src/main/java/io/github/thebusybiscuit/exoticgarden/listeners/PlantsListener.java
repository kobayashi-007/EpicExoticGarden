package io.github.thebusybiscuit.exoticgarden.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import io.github.thebusybiscuit.exoticgarden.MaterialCompat;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.exoticgarden.Berry;
import io.github.thebusybiscuit.exoticgarden.ExoticGarden;
import io.github.thebusybiscuit.exoticgarden.PlantType;
import io.github.thebusybiscuit.exoticgarden.Tree;
import io.github.thebusybiscuit.exoticgarden.schematics.Schematic;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun5.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.BlockDataCompat;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.Tag;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedPlayerHead;
import io.github.thebusybiscuit.slimefun5.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/**
 * Handles all plant-related events including growth, harvesting,
 * world generation, and block interactions.
 *
 * @author TheBusyBiscuit
 */
public class PlantsListener implements Listener {

    private final Config cfg;
    private final ExoticGarden plugin;
    private final BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public PlantsListener(ExoticGarden plugin) {
        this.plugin = plugin;
        cfg = plugin.getCfg();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        registerVersionGatedListeners();
    }

    /**
     * Registers listeners for events that are absent on the 1.8 API floor.
     *
     * @implNote BlockExplodeEvent (1.9+) and BlockFertilizeEvent (1.13+) do not exist on 1.8, so a
     *           listener class naming them would crash on load; they live in separate classes
     *           registered only when the corresponding event class is present.
     */
    private void registerVersionGatedListeners() {
        if (classExists("org.bukkit.event.block.BlockExplodeEvent")) {
            plugin.getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), plugin);
        }

        if (classExists("org.bukkit.event.block.BlockFertilizeEvent")) {
            plugin.getServer().getPluginManager().registerEvents(new BonemealListener(), plugin);
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

    @EventHandler
    public void onGrow(StructureGrowEvent e) {
        if (PaperLib.isPaper()) {
            if (PaperLib.isChunkGenerated(e.getLocation())) {
                growStructure(e);
            }
            else {
                PaperLib.getChunkAtAsync(e.getLocation()).thenRun(() -> growStructure(e));
            }
        }
        else {
            if (!e.getLocation().getChunk().isLoaded()) {
                e.getLocation().getChunk().load();
            }
            growStructure(e);
        }
    }

    @EventHandler
    public void onGenerate(ChunkPopulateEvent e) {
        final World world = e.getWorld();

        if (BlockStorage.getStorage(world) == null) {
            return;
        }

        if (!Slimefun.getWorldSettingsService().isWorldEnabled(world)) {
            return;
        }

        if (!cfg.getStringList("world-blacklist").contains(world.getName())) {
            Random random = ThreadLocalRandom.current();

            final int worldLimit = getWorldBorder(world);

            if (random.nextInt(100) < cfg.getInt("chances.BUSH")) {
                Berry berry = ExoticGarden.getBerries().get(random.nextInt(ExoticGarden.getBerries().size()));
                if (berry.getType().equals(PlantType.ORE_PLANT)) return;

                int chunkX = e.getChunk().getX();
                int chunkZ = e.getChunk().getZ();

                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);

                if ((x < worldLimit && x > -worldLimit) && (z < worldLimit && z > -worldLimit)) {
                    if (PaperLib.isPaper()) {
                        if (PaperLib.isChunkGenerated(world, chunkX, chunkZ)) {
                            growBush(e, x, z, berry, random, true);
                        }
                        else {
                            PaperLib.getChunkAtAsync(world, chunkX, chunkZ).thenRun(() -> growBush(e, x, z, berry, random, true));
                        }
                    }
                    else {
                        growBush(e, x, z, berry, random, false);
                    }
                }
            }
            else if (random.nextInt(100) < cfg.getInt("chances.TREE")) {
                Tree tree = ExoticGarden.getTrees().get(random.nextInt(ExoticGarden.getTrees().size()));

                int chunkX = e.getChunk().getX();
                int chunkZ = e.getChunk().getZ();

                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);

                if ((x < worldLimit && x > -worldLimit) && (z < worldLimit && z > -worldLimit)) {
                    if (PaperLib.isPaper()) {
                        if (PaperLib.isChunkGenerated(world, chunkX, chunkZ)) {
                            pasteTree(e, x, z, tree);
                        }
                        else {
                            PaperLib.getChunkAtAsync(world, chunkX, chunkZ).thenRun(() -> pasteTree(e, x, z, tree));
                        }
                    }
                    else {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> pasteTree(e, x, z, tree));
                    }
                }
            }
        }
    }

    private int getWorldBorder(World world) {
        return (int) world.getWorldBorder().getSize();
    }

    // World#getMaxHeight() was added in 1.17; on older servers (1.13-1.16) the build height is 256.
    private static int getWorldMaxHeight(World world) {
        try {
            return world.getMaxHeight();
        } catch (Throwable e) {
            return 256;
        }
    }

    private void growStructure(StructureGrowEvent e) {
        SlimefunItem item = BlockStorage.check(e.getLocation().getBlock());

        if (item != null) {
            e.setCancelled(true);
            for (Tree tree : ExoticGarden.getTrees()) {
                if (item.getId().equalsIgnoreCase(tree.getSapling())) {
                    BlockStorage.clearBlockInfo(e.getLocation());
                    Schematic.pasteSchematic(e.getLocation(), tree);
                    return;
                }
            }

            for (Berry berry : ExoticGarden.getBerries()) {
                if (item.getId().equalsIgnoreCase(berry.toBush())) {
                    switch (berry.getType()) {
                    case BUSH:
                        e.getLocation().getBlock().setType(MaterialCompat.safe(XMaterial.OAK_LEAVES));
                        break;
                    case ORE_PLANT:
                    case DOUBLE_PLANT:
                        Block blockAbove = e.getLocation().getBlock().getRelative(BlockFace.UP);
                        item = BlockStorage.check(blockAbove);
                        if (item != null) return;

                        if (!Tag.SAPLINGS.isTagged(blockAbove.getType()) && !Tag.LEAVES.isTagged(blockAbove.getType())) {
                            switch (blockAbove.getType()) {
                            case AIR:
                            case CAVE_AIR:
                            case SNOW:
                                break;
                            default:
                                return;
                            }
                        }

                        BlockStorage.store(blockAbove, berry.getItem());
                        e.getLocation().getBlock().setType(MaterialCompat.safe(XMaterial.OAK_LEAVES));
                        blockAbove.setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                        rotateHead(blockAbove);

                        VersionedPlayerHead.setSkin(blockAbove, VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                        break;
                    default:
                        e.getLocation().getBlock().setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                        rotateHead(e.getLocation().getBlock());

                        VersionedPlayerHead.setSkin(e.getLocation().getBlock(), VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                        break;
                    }

                    BlockStorage.deleteLocationInfoUnsafely(e.getLocation(), false);
                    BlockStorage.store(e.getLocation().getBlock(), berry.getItem());
                    e.getWorld().playEffect(e.getLocation(), Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.OAK_LEAVES));
                    break;
                }
            }
        }
    }

    private void pasteTree(ChunkPopulateEvent e, int x, int z, Tree tree) {
        for (int y = getWorldMaxHeight(e.getWorld()); y > 30; y--) {
            Block current = e.getWorld().getBlockAt(x, y, z);
            if (!current.getType().isSolid() && current.getType() != MaterialCompat.safe(XMaterial.WATER) && current.getType() != MaterialCompat.safe(XMaterial.SEAGRASS) && current.getType() != MaterialCompat.safe(XMaterial.TALL_SEAGRASS) && !isWaterlogged(current) && tree.isSoil(current.getRelative(0, -1, 0).getType()) && isFlat(current)) {
                Schematic.pasteSchematic(new Location(e.getWorld(), x, y, z), tree);
                break;
            }
        }
    }

    private void growBush(ChunkPopulateEvent e, int x, int z, Berry berry, Random random, boolean isPaper) {
        for (int y = getWorldMaxHeight(e.getWorld()); y > 30; y--) {
            Block current = e.getWorld().getBlockAt(x, y, z);
            if (!current.getType().isSolid() && current.getType() != MaterialCompat.safe(XMaterial.WATER) && berry.isSoil(current.getRelative(BlockFace.DOWN).getType())) {
                BlockStorage.store(current, berry.getItem());
                switch (berry.getType()) {
                case BUSH:
                    if (isPaper) {
                        current.setType(MaterialCompat.safe(XMaterial.OAK_LEAVES));
                    }
                    else {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> current.setType(MaterialCompat.safe(XMaterial.OAK_LEAVES)));
                    }
                    break;
                case FRUIT:
                    if (isPaper) {
                        current.setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                        rotateHead(current);
                        VersionedPlayerHead.setSkin(current, VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                    }
                    else {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            current.setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                            rotateHead(current);
                            VersionedPlayerHead.setSkin(current, VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                        });
                    }
                    break;
                case ORE_PLANT:
                case DOUBLE_PLANT:
                    if (isPaper) {
                        current.setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                        rotateHead(current);
                        VersionedPlayerHead.setSkin(current, VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                    }
                    else {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            BlockStorage.store(current.getRelative(BlockFace.UP), berry.getItem());
                            current.setType(MaterialCompat.safe(XMaterial.OAK_LEAVES));
                            current.getRelative(BlockFace.UP).setType(MaterialCompat.safe(XMaterial.PLAYER_HEAD));
                            rotateHead(current.getRelative(BlockFace.UP));
                            VersionedPlayerHead.setSkin(current.getRelative(BlockFace.UP), VersionedPlayerHead.hashToBase64(berry.getTexture()), true);
                        });
                    }
                    break;
                default:
                    break;
                }
                break;
            }
        }
    }

    private boolean isFlat(Block current) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 6; k++) {
                    if (current.getRelative(i, k, j).getType().isSolid() || Tag.LEAVES.isTagged(current.getRelative(i, k, j).getType()) || !current.getRelative(i, -1, j).getType().isSolid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent e) {
        if (Slimefun.getProtectionManager().hasPermission(e.getPlayer(), e.getBlock().getLocation(), Interaction.BREAK_BLOCK)) {
            if (e.getBlock().getType().equals(MaterialCompat.safe(XMaterial.PLAYER_HEAD)) || Tag.LEAVES.isTagged(e.getBlock().getType())) {
                dropFruitFromTree(e.getBlock());
            }

            if (e.getBlock().getType() == MaterialCompat.safe(XMaterial.SHORT_GRASS)) {
                if (!ExoticGarden.getGrassDrops().keySet().isEmpty() && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    Random random = ThreadLocalRandom.current();

                    if (random.nextInt(100) < 6) {
                        ItemStack[] items = ExoticGarden.getGrassDrops().values().toArray(new ItemStack[0]);
                        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), items[random.nextInt(items.length)]);
                    }
                }
            }
            else {
                ItemStack item = ExoticGarden.harvestPlant(e.getBlock());

                if (item != null) {
                    e.setCancelled(true);
                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDecay(LeavesDecayEvent e) {
        if (!Slimefun.getWorldSettingsService().isWorldEnabled(e.getBlock().getWorld())) {
            return;
        }

        String id = BlockStorage.checkID(e.getBlock());

        if (id != null) {
            for (Berry berry : ExoticGarden.getBerries()) {
                if (id.equalsIgnoreCase(berry.getID())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        dropFruitFromTree(e.getBlock());
        ItemStack item = BlockStorage.retrieve(e.getBlock());

        if (item != null) {
            e.setCancelled(true);
            e.getBlock().setType(MaterialCompat.safe(XMaterial.AIR));
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (isOffHand(e)) return;
        if (e.getPlayer().isSneaking()) return;

        if (Slimefun.getProtectionManager().hasPermission(e.getPlayer(), e.getClickedBlock().getLocation(), Interaction.BREAK_BLOCK)) {
            ItemStack item = ExoticGarden.harvestPlant(e.getClickedBlock());

            if (item != null) {
                e.getClickedBlock().getWorld().playEffect(e.getClickedBlock().getLocation(), Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.OAK_LEAVES));
                e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), item);
            } else {
                ExoticGarden.getInstance().harvestFruit(e.getClickedBlock());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeAll(getAffectedBlocks(e.blockList()));
    }

    Set<Block> getAffectedBlocks(List<Block> blockList) {
        Set<Block> blocksToRemove = new HashSet<>();

        for (Block block : blockList) {
            ItemStack item = ExoticGarden.harvestPlant(block);

            if (item != null) {
                blocksToRemove.add(block);
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }

        return blocksToRemove;
    }

    private void dropFruitFromTree(Block block) {
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    Block fruit = block.getRelative(x, y, z);
                    // Block#isEmpty() was added after 1.13; Material#isAir() covers AIR/CAVE_AIR/VOID_AIR.
                    if (fruit.getType().isAir()) continue;


                    Location loc = fruit.getLocation();
                    SlimefunItem check = BlockStorage.check(loc);
                    if (check == null) continue;
                    for (Tree tree : ExoticGarden.getTrees()) {
                        if (check.getId().equalsIgnoreCase(tree.getFruitID())) {
                            BlockStorage.clearBlockInfo(loc);
                            ItemStack fruits = check.getItem();
                            fruit.getWorld().playEffect(loc, Effect.STEP_SOUND, MaterialCompat.safe(XMaterial.OAK_LEAVES));
                            fruit.getWorld().dropItemNaturally(loc, fruits);
                            fruit.setType(MaterialCompat.safe(XMaterial.AIR));
                            break;
                        }
                    }
                }
            }
        }
    }

    // Player heads carry rotation via BlockData (1.13+); on legacy versions this is a no-op (heads are
    // placed with their default rotation). Block data is held as an opaque Object so this class loads on 1.8.
    private void rotateHead(Block block) {
        Object data = BlockDataCompat.getBlockData(block);

        if (data != null) {
            BlockDataCompat.set(data, "setRotation", faces[ThreadLocalRandom.current().nextInt(faces.length)]);
            BlockDataCompat.setBlockData(block, data);
        }
    }

    // Waterlogged is a 1.13+ BlockData trait; on legacy versions blocks are never waterlogged.
    private boolean isWaterlogged(Block block) {
        Object data = BlockDataCompat.getBlockData(block);
        return BlockDataCompat.isInstance(data, "org.bukkit.block.data.Waterlogged") && Boolean.TRUE.equals(BlockDataCompat.get(data, "isWaterlogged"));
    }

    // PlayerInteractEvent#getHand() / EquipmentSlot are 1.9+; resolve reflectively so the handler runs
    // on 1.8 (where there is only a main hand, so this is never an off-hand interaction).
    private boolean isOffHand(PlayerInteractEvent e) {
        try {
            Object hand = PlayerInteractEvent.class.getMethod("getHand").invoke(e);
            return hand != null && !"HAND".equals(((Enum<?>) hand).name());
        } catch (Throwable ex) {
            return false;
        }
    }

}

