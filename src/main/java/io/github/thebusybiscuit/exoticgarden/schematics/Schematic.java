package io.github.thebusybiscuit.exoticgarden.schematics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import io.github.thebusybiscuit.exoticgarden.MaterialCompat;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import io.github.thebusybiscuit.slimefun5.utils.compatibility.BlockDataCompat;
import io.github.thebusybiscuit.exoticgarden.ExoticGarden;
import io.github.thebusybiscuit.exoticgarden.Tree;
import io.github.thebusybiscuit.exoticgarden.schematics.org.jnbt.ByteArrayTag;
import io.github.thebusybiscuit.exoticgarden.schematics.org.jnbt.CompoundTag;
import io.github.thebusybiscuit.exoticgarden.schematics.org.jnbt.NBTInputStream;
import io.github.thebusybiscuit.exoticgarden.schematics.org.jnbt.ShortTag;
import io.github.thebusybiscuit.exoticgarden.schematics.org.jnbt.Tag;
import io.github.thebusybiscuit.slimefun5.utils.tags.SlimefunTag;
import io.github.thebusybiscuit.slimefun5.utils.compatibility.VersionedPlayerHead;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/*
 *
 * This class is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This class is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this class. If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * This class was originally written by Max but was modified for ExoticGarden under the same
 * license as the original work.
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

    /**
     * @return the blocks
     */
    public short[] getBlocks() {
        return blocks;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the width
     */
    public short getWidth() {
        return width;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @return the height
     */
    public short getHeight() {
        return height;
    }

    public static void pasteSchematic(Location loc, Tree tree) {
        Schematic schematic;

        try {
            schematic = tree.getSchematic();
        }
        catch (IOException e) {
            ExoticGarden.instance.getLogger().log(Level.WARNING, "Could not paste Schematic for Tree: " + tree.getFruitID() + "_TREE (" + e.getClass().getSimpleName() + ')', e);
            return;
        }

        BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
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
                    
                    if ((!blockType.isSolid() && !isInteractable(blockType) && !SlimefunTag.UNBREAKABLE_MATERIALS.isTagged(blockType)) || blockType == MaterialCompat.safe(XMaterial.AIR) || blockType == MaterialCompat.safe(XMaterial.CAVE_AIR) || io.github.thebusybiscuit.slimefun5.utils.compatibility.Tag.SAPLINGS.isTagged(blockType)) {
                        Material material = parseId(blocks[index], blockData[index]);

                        if (material != null) {
                            if (blocks[index] != 0) {
                                block.setType(material);
                            }

                            if (io.github.thebusybiscuit.slimefun5.utils.compatibility.Tag.LEAVES.isTagged(material)) {
                                if (ThreadLocalRandom.current().nextInt(100) < 25) {
                                    BlockStorage.store(block, tree.getItem());
                                }
                            }
                            else if (material == MaterialCompat.safe(XMaterial.PLAYER_HEAD)) {
                                Object data = BlockDataCompat.getBlockData(block);
                                BlockDataCompat.set(data, "setRotation", faces[ThreadLocalRandom.current().nextInt(faces.length)]);
                                BlockDataCompat.setBlockData(block, data);

                                VersionedPlayerHead.setSkin(block, VersionedPlayerHead.hashToBase64(tree.getTexture()), true);
                                BlockStorage.store(block, tree.getFruit());
                            }
                        }
                    }
                }
            }
        }
    }

    public static Material parseId(short blockId, byte blockData) {
        switch (blockId) {
        case 6:
            if (blockData == 0) return MaterialCompat.safe(XMaterial.OAK_SAPLING);
            if (blockData == 1) return MaterialCompat.safe(XMaterial.SPRUCE_SAPLING);
            if (blockData == 2) return MaterialCompat.safe(XMaterial.BIRCH_SAPLING);
            if (blockData == 3) return MaterialCompat.safe(XMaterial.JUNGLE_SAPLING);
            if (blockData == 4) return MaterialCompat.safe(XMaterial.ACACIA_SAPLING);
            if (blockData == 5) return MaterialCompat.safe(XMaterial.DARK_OAK_SAPLING);
            break;
        case 17:
            if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return MaterialCompat.safe(XMaterial.OAK_LOG);
            if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return MaterialCompat.safe(XMaterial.SPRUCE_LOG);
            if (blockData == 2 || blockData == 6 || blockData == 10 || blockData == 14) return MaterialCompat.safe(XMaterial.BIRCH_LOG);
            if (blockData == 3 || blockData == 7 || blockData == 11 || blockData == 15) return MaterialCompat.safe(XMaterial.JUNGLE_LOG);
            break;
        case 18:
            if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return MaterialCompat.safe(XMaterial.OAK_LEAVES);
            if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return MaterialCompat.safe(XMaterial.SPRUCE_LEAVES);
            if (blockData == 2 || blockData == 6 || blockData == 10 || blockData == 14) return MaterialCompat.safe(XMaterial.BIRCH_LEAVES);
            if (blockData == 3 || blockData == 7 || blockData == 11 || blockData == 15) return MaterialCompat.safe(XMaterial.JUNGLE_LEAVES);
            return MaterialCompat.safe(XMaterial.OAK_LEAVES);
        case 161:
            if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return MaterialCompat.safe(XMaterial.ACACIA_LEAVES);
            if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return MaterialCompat.safe(XMaterial.DARK_OAK_LEAVES);
            break;
        case 162:
            if (blockData == 0 || blockData == 4 || blockData == 8 || blockData == 12) return MaterialCompat.safe(XMaterial.ACACIA_LOG);
            if (blockData == 1 || blockData == 5 || blockData == 9 || blockData == 13) return MaterialCompat.safe(XMaterial.DARK_OAK_LOG);
            break;
        case 144:
            return MaterialCompat.safe(XMaterial.PLAYER_HEAD);
        default:
            return null;
        }

        return null;
    }

    // Material#isInteractable() was added in 1.12; resolve it reflectively so this class also loads and
    // pastes schematics on 1.8, where the absence of the method would otherwise crash.
    private static boolean isInteractable(Material material) {
        try {
            return (boolean) Material.class.getMethod("isInteractable").invoke(material);
        } catch (Throwable e) {
            return false;
        }
    }

    public static Schematic loadSchematic(File file) throws IOException {
        Map<String, Tag> schematic;

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
        short[] blocks = new short[blockId.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockId[index] & 0xFF);
            }
            else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                }
                else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        return new Schematic(file.getName().replace(".schematic", ""), blocks, blockData, width, length, height);
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items
     *            The parent tag map
     * @param key
     *            The name of the tag to get
     * @param expected
     *            The expected type of the tag
     * @return child tag casted to the expected type
     * @throws IllegalArgumentException
     *             if the tag does not exist or the tag is not of the
     *             expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }

        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }

}

