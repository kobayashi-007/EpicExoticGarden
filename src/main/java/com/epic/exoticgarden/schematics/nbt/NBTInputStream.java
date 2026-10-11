/*
 * Minimal NBT reader adapted from the JNBT library (GPLv3), originally by Graham Edgecombe,
 * adapted for EpicExoticGarden. See Tag.java.
 */
package com.epic.exoticgarden.schematics.nbt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Reads NBT-tagged, gzip-compressed input streams (classic .schematic format).
 */
public class NBTInputStream implements Closeable {

    // NBT tag type ids.
    private static final int TYPE_END = 0;
    private static final int TYPE_BYTE = 1;
    private static final int TYPE_SHORT = 2;
    private static final int TYPE_INT = 3;
    private static final int TYPE_LONG = 4;
    private static final int TYPE_FLOAT = 5;
    private static final int TYPE_DOUBLE = 6;
    private static final int TYPE_BYTE_ARRAY = 7;
    private static final int TYPE_STRING = 8;
    private static final int TYPE_LIST = 9;
    private static final int TYPE_COMPOUND = 10;

    private final DataInputStream is;

    public NBTInputStream(InputStream input) throws IOException {
        this.is = new DataInputStream(new GZIPInputStream(input));
    }

    public Tag<?> readTag() throws IOException {
        int type = is.readByte() & 0xFF;
        String name = "";
        if (type != TYPE_END) {
            int nameLength = is.readShort() & 0xFFFF;
            byte[] nameBytes = new byte[nameLength];
            is.readFully(nameBytes);
            name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);
        }
        return readTagPayload(type, name);
    }

    private Tag<?> readTagPayload(int type, String name) throws IOException {
        switch (type) {
            case TYPE_END:
                if (name.isEmpty()) {
                    return new EndTag();
                }
                throw new IOException("Unexpected TAG_End with a name.");
            case TYPE_BYTE:
                return new ByteTag(name, is.readByte());
            case TYPE_SHORT:
                return new ShortTag(name, is.readShort());
            case TYPE_INT:
                return new IntTag(name, is.readInt());
            case TYPE_LONG:
                return new LongTag(name, is.readLong());
            case TYPE_FLOAT:
                return new FloatTag(name, is.readFloat());
            case TYPE_DOUBLE:
                return new DoubleTag(name, is.readDouble());
            case TYPE_BYTE_ARRAY: {
                int length = is.readInt();
                byte[] bytes = new byte[length];
                is.readFully(bytes);
                return new ByteArrayTag(name, bytes);
            }
            case TYPE_STRING: {
                int length = is.readShort();
                byte[] bytes = new byte[length];
                is.readFully(bytes);
                return new StringTag(name, new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
            }
            case TYPE_LIST: {
                int childType = is.readByte();
                int length = is.readInt();
                Class<? extends Tag<?>> tagClass = tagClass(childType);
                List<Tag<?>> list = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Tag<?> child = readTagPayload(childType, "");
                    if (tagClass.isInstance(child)) {
                        list.add(child);
                    }
                }
                return new ListTag(name, tagClass, list);
            }
            case TYPE_COMPOUND: {
                Map<String, Tag<?>> map = new HashMap<>();
                Tag<?> child;
                do {
                    child = readTag();
                    if (child instanceof EndTag) {
                        break;
                    }
                    map.put(child.getName(), child);
                } while (true);
                return new CompoundTag(name, map);
            }
            default:
                throw new IOException("Invalid tag type: " + type + '.');
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Tag<?>> tagClass(int type) throws IOException {
        switch (type) {
            case TYPE_END: return (Class<? extends Tag<?>>) (Class<?>) EndTag.class;
            case TYPE_BYTE: return (Class<? extends Tag<?>>) (Class<?>) ByteTag.class;
            case TYPE_SHORT: return (Class<? extends Tag<?>>) (Class<?>) ShortTag.class;
            case TYPE_INT: return (Class<? extends Tag<?>>) (Class<?>) IntTag.class;
            case TYPE_LONG: return (Class<? extends Tag<?>>) (Class<?>) LongTag.class;
            case TYPE_FLOAT: return (Class<? extends Tag<?>>) (Class<?>) FloatTag.class;
            case TYPE_DOUBLE: return (Class<? extends Tag<?>>) (Class<?>) DoubleTag.class;
            case TYPE_BYTE_ARRAY: return (Class<? extends Tag<?>>) (Class<?>) ByteArrayTag.class;
            case TYPE_STRING: return (Class<? extends Tag<?>>) (Class<?>) StringTag.class;
            case TYPE_LIST: return (Class<? extends Tag<?>>) (Class<?>) ListTag.class;
            case TYPE_COMPOUND: return (Class<? extends Tag<?>>) (Class<?>) CompoundTag.class;
            default: throw new IOException("Invalid tag type: " + type + '.');
        }
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
