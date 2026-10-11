package com.epic.exoticgarden.schematics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.epic.exoticgarden.EpicExoticGarden;
import com.epic.exoticgarden.compat.BlockDataAccess;
import com.epic.exoticgarden.compat.BlockTags;
import com.epic.exoticgarden.compat.Materials;
import com.epic.exoticgarden.compat.SkullUtil;
import com.epic.exoticgarden.compat.XMat;
import com.epic.exoticgarden.core.Blocks;
import com.epic.exoticgarden.plant.Tree;
import com.epic.exoticgarden.schematics.nbt.ByteArrayTag;
import com.epic.exoticgarden.schematics.nbt.CompoundTag;
import com.epic.exoticgarden.schematics.nbt.NBTInputStream;
import com.epic.exoticgarden.schematics.nbt.ShortTag;
import com.epic.exoticgarden.schematics.nbt.Tag;

/*
 * This class was originally written by Max and was modified for ExoticGarden under the GPLv3.
 * EpicExoticGarden keeps the same license and has replaced all Slimefun dependencies with its
 * own standalone helpers.
 */

/**
 * Loads and pastes classic (pre-sponge) .schematic files for custom fruit trees.
 *
 * @author Max
 * @author TheBusyBiscuit
 */
public class Schematic {

    private final short[] blocks;
    private final byte[] data;
    private final short width;
    private final short length;
    private final short height;
    private final String name;

    public Schematic(String name, short[] blocks, byte[] data, short width, short length, short height) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
        this.name = name;
    }

    public short[] getBlocks() {
        return blocks;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public short getWidth() {
        return width;
    }

    public short getLength() {
        return length;
    }

    public short getHeight() {
        return height;
    }

    private static final BlockFace[] FACES = {
        BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
        BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };

    public static void pasteSchematic(Location loc, Tree tree) {
        Schematic schematic;

        try {
            schematic = tree.getSchematic();
        } catch (IOException e) {
            EpicExoticGarden.getInstance().getLogger().log(Level.WARNING,
                    "Could not paste Schematic for Tree: " + tree.getFruitID() + "_TREE (" + e.getClass().getSimpleName() + ')', e);
            return;
        }

        short[] blocks = schematic.getBlocks();
        byte[] blockData = schematic.getData();

        short length = schematic.getLength();
        short width = schematic.getWidth();
        short height = schematic.getHeight();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;

                    int blockX = x + loc.getBlockX() - length / 2;
                    int blockY = y + loc.getBlockY();
                    int blockZ = z + loc.getBlockZ() - width / 2;
                    Block block = new Location(loc.getWorld(), blockX, blockY, blockZ).getBlock();
                    Material blockType = block.getType();

                    if ((!blockType.isSolid() && !isInteractable(blockType) && !BlockTags.isUnbreakable(blockType))
                            || blockType == Materials.AIR.material || blockType == Materials.CAVE_AIR.material
                            || BlockTags.isSapling(blockType)) {
                        XMat material = parseId(blocks[index], blockData[index]);

                        if (material != null) {
                            if (blocks[index] != 0) {
                                Materials.setBlock(block, material);
                            }

                            if (BlockTags.isLeaves(material.material)) {
                                if (ThreadLocalRandom.current().nextInt(100) < 25) {
                                    Blocks.store(block, tree.getSapling());
                                }
                            } else if (material.material == Materials.PLAYER_HEAD.material) {
                                BlockDataAccess.setRotation(block, FACES[ThreadLocalRandom.current().nextInt(FACES.length)]);
                                SkullUtil.setSkin(block, tree.getTexture(), true);
                                Blocks.store(block, tree.getFruitID());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps legacy block id/data pairs from the schematic format to version-safe materials.
     */
    public static XMat parseId(short blockId, byte blockData) {
        switch (blockId) {
            case 6:
                if (blockData == 0) return Materials.OAK_SAPLING;
                if (blockData == 1) return Materials.SPRUCE_SAPLING;
                if (blockData == 2) return Materials.BIRCH_SAPLING;
                if (blockData == 3) return Materials.JUNGLE_SAPLING;
                if (blockData == 4) return Materials.ACACIA_SAPLING;
                if (blockData == 5) return Materials.DARK_OAK_SAPLING;
                break;
            case 17:
                if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return Materials.OAK_LOG;
                if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return Materials.SPRUCE_LOG;
                if (blockData == 2 || blockData == 6 || blockData == 10 || blockData == 14) return Materials.BIRCH_LOG;
                if (blockData == 3 || blockData == 7 || blockData == 11 || blockData == 15) return Materials.JUNGLE_LOG;
                break;
            case 18:
                if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return Materials.OAK_LEAVES;
                if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return Materials.SPRUCE_LEAVES;
                if (blockData == 2 || blockData == 6 || blockData == 10 || blockData == 14) return Materials.BIRCH_LEAVES;
                if (blockData == 3 || blockData == 7 || blockData == 11 || blockData == 15) return Materials.JUNGLE_LEAVES;
                return Materials.OAK_LEAVES;
            case 161:
                if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return Materials.ACACIA_LEAVES;
                if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return Materials.DARK_OAK_LEAVES;
                break;
            case 162:
                if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return Materials.ACACIA_LOG;
                if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return Materials.DARK_OAK_LOG;
                break;
            case 144:
                return Materials.PLAYER_HEAD;
            default:
                return null;
        }

        return null;
    }

    // Material#isInteractable() was added in 1.12; resolve reflectively so older servers still load.
    private static boolean isInteractable(Material material) {
        try {
            return (boolean) Material.class.getMethod("isInteractable").invoke(material);
        } catch (Throwable e) {
            return false;
        }
    }

    public static Schematic loadSchematic(File file) throws IOException {
        Map<String, Tag<?>> schematic;

        try (NBTInputStream stream = new NBTInputStream(new FileInputStream(file))) {
            CompoundTag schematicTag = (CompoundTag) stream.readTag();

            if (!schematicTag.getName().equals("Schematic")) {
                throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
            }

            schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
            }
        }

        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

        byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length];

        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) {
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        return new Schematic(file.getName().replace(".schematic", ""), blocks, blockData, width, length, height);
    }

    private static <T extends Tag<?>> T getChildTag(Map<String, Tag<?>> items, String key, Class<T> expected) {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }

        Tag<?> tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }

}
